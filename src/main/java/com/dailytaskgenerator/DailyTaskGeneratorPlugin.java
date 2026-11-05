package com.dailytaskgenerator;

import com.dailytaskgenerator.data.CombatAchievementLoader;
import com.dailytaskgenerator.data.DiaryRegion;
import com.dailytaskgenerator.data.QuestRequirementLoader;
import com.dailytaskgenerator.generate.CombatAchievementTaskProvider;
import com.dailytaskgenerator.generate.DiaryTaskProvider;
import com.dailytaskgenerator.generate.QuestTaskProvider;
import com.dailytaskgenerator.generate.SkillTaskProvider;
import com.dailytaskgenerator.generate.TaskGenerator;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.DailyTaskSet;
import com.dailytaskgenerator.model.Streak;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.notify.TaskNotifier;
import com.dailytaskgenerator.player.PlayerSnapshot;
import com.dailytaskgenerator.player.PlayerSnapshotBuilder;
import com.dailytaskgenerator.store.TaskStore;
import com.dailytaskgenerator.track.TaskTracker;
import com.dailytaskgenerator.ui.DailyTaskPanel;
import com.dailytaskgenerator.ui.TaskActions;
import com.google.inject.Provides;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Daily Task Generator",
	description = "Generates daily tasks tailored to your character",
	tags = {"daily", "tasks", "goals", "quests", "diary", "combat achievements"}
)
public class DailyTaskGeneratorPlugin extends Plugin implements TaskActions
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private EventBus eventBus;

	@Inject
	private DailyTaskGeneratorConfig config;

	@Inject
	private PlayerSnapshotBuilder snapshotBuilder;

	@Inject
	private SkillTaskProvider skillTaskProvider;

	@Inject
	private QuestTaskProvider questTaskProvider;

	@Inject
	private QuestRequirementLoader questRequirementLoader;

	@Inject
	private CombatAchievementTaskProvider caTaskProvider;

	@Inject
	private CombatAchievementLoader caLoader;

	@Inject
	private DiaryTaskProvider diaryTaskProvider;

	@Inject
	private TaskStore taskStore;

	@Inject
	private TaskNotifier notifier;

	private TaskGenerator generator;
	private DailyTaskPanel panel;
	private NavigationButton navButton;
	private SettingsLink settingsLink;

	// Client-thread state
	private DailyTaskSet taskSet;
	private Streak streak = new Streak();
	private long accountHash = -1;
	private boolean refreshPending;
	private boolean questCheckPending;
	private int ticksSinceQuestCheck;

	/**
	 * Minimum ticks between quest-state checks; each check runs one client script per quest task.
	 */
	private static final int QUEST_CHECK_INTERVAL = 5;

	@Override
	protected void startUp()
	{
		generator = new TaskGenerator(List.of(skillTaskProvider, questTaskProvider, diaryTaskProvider, caTaskProvider));
		settingsLink = new SettingsLink(this);
		panel = new DailyTaskPanel(this);

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Daily Tasks")
			.icon(icon)
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				refreshPending = true;
			}
		});
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		navButton = null;
		settingsLink = null;
		panel = null;
		generator = null;
		notifier.reset();
		taskSet = null;
		streak = new Streak();
		accountHash = -1;
		refreshPending = false;
		questCheckPending = false;
		questTaskProvider.setRequirements(Collections.emptyMap());
		caTaskProvider.setAchievements(Collections.emptyList());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGED_IN:
				// Stats aren't reliable until the first tick after login.
				refreshPending = true;
				break;
			case LOGIN_SCREEN:
				notifier.reset();
				taskSet = null;
				streak = new Streak();
				accountHash = -1;
				SwingUtilities.invokeLater(() ->
				{
					if (panel != null)
					{
						panel.showLoggedOut();
					}
				});
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		ticksSinceQuestCheck++;
		notifier.tick();
		if (refreshPending)
		{
			refreshPending = false;
			refresh();
		}
		else if (questCheckPending && ticksSinceQuestCheck >= QUEST_CHECK_INTERVAL)
		{
			checkQuestTasks();
		}
		else if (taskSet != null && !today().equals(taskSet.getDate()))
		{
			log.debug("UTC day rolled over, generating new tasks");
			generateNewSet(snapshotBuilder.build());
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		// The profile decides which saved tasks belong to the account, so reload once it's known.
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			refreshPending = true;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Quest progress is stored in varps/varbits. Just flag it; the check happens on a tick.
		questCheckPending = true;

		if (taskSet == null)
		{
			return;
		}
		CompletionCheck check = new CompletionCheck();
		boolean changed = false;
		if (isCaCompletionVarp(event.getVarpId()))
		{
			changed = TaskTracker.onCombatAchievementVarps(taskSet, snapshotBuilder.readCaVarps());
		}
		else if (DiaryRegion.isCompletionVarbit(event.getVarbitId()))
		{
			changed = TaskTracker.onDiaryTiers(taskSet, snapshotBuilder.readDiaryTiers());
		}
		if (changed)
		{
			afterCompletionCheck(check);
		}
	}

	private static boolean isCaCompletionVarp(int varpId)
	{
		for (int varp : CombatAchievementLoader.COMPLETION_VARPS)
		{
			if (varp == varpId)
			{
				return true;
			}
		}
		return false;
	}

	private void checkQuestTasks()
	{
		questCheckPending = false;
		ticksSinceQuestCheck = 0;
		if (taskSet == null)
		{
			return;
		}
		CompletionCheck check = new CompletionCheck();
		Map<Quest, QuestState> states = new EnumMap<>(Quest.class);
		for (DailyTask task : taskSet.getTasks())
		{
			if (task.getType() == TaskType.QUEST && task.getQuest() != null && !task.isCompleted())
			{
				states.put(task.getQuest(), task.getQuest().getState(client));
			}
		}
		if (!states.isEmpty() && TaskTracker.onQuestStates(taskSet, states))
		{
			afterCompletionCheck(check);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (taskSet == null)
		{
			return;
		}
		CompletionCheck check = new CompletionCheck();
		if (TaskTracker.onXp(taskSet, event.getSkill(), event.getXp()))
		{
			// Progress itself is recomputed from XP on load, so only completions need saving.
			if (check.anyNewlyCompleted())
			{
				afterCompletionCheck(check);
			}
			else
			{
				pushToPanel();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (DailyTaskGeneratorConfig.GROUP.equals(event.getGroup()) && "rerollsPerDay".equals(event.getKey()))
		{
			clientThread.invoke(this::pushToPanel);
		}
	}

	private void refresh()
	{
		if (!taskStore.isProfileReady())
		{
			// Profile isn't set for this account yet; try again next tick.
			refreshPending = true;
			return;
		}

		if (!questTaskProvider.hasRequirements())
		{
			// Cache data is only available once logged in; it doesn't change, so load it once.
			questTaskProvider.setRequirements(questRequirementLoader.load());
		}
		if (!caTaskProvider.hasAchievements())
		{
			caTaskProvider.setAchievements(caLoader.load());
		}

		PlayerSnapshot snapshot = snapshotBuilder.build();
		if (taskSet != null && snapshot.getAccountHash() == accountHash && today().equals(taskSet.getDate()))
		{
			pushToPanel();
			return;
		}

		accountHash = snapshot.getAccountHash();
		streak = taskStore.loadStreak();
		DailyTaskSet saved = taskStore.load();
		if (saved != null && today().equals(saved.getDate()))
		{
			taskSet = saved;
			log.debug("Loaded {} saved tasks for {}", saved.getTasks().size(), saved.getDate());
			// Tasks finished while logged out (e.g. on mobile) complete now, with the usual messages.
			CompletionCheck check = new CompletionCheck();
			if (TaskTracker.sync(taskSet, snapshot))
			{
				afterCompletionCheck(check);
			}
			else
			{
				pushToPanel();
			}
		}
		else
		{
			generateNewSet(snapshot);
		}
	}

	/**
	 * Snapshot of which tasks were done before a change, so the change can be compared against it.
	 */
	private final class CompletionCheck
	{
		private final Set<String> doneBefore = taskSet.getTasks().stream()
			.filter(DailyTask::isDone).map(DailyTask::getId).collect(Collectors.toSet());
		private final boolean allDoneBefore = allDone();

		boolean anyNewlyCompleted()
		{
			return taskSet.getTasks().stream().anyMatch(t -> t.isDone() && !doneBefore.contains(t.getId()));
		}
	}

	private boolean allDone()
	{
		return taskSet != null && !taskSet.getTasks().isEmpty() && taskSet.getTasks().stream().allMatch(DailyTask::isDone);
	}

	/**
	 * Call after anything that may have completed tasks: announces newly completed tasks (ticked
	 * ones only if that setting is on), records the streak when the last task is finished, then
	 * saves and refreshes the panel.
	 */
	private void afterCompletionCheck(CompletionCheck check)
	{
		int total = taskSet.getTasks().size();
		for (DailyTask task : taskSet.getTasks())
		{
			boolean announce = task.isCompleted() || config.announceTickedTasks();
			if (announce && task.isDone() && !check.doneBefore.contains(task.getId()))
			{
				notifier.taskCompleted(task.getTitle(), taskSet.completedCount(), total);
			}
		}
		if (!check.allDoneBefore && allDone() && today().equals(taskSet.getDate()))
		{
			Streak updated = streak.record(taskSet.getDate());
			if (updated != streak)
			{
				streak = updated;
				taskStore.saveStreak(streak);
				notifier.allCompleted(streak.getCurrent());
			}
		}
		save();
		pushToPanel();
	}

	private void save()
	{
		if (taskSet != null && taskStore.isProfileReady())
		{
			taskStore.save(taskSet);
		}
	}

	private void generateNewSet(PlayerSnapshot snapshot)
	{
		DailyTaskSet set = new DailyTaskSet();
		set.setDate(today());
		set.setRerollCount(0);
		set.setTasks(generator.generate(snapshot, config.tasksPerDay(),
			TaskGenerator.seed(snapshot.getAccountHash(), set.getDate(), 0)));
		taskSet = set;
		log.debug("Generated {} tasks for {}", set.getTasks().size(), set.getDate());
		save();
		pushToPanel();
	}

	private int rerollsLeft()
	{
		return taskSet == null ? 0 : Math.max(0, config.rerollsPerDay() - taskSet.getRerollCount());
	}

	/**
	 * Client thread only. Hands the panel a copy so Swing never sees state being mutated.
	 */
	private void pushToPanel()
	{
		if (taskSet == null)
		{
			return;
		}
		DailyTaskSet copy = taskSet.copy();
		int rerolls = rerollsLeft();
		int currentStreak = streak.currentAsOf(today());
		int bestStreak = streak.getBest();
		SwingUtilities.invokeLater(() ->
		{
			if (panel != null)
			{
				panel.showTasks(copy, rerolls, currentStreak, bestStreak);
			}
		});
	}

	private Random rerollRandom()
	{
		return new Random(TaskGenerator.seed(accountHash, taskSet.getDate(), taskSet.getRerollCount()));
	}

	private Set<String> currentIds()
	{
		return taskSet.getTasks().stream().map(DailyTask::getId).collect(Collectors.toSet());
	}

	@Override
	public void setManuallyCompleted(String taskId, boolean done)
	{
		clientThread.invoke(() ->
		{
			if (taskSet == null)
			{
				return;
			}
			CompletionCheck check = new CompletionCheck();
			taskSet.getTasks().stream()
				.filter(t -> t.getId().equals(taskId))
				.forEach(t -> t.setManuallyCompleted(done));
			afterCompletionCheck(check);
		});
	}

	@Override
	public void reroll(String taskId)
	{
		clientThread.invoke(() ->
		{
			if (taskSet == null || rerollsLeft() <= 0)
			{
				return;
			}
			List<DailyTask> tasks = taskSet.getTasks();
			int index = -1;
			for (int i = 0; i < tasks.size(); i++)
			{
				if (tasks.get(i).getId().equals(taskId) && !tasks.get(i).isDone())
				{
					index = i;
				}
			}
			if (index < 0)
			{
				return;
			}

			taskSet.setRerollCount(taskSet.getRerollCount() + 1);
			List<DailyTask> replacement = generator.pick(snapshotBuilder.build(), 1, currentIds(), rerollRandom());
			if (replacement.isEmpty())
			{
				log.debug("No replacement available for {}", taskId);
				taskSet.setRerollCount(taskSet.getRerollCount() - 1);
				return;
			}
			tasks.set(index, replacement.get(0));
			save();
			pushToPanel();
		});
	}

	@Override
	public void regenerate()
	{
		clientThread.invoke(() ->
		{
			if (taskSet == null || rerollsLeft() <= 0)
			{
				return;
			}
			// Keep finished tasks; replace the rest.
			List<DailyTask> kept = taskSet.getTasks().stream().filter(DailyTask::isDone).collect(Collectors.toList());
			int needed = Math.max(0, config.tasksPerDay() - kept.size());

			taskSet.setRerollCount(taskSet.getRerollCount() + 1);
			kept.addAll(generator.pick(snapshotBuilder.build(), needed, currentIds(), rerollRandom()));
			taskSet.setTasks(kept);
			save();
			pushToPanel();
		});
	}

	@Override
	public void openSettings()
	{
		// The core Config plugin opens a plugin's settings when an overlay's "Configure" entry is
		// clicked. Posting that event is the only supported way to open our own config page.
		eventBus.post(new OverlayMenuClicked(
			new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Configure", "Daily Task Generator"),
			settingsLink));
	}

	/**
	 * Never added to the overlay manager; it only identifies this plugin in {@link #openSettings()}.
	 */
	private static class SettingsLink extends Overlay
	{
		SettingsLink(Plugin plugin)
		{
			super(plugin);
		}

		@Override
		public Dimension render(Graphics2D graphics)
		{
			return null;
		}
	}

	private static String today()
	{
		return LocalDate.now(ZoneOffset.UTC).toString();
	}

	@Provides
	DailyTaskGeneratorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DailyTaskGeneratorConfig.class);
	}
}

package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.QuestRequirement;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.QuestState;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

@Singleton
public class QuestTaskProvider implements TaskProvider
{
	/**
	 * Quests already started are this many times more likely to be picked.
	 */
	static final double IN_PROGRESS_WEIGHT = 3;

	private final DailyTaskGeneratorConfig config;
	private volatile Map<Quest, QuestRequirement> requirements = Collections.emptyMap();

	@Inject
	QuestTaskProvider(DailyTaskGeneratorConfig config)
	{
		this.config = config;
	}

	public boolean hasRequirements()
	{
		return !requirements.isEmpty();
	}

	public void setRequirements(Map<Quest, QuestRequirement> requirements)
	{
		this.requirements = requirements;
	}

	@Override
	public TaskType getType()
	{
		return TaskType.QUEST;
	}

	@Override
	public boolean isEnabled()
	{
		return config.questTasks();
	}

	@Override
	public List<TaskCandidate> candidates(PlayerSnapshot snapshot)
	{
		List<TaskCandidate> out = new ArrayList<>();
		for (QuestRequirement req : requirements.values())
		{
			QuestState state = snapshot.getQuestState(req.getQuest());
			if (state == QuestState.FINISHED || !meets(snapshot, req))
			{
				continue;
			}
			boolean started = state == QuestState.IN_PROGRESS;
			out.add(new TaskCandidate(buildTask(req.getQuest(), started), started ? IN_PROGRESS_WEIGHT : 1));
		}
		return out;
	}

	static boolean meets(PlayerSnapshot snapshot, QuestRequirement req)
	{
		if (req.isMembers() && !snapshot.isMembers())
		{
			return false;
		}
		if (snapshot.getQuestPoints() < req.getQuestPoints() || snapshot.getCombatLevel() < req.getCombatLevel())
		{
			return false;
		}
		for (Map.Entry<Skill, Integer> e : req.getSkills().entrySet())
		{
			if (snapshot.getLevel(e.getKey()) < e.getValue())
			{
				return false;
			}
		}
		for (Quest q : req.getQuests())
		{
			if (!snapshot.isQuestFinished(q))
			{
				return false;
			}
		}
		return true;
	}

	static DailyTask buildTask(Quest quest, boolean started)
	{
		DailyTask task = new DailyTask();
		task.setId("quest:" + quest.name());
		task.setType(TaskType.QUEST);
		task.setQuest(quest);
		task.setWikiPage(quest.getName());
		task.setTitle((started ? "Continue " : "Complete ") + quest.getName());
		task.setDescription(started ? "Already started" : "You meet the listed requirements");
		return task;
	}
}

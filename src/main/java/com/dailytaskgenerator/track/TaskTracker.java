package com.dailytaskgenerator.track;

import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.DailyTaskSet;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.Map;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

public final class TaskTracker
{
	private TaskTracker()
	{
	}

	/**
	 * Brings every task up to date with the player's current state, e.g. after loading saved tasks.
	 *
	 * @return true if any task changed
	 */
	public static boolean sync(DailyTaskSet set, PlayerSnapshot snapshot)
	{
		boolean changed = false;
		for (DailyTask task : set.getTasks())
		{
			if (task.getType() == TaskType.SKILL && task.getSkill() != null)
			{
				changed |= onXp(set, task.getSkill(), snapshot.getXp(task.getSkill()));
			}
		}
		changed |= onQuestStates(set, snapshot.getQuestStates());
		return changed;
	}

	/**
	 * Marks quest tasks complete once the quest is finished.
	 *
	 * @return true if any task changed
	 */
	public static boolean onQuestStates(DailyTaskSet set, Map<Quest, QuestState> states)
	{
		boolean changed = false;
		for (DailyTask task : set.getTasks())
		{
			if (task.getType() == TaskType.QUEST && !task.isCompleted() && task.getQuest() != null
				&& states.get(task.getQuest()) == QuestState.FINISHED)
			{
				task.setCompleted(true);
				changed = true;
			}
		}
		return changed;
	}

	/**
	 * Updates skill-task progress for a new XP total.
	 *
	 * @return true if any task changed
	 */
	public static boolean onXp(DailyTaskSet set, Skill skill, int xp)
	{
		boolean changed = false;
		for (DailyTask task : set.getTasks())
		{
			if (task.getType() != TaskType.SKILL || task.getSkill() != skill)
			{
				continue;
			}
			// Guard against the stat being reported before it has loaded.
			int progress = Math.max(0, Math.min(xp - task.getBaselineXp(), task.getTargetXp()));
			if (progress != task.getProgressXp())
			{
				task.setProgressXp(progress);
				changed = true;
			}
			if (!task.isCompleted() && progress >= task.getTargetXp())
			{
				task.setCompleted(true);
				changed = true;
			}
		}
		return changed;
	}
}

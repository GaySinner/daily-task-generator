package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.CombatAchievement;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CombatAchievementTaskProvider implements TaskProvider
{
	private final DailyTaskGeneratorConfig config;
	private volatile List<CombatAchievement> achievements = Collections.emptyList();

	@Inject
	CombatAchievementTaskProvider(DailyTaskGeneratorConfig config)
	{
		this.config = config;
	}

	public boolean hasAchievements()
	{
		return !achievements.isEmpty();
	}

	public void setAchievements(List<CombatAchievement> achievements)
	{
		this.achievements = achievements;
	}

	@Override
	public TaskType getType()
	{
		return TaskType.COMBAT_ACHIEVEMENT;
	}

	@Override
	public boolean isEnabled()
	{
		return config.caTasks();
	}

	@Override
	public List<TaskCandidate> candidates(PlayerSnapshot snapshot)
	{
		// Almost every task needs members content, and the cache data doesn't say which don't.
		if (!snapshot.isMembers())
		{
			return Collections.emptyList();
		}
		int maxTier = config.caMaxTier().ordinal();
		List<TaskCandidate> out = new ArrayList<>();
		for (CombatAchievement ca : achievements)
		{
			int tier = ca.getTier().ordinal();
			if (tier > maxTier || snapshot.isCombatAchievementComplete(ca.getId()))
			{
				continue;
			}
			// Easier tiers are more likely: the easiest allowed tier gets the highest weight.
			out.add(new TaskCandidate(buildTask(ca), maxTier - tier + 1));
		}
		return out;
	}

	static DailyTask buildTask(CombatAchievement ca)
	{
		DailyTask task = new DailyTask();
		task.setId("ca:" + ca.getId());
		task.setType(TaskType.COMBAT_ACHIEVEMENT);
		task.setCaTaskId(ca.getId());
		task.setTitle(ca.getName());
		task.setWikiPage(ca.getMonster() != null ? ca.getMonster() : ca.getName());
		String desc = ca.getDescription() == null ? "" : ca.getDescription();
		task.setDescription(ca.getTier().getDisplayName() + " · " + desc);
		return task;
	}
}

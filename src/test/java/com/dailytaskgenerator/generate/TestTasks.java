package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.data.CombatAchievement;
import com.dailytaskgenerator.model.DailyTask;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * Builds tasks through the real providers, for tests in other packages.
 */
public final class TestTasks
{
	private TestTasks()
	{
	}

	public static DailyTask skill(Skill skill)
	{
		return SkillTaskProvider.buildTask(skill, 50, 101_333, SkillEffort.MEDIUM);
	}

	public static DailyTask quest(Quest quest)
	{
		return QuestTaskProvider.buildTask(quest, false);
	}

	public static DailyTask ca(CombatAchievement ca)
	{
		return CombatAchievementTaskProvider.buildTask(ca);
	}

	public static DailyTask diary(com.dailytaskgenerator.data.DiaryRegion region, com.dailytaskgenerator.data.DiaryTier tier)
	{
		return DiaryTaskProvider.buildTask(region, tier);
	}
}

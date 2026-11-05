package com.dailytaskgenerator;

import com.dailytaskgenerator.data.CombatAchievementTier;
import com.dailytaskgenerator.generate.SkillEffort;
import com.dailytaskgenerator.generate.SkillSelection;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(DailyTaskGeneratorConfig.GROUP)
public interface DailyTaskGeneratorConfig extends Config
{
	String GROUP = "daily-task-generator";

	@ConfigSection(
		name = "Skill tasks",
		description = "Settings for skill XP tasks",
		position = 10
	)
	String skillSection = "skillSection";

	@ConfigSection(
		name = "Quest tasks",
		description = "Settings for quest tasks",
		position = 20
	)
	String questSection = "questSection";

	@ConfigSection(
		name = "Combat achievement tasks",
		description = "Settings for combat achievement tasks",
		position = 30
	)
	String caSection = "caSection";

	@Range(min = 1, max = 10)
	@ConfigItem(
		keyName = "tasksPerDay",
		name = "Tasks per day",
		description = "How many tasks to generate each day",
		position = 1
	)
	default int tasksPerDay()
	{
		return 4;
	}

	@Range(max = 20)
	@ConfigItem(
		keyName = "rerollsPerDay",
		name = "Rerolls per day",
		description = "How many single-task rerolls or full regenerations are allowed each day",
		position = 2
	)
	default int rerollsPerDay()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "skillTasks",
		name = "Enable skill tasks",
		description = "Generate skill XP and level goals",
		section = skillSection,
		position = 11
	)
	default boolean skillTasks()
	{
		return true;
	}

	@ConfigItem(
		keyName = "skillEffort",
		name = "Effort",
		description = "How large skill XP goals should be",
		section = skillSection,
		position = 12
	)
	default SkillEffort skillEffort()
	{
		return SkillEffort.MEDIUM;
	}

	@ConfigItem(
		keyName = "skillSelection",
		name = "Skill choice",
		description = "How skills are picked for tasks",
		section = skillSection,
		position = 13
	)
	default SkillSelection skillSelection()
	{
		return SkillSelection.FAVOR_LOWEST;
	}

	@ConfigItem(
		keyName = "includeMaxedSkills",
		name = "Include level 99 skills",
		description = "Allow XP goals for skills that are already level 99",
		section = skillSection,
		position = 14
	)
	default boolean includeMaxedSkills()
	{
		return false;
	}

	@ConfigItem(
		keyName = "questTasks",
		name = "Enable quest tasks",
		description = "Suggest quests and miniquests you meet the requirements for",
		section = questSection,
		position = 21
	)
	default boolean questTasks()
	{
		return true;
	}

	@ConfigItem(
		keyName = "caTasks",
		name = "Enable combat achievements",
		description = "Suggest combat achievement tasks you haven't completed (members worlds only)",
		section = caSection,
		position = 31
	)
	default boolean caTasks()
	{
		return true;
	}

	@ConfigItem(
		keyName = "caMaxTier",
		name = "Hardest tier",
		description = "The hardest combat achievement tier to suggest tasks from",
		section = caSection,
		position = 32
	)
	default CombatAchievementTier caMaxTier()
	{
		return CombatAchievementTier.MEDIUM;
	}
}

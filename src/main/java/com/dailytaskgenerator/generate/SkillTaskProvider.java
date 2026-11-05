package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

@Singleton
public class SkillTaskProvider implements TaskProvider
{
	static final Set<Skill> FREE_TO_PLAY_SKILLS = EnumSet.of(
		Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS, Skill.RANGED, Skill.PRAYER,
		Skill.MAGIC, Skill.RUNECRAFT, Skill.CRAFTING, Skill.MINING, Skill.SMITHING, Skill.FISHING,
		Skill.COOKING, Skill.FIREMAKING, Skill.WOODCUTTING);

	private static final int MIN_XP_TARGET = 100;

	private final DailyTaskGeneratorConfig config;

	@Inject
	SkillTaskProvider(DailyTaskGeneratorConfig config)
	{
		this.config = config;
	}

	@Override
	public TaskType getType()
	{
		return TaskType.SKILL;
	}

	@Override
	public boolean isEnabled()
	{
		return config.skillTasks();
	}

	@Override
	public List<TaskCandidate> candidates(PlayerSnapshot snapshot)
	{
		List<TaskCandidate> out = new ArrayList<>();
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL)
			{
				continue;
			}
			if (!snapshot.isMembers() && !FREE_TO_PLAY_SKILLS.contains(skill))
			{
				continue;
			}

			int level = snapshot.getLevel(skill);
			int xp = snapshot.getXp(skill);
			if (xp >= Experience.MAX_SKILL_XP)
			{
				continue;
			}
			if (level >= Experience.MAX_REAL_LEVEL && !config.includeMaxedSkills())
			{
				continue;
			}

			out.add(new TaskCandidate(buildTask(skill, level, xp, config.skillEffort()), weight(level)));
		}
		return out;
	}

	private double weight(int level)
	{
		if (config.skillSelection() == SkillSelection.RANDOM)
		{
			return 1;
		}
		// Lower levels are more likely, but every skill stays possible.
		return (Experience.MAX_REAL_LEVEL + 1 - Math.min(level, Experience.MAX_REAL_LEVEL)) + 10;
	}

	static DailyTask buildTask(Skill skill, int level, int xp, SkillEffort effort)
	{
		int target = xpTarget(level, effort);
		target = Math.min(target, Experience.MAX_SKILL_XP - xp);

		DailyTask task = new DailyTask();
		task.setId("skill:" + skill.name());
		task.setType(TaskType.SKILL);
		task.setSkill(skill);
		task.setBaselineXp(xp);

		if (level < Experience.MAX_REAL_LEVEL)
		{
			int toNext = Experience.getXpForLevel(level + 1) - xp;
			if (toNext > 0 && toNext <= target)
			{
				task.setTargetXp(toNext);
				task.setTitle("Reach " + (level + 1) + " " + skill.getName());
				task.setDescription(format(toNext) + " " + skill.getName() + " XP to go");
				return task;
			}
		}

		task.setTargetXp(target);
		task.setTitle("Gain " + format(target) + " " + skill.getName() + " XP");
		task.setDescription("Current level " + level);
		return task;
	}

	static int xpTarget(int level, SkillEffort effort)
	{
		int span;
		if (level < Experience.MAX_REAL_LEVEL)
		{
			span = Experience.getXpForLevel(level + 1) - Experience.getXpForLevel(level);
		}
		else
		{
			span = effort.getMaxXp();
		}
		int raw = (int) Math.round(span * effort.getLevelSpanFactor());
		return round(Math.max(MIN_XP_TARGET, Math.min(raw, effort.getMaxXp())));
	}

	private static int round(int xp)
	{
		int step = xp < 1_000 ? 50 : xp < 10_000 ? 500 : 1_000;
		return Math.max(step, Math.round((float) xp / step) * step);
	}

	private static String format(int xp)
	{
		return NumberFormat.getIntegerInstance(Locale.US).format(xp);
	}
}

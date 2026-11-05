package com.dailytaskgenerator.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.model.DailyTask;
import java.util.List;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import org.junit.Test;

public class SkillTaskProviderTest
{
	static DailyTaskGeneratorConfig config(boolean includeMaxed)
	{
		return new DailyTaskGeneratorConfig()
		{
			@Override
			public boolean includeMaxedSkills()
			{
				return includeMaxed;
			}
		};
	}

	@Test
	public void lowLevelBecomesLevelUpGoal()
	{
		DailyTask t = SkillTaskProvider.buildTask(Skill.FISHING, 1, 0, SkillEffort.MEDIUM);
		assertEquals("Reach 2 Fishing", t.getTitle());
		assertEquals(Experience.getXpForLevel(2), t.getTargetXp());
	}

	@Test
	public void highLevelTargetIsCapped()
	{
		for (SkillEffort effort : SkillEffort.values())
		{
			int target = SkillTaskProvider.xpTarget(95, effort);
			assertTrue(target <= effort.getMaxXp());
			assertTrue(target > 0);
		}
	}

	@Test
	public void effortIncreasesTarget()
	{
		assertTrue(SkillTaskProvider.xpTarget(60, SkillEffort.SMALL) < SkillTaskProvider.xpTarget(60, SkillEffort.MEDIUM));
		assertTrue(SkillTaskProvider.xpTarget(60, SkillEffort.MEDIUM) < SkillTaskProvider.xpTarget(60, SkillEffort.LARGE));
	}

	@Test
	public void freeToPlayOnlyGetsFreeSkills()
	{
		List<TaskCandidate> c = new SkillTaskProvider(config(false)).candidates(TestSnapshots.allSkillsAt(40, false));
		assertEquals(SkillTaskProvider.FREE_TO_PLAY_SKILLS.size(), c.size());
		c.forEach(x -> assertTrue(SkillTaskProvider.FREE_TO_PLAY_SKILLS.contains(x.getTask().getSkill())));
	}

	@Test
	public void maxedSkillsExcludedByDefault()
	{
		assertTrue(new SkillTaskProvider(config(false)).candidates(TestSnapshots.allSkillsAt(99, true)).isEmpty());
		assertFalse(new SkillTaskProvider(config(true)).candidates(TestSnapshots.allSkillsAt(99, true)).isEmpty());
	}
}

package com.dailytaskgenerator.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.CombatAchievement;
import com.dailytaskgenerator.data.CombatAchievementTier;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class CombatAchievementTaskProviderTest
{
	private static final List<CombatAchievement> TASKS = List.of(
		new CombatAchievement(0, "Easy one", "Kill a thing", CombatAchievementTier.EASY, null),
		new CombatAchievement(33, "Easy two", "Kill another", CombatAchievementTier.EASY, null),
		new CombatAchievement(40, "Medium one", "Do it fast", CombatAchievementTier.MEDIUM, null),
		new CombatAchievement(70, "Hard one", "Do it flawlessly", CombatAchievementTier.HARD, null));

	private static CombatAchievementTaskProvider provider(CombatAchievementTier maxTier)
	{
		CombatAchievementTaskProvider p = new CombatAchievementTaskProvider(new DailyTaskGeneratorConfig()
		{
			@Override
			public CombatAchievementTier caMaxTier()
			{
				return maxTier;
			}
		});
		p.setAchievements(TASKS);
		return p;
	}

	private static PlayerSnapshot player(boolean members, int... varps)
	{
		return TestSnapshots.allSkillsAt(70, members).toBuilder().caCompletionVarps(varps).build();
	}

	private static List<Integer> ids(List<TaskCandidate> c)
	{
		return c.stream().map(x -> x.getTask().getCaTaskId()).sorted().collect(Collectors.toList());
	}

	@Test
	public void respectsMaxTier()
	{
		assertEquals(List.of(0, 33, 40), ids(provider(CombatAchievementTier.MEDIUM).candidates(player(true, 0, 0, 0))));
		assertEquals(List.of(0, 33), ids(provider(CombatAchievementTier.EASY).candidates(player(true, 0, 0, 0))));
	}

	@Test
	public void skipsCompletedTasks()
	{
		// Task 0 is bit 0 of varp 0; task 33 is bit 1 of varp 1.
		assertEquals(List.of(40, 70), ids(provider(CombatAchievementTier.HARD).candidates(player(true, 1, 1 << 1, 0))));
	}

	@Test
	public void noneOnFreeWorlds()
	{
		assertTrue(provider(CombatAchievementTier.HARD).candidates(player(false, 0, 0, 0)).isEmpty());
	}

	@Test
	public void easierTiersWeighMore()
	{
		List<TaskCandidate> c = provider(CombatAchievementTier.HARD).candidates(player(true, 0, 0, 0));
		double easy = c.stream().filter(x -> x.getTask().getCaTaskId() == 0).findFirst().get().getWeight();
		double hard = c.stream().filter(x -> x.getTask().getCaTaskId() == 70).findFirst().get().getWeight();
		assertTrue(easy > hard);
	}

	@Test
	public void bitLookup()
	{
		assertTrue(PlayerSnapshot.isBitSet(new int[]{0, 0x80000000}, 63));
		assertFalse(PlayerSnapshot.isBitSet(new int[]{0, 0x80000000}, 62));
		assertFalse(PlayerSnapshot.isBitSet(new int[]{-1}, 32));
		assertFalse(PlayerSnapshot.isBitSet(new int[]{-1}, -1));
	}
}

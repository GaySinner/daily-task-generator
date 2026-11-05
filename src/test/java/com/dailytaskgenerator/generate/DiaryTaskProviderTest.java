package com.dailytaskgenerator.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.DiaryRegion;
import com.dailytaskgenerator.data.DiaryTier;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.runelite.api.gameval.VarbitID;
import org.junit.Test;

public class DiaryTaskProviderTest
{
	private static DiaryTaskProvider provider(DiaryTier maxTier)
	{
		return new DiaryTaskProvider(new DailyTaskGeneratorConfig()
		{
			@Override
			public DiaryTier diaryMaxTier()
			{
				return maxTier;
			}
		});
	}

	private static PlayerSnapshot player(boolean members, Map<DiaryRegion, java.util.Set<DiaryTier>> done)
	{
		return TestSnapshots.allSkillsAt(70, members).toBuilder().completedDiaryTiers(done).build();
	}

	private static Optional<TaskCandidate> forRegion(List<TaskCandidate> c, DiaryRegion region)
	{
		return c.stream().filter(x -> x.getTask().getDiaryRegion() == region).findFirst();
	}

	@Test
	public void suggestsLowestUnfinishedTierPerRegion()
	{
		List<TaskCandidate> c = provider(DiaryTier.ELITE).candidates(player(true, Map.of(
			DiaryRegion.ARDOUGNE, EnumSet.of(DiaryTier.EASY, DiaryTier.MEDIUM))));
		assertEquals(DiaryRegion.values().length, c.size());
		assertEquals(DiaryTier.HARD, forRegion(c, DiaryRegion.ARDOUGNE).get().getTask().getDiaryTier());
		assertEquals(DiaryTier.EASY, forRegion(c, DiaryRegion.VARROCK).get().getTask().getDiaryTier());
	}

	@Test
	public void regionsPastMaxTierAreSkipped()
	{
		List<TaskCandidate> c = provider(DiaryTier.MEDIUM).candidates(player(true, Map.of(
			DiaryRegion.ARDOUGNE, EnumSet.of(DiaryTier.EASY, DiaryTier.MEDIUM))));
		assertFalse(forRegion(c, DiaryRegion.ARDOUGNE).isPresent());
		assertEquals(DiaryRegion.values().length - 1, c.size());
	}

	@Test
	public void noneOnFreeWorlds()
	{
		assertTrue(provider(DiaryTier.ELITE).candidates(player(false, Map.of())).isEmpty());
	}

	@Test
	public void karamjaUsesItsOwnVarbits()
	{
		assertEquals(VarbitID.ATJUN_MED_DONE, DiaryRegion.KARAMJA.getVarbit(DiaryTier.MEDIUM));
		assertTrue(DiaryRegion.isCompletionVarbit(VarbitID.ATJUN_EASY_DONE));
		assertTrue(DiaryRegion.isCompletionVarbit(VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE));
		assertFalse(DiaryRegion.isCompletionVarbit(VarbitID.WILDERNESS_DIARY_ANY_COMPLETE));
	}
}

package com.dailytaskgenerator.data;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import net.runelite.api.gameval.VarbitID;

/**
 * Achievement diary regions and the varbit that is set once each tier is complete.
 */
@Getter
public enum DiaryRegion
{
	ARDOUGNE("Ardougne", "Ardougne Diary",
		VarbitID.ARDOUGNE_DIARY_EASY_COMPLETE, VarbitID.ARDOUGNE_DIARY_MEDIUM_COMPLETE,
		VarbitID.ARDOUGNE_DIARY_HARD_COMPLETE, VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE),
	DESERT("Desert", "Desert Diary",
		VarbitID.DESERT_DIARY_EASY_COMPLETE, VarbitID.DESERT_DIARY_MEDIUM_COMPLETE,
		VarbitID.DESERT_DIARY_HARD_COMPLETE, VarbitID.DESERT_DIARY_ELITE_COMPLETE),
	FALADOR("Falador", "Falador Diary",
		VarbitID.FALADOR_DIARY_EASY_COMPLETE, VarbitID.FALADOR_DIARY_MEDIUM_COMPLETE,
		VarbitID.FALADOR_DIARY_HARD_COMPLETE, VarbitID.FALADOR_DIARY_ELITE_COMPLETE),
	FREMENNIK("Fremennik", "Fremennik Diary",
		VarbitID.FREMENNIK_DIARY_EASY_COMPLETE, VarbitID.FREMENNIK_DIARY_MEDIUM_COMPLETE,
		VarbitID.FREMENNIK_DIARY_HARD_COMPLETE, VarbitID.FREMENNIK_DIARY_ELITE_COMPLETE),
	KANDARIN("Kandarin", "Kandarin Diary",
		VarbitID.KANDARIN_DIARY_EASY_COMPLETE, VarbitID.KANDARIN_DIARY_MEDIUM_COMPLETE,
		VarbitID.KANDARIN_DIARY_HARD_COMPLETE, VarbitID.KANDARIN_DIARY_ELITE_COMPLETE),
	// Karamja's first three tiers predate the shared naming scheme.
	KARAMJA("Karamja", "Karamja Diary",
		VarbitID.ATJUN_EASY_DONE, VarbitID.ATJUN_MED_DONE,
		VarbitID.ATJUN_HARD_DONE, VarbitID.KARAMJA_DIARY_ELITE_COMPLETE),
	KOUREND("Kourend & Kebos", "Kourend & Kebos Diary",
		VarbitID.KOUREND_DIARY_EASY_COMPLETE, VarbitID.KOUREND_DIARY_MEDIUM_COMPLETE,
		VarbitID.KOUREND_DIARY_HARD_COMPLETE, VarbitID.KOUREND_DIARY_ELITE_COMPLETE),
	LUMBRIDGE("Lumbridge & Draynor", "Lumbridge & Draynor Diary",
		VarbitID.LUMBRIDGE_DIARY_EASY_COMPLETE, VarbitID.LUMBRIDGE_DIARY_MEDIUM_COMPLETE,
		VarbitID.LUMBRIDGE_DIARY_HARD_COMPLETE, VarbitID.LUMBRIDGE_DIARY_ELITE_COMPLETE),
	MORYTANIA("Morytania", "Morytania Diary",
		VarbitID.MORYTANIA_DIARY_EASY_COMPLETE, VarbitID.MORYTANIA_DIARY_MEDIUM_COMPLETE,
		VarbitID.MORYTANIA_DIARY_HARD_COMPLETE, VarbitID.MORYTANIA_DIARY_ELITE_COMPLETE),
	VARROCK("Varrock", "Varrock Diary",
		VarbitID.VARROCK_DIARY_EASY_COMPLETE, VarbitID.VARROCK_DIARY_MEDIUM_COMPLETE,
		VarbitID.VARROCK_DIARY_HARD_COMPLETE, VarbitID.VARROCK_DIARY_ELITE_COMPLETE),
	WESTERN("Western Provinces", "Western Provinces Diary",
		VarbitID.WESTERN_DIARY_EASY_COMPLETE, VarbitID.WESTERN_DIARY_MEDIUM_COMPLETE,
		VarbitID.WESTERN_DIARY_HARD_COMPLETE, VarbitID.WESTERN_DIARY_ELITE_COMPLETE),
	WILDERNESS("Wilderness", "Wilderness Diary",
		VarbitID.WILDERNESS_DIARY_EASY_COMPLETE, VarbitID.WILDERNESS_DIARY_MEDIUM_COMPLETE,
		VarbitID.WILDERNESS_DIARY_HARD_COMPLETE, VarbitID.WILDERNESS_DIARY_ELITE_COMPLETE);

	private final String displayName;
	private final String wikiPage;
	/**
	 * Completion varbits indexed by {@link DiaryTier#ordinal()}.
	 */
	private final int[] tierVarbits;

	DiaryRegion(String displayName, String wikiPage, int... tierVarbits)
	{
		this.displayName = displayName;
		this.wikiPage = wikiPage;
		this.tierVarbits = tierVarbits;
	}

	public int getVarbit(DiaryTier tier)
	{
		return tierVarbits[tier.ordinal()];
	}

	public static boolean isCompletionVarbit(int varbitId)
	{
		return CompletionVarbits.ALL.contains(varbitId);
	}

	/**
	 * Built on first use; enum constants can't be read from their own static initializers.
	 */
	private static final class CompletionVarbits
	{
		static final Set<Integer> ALL = Arrays.stream(values())
			.flatMapToInt(r -> Arrays.stream(r.tierVarbits))
			.boxed()
			.collect(Collectors.toUnmodifiableSet());
	}
}

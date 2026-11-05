package com.dailytaskgenerator.player;

import com.dailytaskgenerator.data.DiaryRegion;
import com.dailytaskgenerator.data.DiaryTier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Value;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

/**
 * Immutable view of the player's state, so generators and tests never touch {@code Client}.
 */
@Value
@Builder(toBuilder = true)
public class PlayerSnapshot
{
	long accountHash;
	boolean members;
	int combatLevel;
	int questPoints;
	@Builder.Default
	Map<Skill, Integer> levels = Collections.emptyMap();
	@Builder.Default
	Map<Skill, Integer> experience = Collections.emptyMap();
	@Builder.Default
	Map<Quest, QuestState> questStates = Collections.emptyMap();
	/**
	 * Values of the CA_TASK_COMPLETED varps, in order.
	 */
	@Builder.Default
	int[] caCompletionVarps = new int[0];
	/**
	 * Completed diary tiers; regions with none completed may be absent.
	 */
	@Builder.Default
	Map<DiaryRegion, Set<DiaryTier>> completedDiaryTiers = Collections.emptyMap();

	public int getLevel(Skill skill)
	{
		return levels.getOrDefault(skill, 1);
	}

	public int getXp(Skill skill)
	{
		return experience.getOrDefault(skill, 0);
	}

	public QuestState getQuestState(Quest quest)
	{
		return questStates.getOrDefault(quest, QuestState.NOT_STARTED);
	}

	public boolean isQuestFinished(Quest quest)
	{
		return getQuestState(quest) == QuestState.FINISHED;
	}

	public boolean isDiaryTierComplete(DiaryRegion region, DiaryTier tier)
	{
		Set<DiaryTier> done = completedDiaryTiers.get(region);
		return done != null && done.contains(tier);
	}

	public boolean isCombatAchievementComplete(int taskId)
	{
		return isBitSet(caCompletionVarps, taskId);
	}

	public static boolean isBitSet(int[] varps, int taskId)
	{
		int index = taskId / 32;
		return taskId >= 0 && index < varps.length && (varps[index] & (1 << (taskId % 32))) != 0;
	}

	public static Map<Skill, Integer> emptySkillMap()
	{
		return new EnumMap<>(Skill.class);
	}
}

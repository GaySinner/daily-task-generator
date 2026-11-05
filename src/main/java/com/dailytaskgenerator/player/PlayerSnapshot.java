package com.dailytaskgenerator.player;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
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

	public static Map<Skill, Integer> emptySkillMap()
	{
		return new EnumMap<>(Skill.class);
	}
}

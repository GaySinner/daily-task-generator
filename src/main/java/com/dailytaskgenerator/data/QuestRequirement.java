package com.dailytaskgenerator.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * A quest's requirements as recorded in the game cache. Non-stat requirements such as items or
 * kudos aren't in the table and so aren't checked.
 */
@Value
@Builder
public class QuestRequirement
{
	Quest quest;
	boolean members;
	@Builder.Default
	Map<Skill, Integer> skills = Collections.emptyMap();
	@Builder.Default
	List<Quest> quests = Collections.emptyList();
	int questPoints;
	int combatLevel;
}

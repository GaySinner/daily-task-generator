package com.dailytaskgenerator.player;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import net.runelite.api.Skill;

/**
 * Immutable view of the player's state, so generators and tests never touch {@code Client}.
 */
@Value
@Builder
public class PlayerSnapshot
{
	long accountHash;
	boolean members;
	int combatLevel;
	@Builder.Default
	Map<Skill, Integer> levels = Collections.emptyMap();
	@Builder.Default
	Map<Skill, Integer> experience = Collections.emptyMap();

	public int getLevel(Skill skill)
	{
		return levels.getOrDefault(skill, 1);
	}

	public int getXp(Skill skill)
	{
		return experience.getOrDefault(skill, 0);
	}

	public static Map<Skill, Integer> emptySkillMap()
	{
		return new EnumMap<>(Skill.class);
	}
}

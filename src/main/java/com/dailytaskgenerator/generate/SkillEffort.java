package com.dailytaskgenerator.generate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillEffort
{
	SMALL("Small", 0.5, 25_000),
	MEDIUM("Medium", 1.0, 75_000),
	LARGE("Large", 2.0, 200_000);

	private final String displayName;
	/**
	 * Fraction of the XP span of the player's current level.
	 */
	private final double levelSpanFactor;
	/**
	 * Upper bound on the XP target, so high-level goals stay achievable in a day.
	 */
	private final int maxXp;

	@Override
	public String toString()
	{
		return displayName;
	}
}

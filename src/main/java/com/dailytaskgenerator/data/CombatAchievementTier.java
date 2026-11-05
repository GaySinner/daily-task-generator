package com.dailytaskgenerator.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CombatAchievementTier
{
	EASY("Easy"),
	MEDIUM("Medium"),
	HARD("Hard"),
	ELITE("Elite"),
	MASTER("Master"),
	GRANDMASTER("Grandmaster");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}

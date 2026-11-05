package com.dailytaskgenerator.generate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SkillSelection
{
	FAVOR_LOWEST("Favor lowest skills"),
	RANDOM("Random");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}

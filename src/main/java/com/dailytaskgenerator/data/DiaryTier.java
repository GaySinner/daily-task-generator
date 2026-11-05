package com.dailytaskgenerator.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiaryTier
{
	EASY("Easy"),
	MEDIUM("Medium"),
	HARD("Hard"),
	ELITE("Elite");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}

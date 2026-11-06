package com.dailytaskgenerator.model;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskType
{
	SKILL("Skill", new Color(0x4CAF50)),
	QUEST("Quest", new Color(0x2196F3)),
	DIARY("Diary", new Color(0xFFC107)),
	COMBAT_ACHIEVEMENT("Combat", new Color(0xF44336)),
	CUSTOM("Custom", new Color(0x9C27B0));

	private final String displayName;
	private final Color color;
}

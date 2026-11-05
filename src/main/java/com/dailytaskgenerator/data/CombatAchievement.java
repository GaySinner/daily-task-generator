package com.dailytaskgenerator.data;

import javax.annotation.Nullable;
import lombok.Value;

@Value
public class CombatAchievement
{
	/**
	 * The task's id; also its bit index across the CA_TASK_COMPLETED varps.
	 */
	int id;
	String name;
	String description;
	CombatAchievementTier tier;
	/**
	 * The monster the task is for, or null for general tasks or when the name isn't known.
	 */
	@Nullable
	String monster;
}

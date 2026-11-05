package com.dailytaskgenerator.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

/**
 * A single daily task. Mutable so it can be (de)serialized and updated as progress is made;
 * instances handed to the UI are always copies.
 */
@Data
@NoArgsConstructor
public class DailyTask
{
	/**
	 * Stable identifier, unique within a day, e.g. "skill:FISHING".
	 */
	private String id;
	private TaskType type;
	private String title;
	private String description;

	// Skill tasks
	private Skill skill;
	private int baselineXp;
	private int targetXp;
	private int progressXp;

	// Quest tasks
	private Quest quest;

	/**
	 * Completed because the game state showed it.
	 */
	private boolean completed;
	/**
	 * Ticked off by the user.
	 */
	private boolean manuallyCompleted;

	public boolean isDone()
	{
		return completed || manuallyCompleted;
	}

	public boolean hasProgress()
	{
		return type == TaskType.SKILL && targetXp > 0;
	}

	public DailyTask copy()
	{
		DailyTask t = new DailyTask();
		t.id = id;
		t.type = type;
		t.title = title;
		t.description = description;
		t.skill = skill;
		t.baselineXp = baselineXp;
		t.targetXp = targetXp;
		t.progressXp = progressXp;
		t.quest = quest;
		t.completed = completed;
		t.manuallyCompleted = manuallyCompleted;
		return t;
	}
}

package com.dailytaskgenerator.model;

import com.dailytaskgenerator.data.DiaryRegion;
import com.dailytaskgenerator.data.DiaryTier;
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
	/**
	 * OSRS Wiki page or search term for this task; null on tasks saved before links existed.
	 */
	private String wikiPage;

	// Skill tasks
	private Skill skill;
	private int baselineXp;
	private int targetXp;
	private int progressXp;

	// Quest tasks
	private Quest quest;

	// Combat achievement tasks; boxed because 0 is a valid task id.
	private Integer caTaskId;

	// Achievement diary tasks
	private DiaryRegion diaryRegion;
	private DiaryTier diaryTier;

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
		t.wikiPage = wikiPage;
		t.skill = skill;
		t.baselineXp = baselineXp;
		t.targetXp = targetXp;
		t.progressXp = progressXp;
		t.quest = quest;
		t.caTaskId = caTaskId;
		t.diaryRegion = diaryRegion;
		t.diaryTier = diaryTier;
		t.completed = completed;
		t.manuallyCompleted = manuallyCompleted;
		return t;
	}
}

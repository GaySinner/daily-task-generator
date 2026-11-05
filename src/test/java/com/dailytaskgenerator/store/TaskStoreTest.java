package com.dailytaskgenerator.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.DailyTaskSet;
import com.dailytaskgenerator.model.Streak;
import com.dailytaskgenerator.model.TaskType;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Skill;
import org.junit.Test;

public class TaskStoreTest
{
	// Plugin code uses the injected Gson; a plain one is equivalent for these types.
	private final Gson gson = new Gson();

	private static DailyTaskSet sample()
	{
		DailyTask t = new DailyTask();
		t.setId("skill:AGILITY");
		t.setType(TaskType.SKILL);
		t.setSkill(Skill.AGILITY);
		t.setTitle("Gain 5,000 Agility XP");
		t.setBaselineXp(12345);
		t.setTargetXp(5000);
		t.setManuallyCompleted(true);
		DailyTaskSet s = new DailyTaskSet();
		s.setDate("2026-11-05");
		s.setRerollCount(2);
		s.setTasks(new ArrayList<>(List.of(t)));
		return s;
	}

	@Test
	public void roundTrips()
	{
		DailyTaskSet loaded = TaskStore.parse(gson, gson.toJson(sample()));
		assertEquals(sample(), loaded);
	}

	@Test
	public void missingOrCorruptDataIsIgnored()
	{
		assertNull(TaskStore.parse(gson, null));
		assertNull(TaskStore.parse(gson, ""));
		assertNull(TaskStore.parse(gson, "{not json"));
		assertNull(TaskStore.parse(gson, "{\"rerollCount\":1}"));
	}

	@Test
	public void unknownTypesAndSkillsAreDropped()
	{
		String json = "{\"date\":\"2026-11-05\",\"tasks\":["
			+ "{\"id\":\"a\",\"type\":\"SOMETHING_NEW\"},"
			+ "{\"id\":\"b\",\"type\":\"SKILL\",\"skill\":\"NOT_A_SKILL\"},"
			+ "{\"id\":\"c\",\"type\":\"QUEST\",\"quest\":\"NOT_A_QUEST\"},"
			+ "{\"id\":\"d\",\"type\":\"COMBAT_ACHIEVEMENT\"},"
			+ "{\"id\":\"skill:MINING\",\"type\":\"SKILL\",\"skill\":\"MINING\",\"targetXp\":100}]}";
		DailyTaskSet set = TaskStore.parse(gson, json);
		assertEquals(1, set.getTasks().size());
		assertEquals("skill:MINING", set.getTasks().get(0).getId());
	}

	@Test
	public void nullTaskListBecomesEmpty()
	{
		DailyTaskSet set = TaskStore.parse(gson, "{\"date\":\"2026-11-05\",\"tasks\":null}");
		assertTrue(set.getTasks().isEmpty());
	}

	@Test
	public void streakRoundTripsAndToleratesBadData()
	{
		Streak streak = new Streak(4, 9, "2026-11-05");
		assertEquals(streak, TaskStore.parseStreak(gson, gson.toJson(streak)));
		assertEquals(new Streak(), TaskStore.parseStreak(gson, null));
		assertEquals(new Streak(), TaskStore.parseStreak(gson, "{bad"));
		assertEquals(new Streak(), TaskStore.parseStreak(gson, "{\"current\":2,\"lastDate\":\"yesterday\"}"));
	}
}

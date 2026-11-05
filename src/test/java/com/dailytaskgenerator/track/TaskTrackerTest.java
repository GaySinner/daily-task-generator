package com.dailytaskgenerator.track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.DailyTaskSet;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.Map;
import java.util.List;
import net.runelite.api.Skill;
import org.junit.Test;

public class TaskTrackerTest
{
	private static DailyTaskSet set()
	{
		DailyTask t = new DailyTask();
		t.setId("skill:MINING");
		t.setType(TaskType.SKILL);
		t.setSkill(Skill.MINING);
		t.setBaselineXp(1000);
		t.setTargetXp(500);
		DailyTaskSet s = new DailyTaskSet();
		s.setTasks(new java.util.ArrayList<>(List.of(t)));
		return s;
	}

	@Test
	public void tracksProgressAndCompletes()
	{
		DailyTaskSet s = set();
		assertTrue(TaskTracker.onXp(s, Skill.MINING, 1200));
		assertEquals(200, s.getTasks().get(0).getProgressXp());
		assertFalse(s.getTasks().get(0).isDone());

		assertTrue(TaskTracker.onXp(s, Skill.MINING, 1600));
		assertEquals(500, s.getTasks().get(0).getProgressXp());
		assertTrue(s.getTasks().get(0).isCompleted());
	}

	@Test
	public void ignoresOtherSkillsAndUnloadedStats()
	{
		DailyTaskSet s = set();
		assertFalse(TaskTracker.onXp(s, Skill.FISHING, 5000));
		assertFalse(TaskTracker.onXp(s, Skill.MINING, 0));
		assertEquals(0, s.getTasks().get(0).getProgressXp());
	}

	@Test
	public void syncRecomputesProgressFromSnapshot()
	{
		DailyTaskSet s = set();
		Map<Skill, Integer> xp = PlayerSnapshot.emptySkillMap();
		xp.put(Skill.MINING, 1700);
		PlayerSnapshot snap = PlayerSnapshot.builder().experience(xp).build();
		assertTrue(TaskTracker.sync(s, snap));
		assertEquals(500, s.getTasks().get(0).getProgressXp());
		assertTrue(s.getTasks().get(0).isCompleted());
		assertFalse(TaskTracker.sync(s, snap));
	}
}

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
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
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

	@Test
	public void questCompletesWhenFinished()
	{
		DailyTask q = new DailyTask();
		q.setId("quest:DORICS_QUEST");
		q.setType(TaskType.QUEST);
		q.setQuest(Quest.DORICS_QUEST);
		DailyTaskSet s = new DailyTaskSet();
		s.setTasks(new java.util.ArrayList<>(List.of(q)));

		assertFalse(TaskTracker.onQuestStates(s, Map.of(Quest.DORICS_QUEST, QuestState.IN_PROGRESS)));
		assertTrue(TaskTracker.onQuestStates(s, Map.of(Quest.DORICS_QUEST, QuestState.FINISHED)));
		assertTrue(q.isCompleted());
	}

	@Test
	public void combatAchievementCompletesWhenBitSet()
	{
		DailyTask ca = new DailyTask();
		ca.setId("ca:35");
		ca.setType(TaskType.COMBAT_ACHIEVEMENT);
		ca.setCaTaskId(35);
		DailyTaskSet s = new DailyTaskSet();
		s.setTasks(new java.util.ArrayList<>(List.of(ca)));

		assertFalse(TaskTracker.onCombatAchievementVarps(s, new int[]{-1, 0}));
		assertTrue(TaskTracker.onCombatAchievementVarps(s, new int[]{0, 1 << 3}));
		assertTrue(ca.isCompleted());
	}

	@Test
	public void diaryCompletesWhenTierDone()
	{
		DailyTask d = new DailyTask();
		d.setId("diary:FALADOR:EASY");
		d.setType(TaskType.DIARY);
		d.setDiaryRegion(com.dailytaskgenerator.data.DiaryRegion.FALADOR);
		d.setDiaryTier(com.dailytaskgenerator.data.DiaryTier.EASY);
		DailyTaskSet s = new DailyTaskSet();
		s.setTasks(new java.util.ArrayList<>(List.of(d)));

		assertFalse(TaskTracker.onDiaryTiers(s, Map.of()));
		assertTrue(TaskTracker.onDiaryTiers(s, Map.of(com.dailytaskgenerator.data.DiaryRegion.FALADOR,
			java.util.EnumSet.of(com.dailytaskgenerator.data.DiaryTier.EASY))));
		assertTrue(d.isCompleted());
	}
}

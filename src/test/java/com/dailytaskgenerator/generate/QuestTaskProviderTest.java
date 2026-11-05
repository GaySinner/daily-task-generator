package com.dailytaskgenerator.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.QuestRequirement;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.Test;

public class QuestTaskProviderTest
{
	private static PlayerSnapshot player(int level, boolean members, int qp, Map<Quest, QuestState> states)
	{
		PlayerSnapshot base = TestSnapshots.allSkillsAt(level, members);
		return base.toBuilder().questPoints(qp).combatLevel(level).questStates(states).build();
	}

	private static QuestRequirement req(Quest quest, boolean members, Map<Skill, Integer> skills, List<Quest> quests, int qp)
	{
		return QuestRequirement.builder().quest(quest).members(members).skills(skills).quests(quests).questPoints(qp).build();
	}

	private static QuestTaskProvider provider(QuestRequirement... reqs)
	{
		QuestTaskProvider p = new QuestTaskProvider(new DailyTaskGeneratorConfig()
		{
		});
		Map<Quest, QuestRequirement> m = new EnumMap<>(Quest.class);
		for (QuestRequirement r : reqs)
		{
			m.put(r.getQuest(), r);
		}
		p.setRequirements(m);
		return p;
	}

	@Test
	public void skillRequirement()
	{
		QuestRequirement r = req(Quest.DRAGON_SLAYER_I, false, Map.of(Skill.CRAFTING, 30), List.of(), 0);
		assertFalse(QuestTaskProvider.meets(player(29, true, 0, Map.of()), r));
		assertTrue(QuestTaskProvider.meets(player(30, true, 0, Map.of()), r));
	}

	@Test
	public void questPointAndPrereqRequirements()
	{
		QuestRequirement r = req(Quest.DRAGON_SLAYER_I, false, Map.of(), List.of(Quest.COOKS_ASSISTANT), 32);
		assertFalse(QuestTaskProvider.meets(player(50, true, 40, Map.of()), r));
		assertFalse(QuestTaskProvider.meets(player(50, true, 31, Map.of(Quest.COOKS_ASSISTANT, QuestState.FINISHED)), r));
		assertTrue(QuestTaskProvider.meets(player(50, true, 32, Map.of(Quest.COOKS_ASSISTANT, QuestState.FINISHED)), r));
	}

	@Test
	public void membersQuestsHiddenOnFreeWorlds()
	{
		QuestRequirement r = req(Quest.BIOHAZARD, true, Map.of(), List.of(), 0);
		assertFalse(QuestTaskProvider.meets(player(50, false, 0, Map.of()), r));
		assertTrue(QuestTaskProvider.meets(player(50, true, 0, Map.of()), r));
	}

	@Test
	public void candidatesSkipFinishedAndFavorStarted()
	{
		QuestTaskProvider p = provider(
			req(Quest.COOKS_ASSISTANT, false, Map.of(), List.of(), 0),
			req(Quest.DORICS_QUEST, false, Map.of(), List.of(), 0),
			req(Quest.DEMON_SLAYER, false, Map.of(), List.of(), 0));
		PlayerSnapshot s = player(10, false, 0, Map.of(
			Quest.COOKS_ASSISTANT, QuestState.FINISHED,
			Quest.DORICS_QUEST, QuestState.IN_PROGRESS));

		List<TaskCandidate> c = p.candidates(s);
		assertEquals(2, c.size());
		TaskCandidate doric = c.stream().filter(x -> x.getTask().getQuest() == Quest.DORICS_QUEST).findFirst().get();
		assertEquals(QuestTaskProvider.IN_PROGRESS_WEIGHT, doric.getWeight(), 0);
		assertTrue(doric.getTask().getTitle().startsWith("Continue"));
	}
}

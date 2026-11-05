package com.dailytaskgenerator.ui;

import static org.junit.Assert.assertEquals;
import com.dailytaskgenerator.data.CombatAchievement;
import com.dailytaskgenerator.data.CombatAchievementTier;
import com.dailytaskgenerator.generate.TestTasks;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.TaskType;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import org.junit.Test;

public class WikiLinksTest
{
	@Test
	public void skillLinksDirectlyToPage()
	{
		assertEquals("https://oldschool.runescape.wiki/w/Herblore?utm_source=runelite",
			WikiLinks.url(TestTasks.skill(Skill.HERBLORE)));
	}

	@Test
	public void questGoesThroughSearchAndIsEncoded()
	{
		assertEquals("https://oldschool.runescape.wiki/w/Special:Search?search=Cook%27s%20Assistant&utm_source=runelite",
			WikiLinks.url(TestTasks.quest(Quest.COOKS_ASSISTANT)));
	}

	@Test
	public void combatAchievementUsesMonsterThenTaskName()
	{
		DailyTask withMonster = TestTasks.ca(new CombatAchievement(1, "Abyssal Adept", "Kill the Abyssal Sire 20 times.",
			CombatAchievementTier.HARD, "Abyssal Sire"));
		assertEquals("https://oldschool.runescape.wiki/w/Special:Search?search=Abyssal%20Sire&utm_source=runelite",
			WikiLinks.url(withMonster));

		DailyTask general = TestTasks.ca(new CombatAchievement(399, "Sit Back and Relax", "Deal 100 damage.",
			CombatAchievementTier.MEDIUM, null));
		assertEquals("https://oldschool.runescape.wiki/w/Special:Search?search=Sit%20Back%20and%20Relax&utm_source=runelite",
			WikiLinks.url(general));
	}

	@Test
	public void tasksSavedBeforeLinksStillGetOne()
	{
		DailyTask old = new DailyTask();
		old.setType(TaskType.QUEST);
		old.setQuest(Quest.DORICS_QUEST);
		assertEquals("https://oldschool.runescape.wiki/w/Special:Search?search=Doric%27s%20Quest&utm_source=runelite",
			WikiLinks.url(old));
	}
}

package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.Map;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

final class TestSnapshots
{
	private TestSnapshots()
	{
	}

	static PlayerSnapshot allSkillsAt(int level, boolean members)
	{
		Map<Skill, Integer> levels = PlayerSnapshot.emptySkillMap();
		Map<Skill, Integer> xp = PlayerSnapshot.emptySkillMap();
		for (Skill s : Skill.values())
		{
			if (s != Skill.OVERALL)
			{
				levels.put(s, level);
				xp.put(s, Experience.getXpForLevel(level));
			}
		}
		return PlayerSnapshot.builder().accountHash(42L).members(members).combatLevel(3).levels(levels).experience(xp).build();
	}
}

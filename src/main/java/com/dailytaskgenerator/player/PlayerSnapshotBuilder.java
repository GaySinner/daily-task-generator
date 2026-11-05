package com.dailytaskgenerator.player;

import com.dailytaskgenerator.data.CombatAchievementLoader;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.VarPlayerID;

@Singleton
public class PlayerSnapshotBuilder
{
	private final Client client;

	@Inject
	PlayerSnapshotBuilder(Client client)
	{
		this.client = client;
	}

	/**
	 * Client thread only.
	 */
	public int[] readCaVarps()
	{
		int[] varps = new int[CombatAchievementLoader.COMPLETION_VARPS.length];
		for (int i = 0; i < varps.length; i++)
		{
			varps[i] = client.getVarpValue(CombatAchievementLoader.COMPLETION_VARPS[i]);
		}
		return varps;
	}

	/**
	 * Must be called on the client thread while logged in.
	 */
	public PlayerSnapshot build()
	{
		Map<Skill, Integer> levels = PlayerSnapshot.emptySkillMap();
		Map<Skill, Integer> xp = PlayerSnapshot.emptySkillMap();
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL)
			{
				continue;
			}
			levels.put(skill, client.getRealSkillLevel(skill));
			xp.put(skill, client.getSkillExperience(skill));
		}

		// Each getState call runs a client script, so this is only done when a snapshot is built
		// (login, generate, reroll), never per tick.
		Map<Quest, QuestState> quests = new EnumMap<>(Quest.class);
		for (Quest quest : Quest.values())
		{
			quests.put(quest, quest.getState(client));
		}

		Player local = client.getLocalPlayer();
		return PlayerSnapshot.builder()
			.accountHash(client.getAccountHash())
			.members(client.getWorldType().contains(WorldType.MEMBERS))
			.combatLevel(local != null ? local.getCombatLevel() : 3)
			.questPoints(client.getVarpValue(VarPlayerID.QP))
			.questStates(quests)
			.caCompletionVarps(readCaVarps())
			.levels(levels)
			.experience(xp)
			.build();
	}
}

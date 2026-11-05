package com.dailytaskgenerator.player;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.WorldType;

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

		Player local = client.getLocalPlayer();
		return PlayerSnapshot.builder()
			.accountHash(client.getAccountHash())
			.members(client.getWorldType().contains(WorldType.MEMBERS))
			.combatLevel(local != null ? local.getCombatLevel() : 3)
			.levels(levels)
			.experience(xp)
			.build();
	}
}

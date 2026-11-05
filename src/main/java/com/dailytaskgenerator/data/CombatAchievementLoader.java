package com.dailytaskgenerator.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.StructComposition;
import net.runelite.api.gameval.VarPlayerID;

/**
 * Reads the combat achievement task list from the game cache.
 */
@Slf4j
@Singleton
public class CombatAchievementLoader
{
	// gameval has no EnumID/ParamID, so these cache ids are named here.
	/**
	 * One enum per tier, Easy through Grandmaster, whose values are the tasks' struct ids.
	 */
	private static final int[] TIER_TASK_ENUMS = {3981, 3982, 3983, 3984, 3985, 3986};
	private static final int PARAM_TASK_ID = 1306;
	private static final int PARAM_TASK_NAME = 1308;
	private static final int PARAM_TASK_DESCRIPTION = 1309;
	private static final int PARAM_TASK_MONSTER = 1312;
	/**
	 * Maps a task's monster param to the monster's display name.
	 */
	private static final int MONSTER_NAME_ENUM = 3970;

	/**
	 * Completion bitfields, 32 tasks each; task id N is bit N % 32 of varp N / 32.
	 */
	public static final int[] COMPLETION_VARPS = {
		VarPlayerID.CA_TASK_COMPLETED_0, VarPlayerID.CA_TASK_COMPLETED_1, VarPlayerID.CA_TASK_COMPLETED_2,
		VarPlayerID.CA_TASK_COMPLETED_3, VarPlayerID.CA_TASK_COMPLETED_4, VarPlayerID.CA_TASK_COMPLETED_5,
		VarPlayerID.CA_TASK_COMPLETED_6, VarPlayerID.CA_TASK_COMPLETED_7, VarPlayerID.CA_TASK_COMPLETED_8,
		VarPlayerID.CA_TASK_COMPLETED_9, VarPlayerID.CA_TASK_COMPLETED_10, VarPlayerID.CA_TASK_COMPLETED_11,
		VarPlayerID.CA_TASK_COMPLETED_12, VarPlayerID.CA_TASK_COMPLETED_13, VarPlayerID.CA_TASK_COMPLETED_14,
		VarPlayerID.CA_TASK_COMPLETED_15, VarPlayerID.CA_TASK_COMPLETED_16, VarPlayerID.CA_TASK_COMPLETED_17,
		VarPlayerID.CA_TASK_COMPLETED_18, VarPlayerID.CA_TASK_COMPLETED_19, VarPlayerID.CA_TASK_COMPLETED_20,
	};

	private final Client client;

	private Map<Integer, String> monsterNames()
	{
		Map<Integer, String> names = new HashMap<>();
		EnumComposition e = client.getEnum(MONSTER_NAME_ENUM);
		if (e == null)
		{
			return names;
		}
		int[] keys = e.getKeys();
		String[] values = e.getStringVals();
		for (int i = 0; i < keys.length && i < values.length; i++)
		{
			String n = values[i];
			// "Other" covers tasks that aren't tied to one monster; those have no page to link to.
			if (n != null && !n.isEmpty() && !n.equalsIgnoreCase("Other"))
			{
				names.put(keys[i], n);
			}
		}
		return names;
	}

	@Inject
	CombatAchievementLoader(Client client)
	{
		this.client = client;
	}

	/**
	 * Must be called on the client thread while logged in. Returns an empty list if the cache data
	 * doesn't look like the task list, so a cache change disables CA tasks instead of producing
	 * nonsense ones.
	 */
	public List<CombatAchievement> load()
	{
		List<CombatAchievement> out = new ArrayList<>();
		int beyondKnownVarps = 0;
		Map<Integer, String> monsterNames = monsterNames();
		CombatAchievementTier[] tiers = CombatAchievementTier.values();
		for (int t = 0; t < TIER_TASK_ENUMS.length; t++)
		{
			EnumComposition tierEnum = client.getEnum(TIER_TASK_ENUMS[t]);
			if (tierEnum == null)
			{
				continue;
			}
			for (int structId : tierEnum.getIntVals())
			{
				StructComposition struct = client.getStructComposition(structId);
				if (struct == null)
				{
					continue;
				}
				int id = struct.getIntValue(PARAM_TASK_ID);
				String name = struct.getStringValue(PARAM_TASK_NAME);
				String description = struct.getStringValue(PARAM_TASK_DESCRIPTION);
				if (id < 0 || name == null || name.isEmpty())
				{
					continue;
				}
				if (id >= COMPLETION_VARPS.length * 32)
				{
					// Newer than our varp list, so completion can't be checked; leave it out.
					beyondKnownVarps++;
					continue;
				}
				out.add(new CombatAchievement(id, name, description, tiers[t],
					monsterNames.get(struct.getIntValue(PARAM_TASK_MONSTER))));
			}
		}

		if (out.size() < 100)
		{
			log.debug("Combat achievement data looks wrong ({} tasks read); disabling CA tasks", out.size());
			return Collections.emptyList();
		}
		if (log.isDebugEnabled())
		{
			for (CombatAchievementTier tier : tiers)
			{
				List<CombatAchievement> inTier = new ArrayList<>();
				out.stream().filter(a -> a.getTier() == tier).forEach(inTier::add);
				log.debug("Combat achievements {}: {} tasks, e.g. {}", tier, inTier.size(),
					inTier.subList(0, Math.min(3, inTier.size())));
			}
		}
		log.debug("Loaded {} combat achievements ({} skipped: id beyond known completion varps, {} with a monster name)",
			out.size(), beyondKnownVarps, out.stream().filter(a -> a.getMonster() != null).count());
		return out;
	}
}

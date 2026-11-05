package com.dailytaskgenerator.data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.gameval.DBTableID;

/**
 * Reads quest requirements from the game cache's quest table, so they stay current with game
 * updates. {@link Quest#getId()} is the quest's row id in that table.
 */
@Slf4j
@Singleton
public class QuestRequirementLoader
{
	private final Client client;

	@Inject
	QuestRequirementLoader(Client client)
	{
		this.client = client;
	}

	/**
	 * Must be called on the client thread while logged in. Quests whose row can't be read are left
	 * out rather than guessed at.
	 */
	public Map<Quest, QuestRequirement> load()
	{
		Map<Integer, Quest> byRow = new HashMap<>();
		for (Quest q : Quest.values())
		{
			byRow.put(q.getId(), q);
		}

		Map<Quest, QuestRequirement> out = new EnumMap<>(Quest.class);
		for (Quest quest : Quest.values())
		{
			try
			{
				int row = quest.getId();
				if (truthy(first(row, DBTableID.Quest.COL_HAS_SUBQUESTS)))
				{
					// e.g. Recipe for Disaster: its subquests are suggested instead.
					continue;
				}

				Map<Skill, Integer> skills = new EnumMap<>(Skill.class);
				Object[] statIds = field(row, DBTableID.Quest.COL_REQUIREMENT_STATS, 0);
				Object[] statLevels = statIds.length > 0 ? field(row, DBTableID.Quest.COL_REQUIREMENT_STATS, 1) : statIds;
				for (int i = 0; i < statIds.length && i < statLevels.length; i++)
				{
					Skill skill = skill(asInt(statIds[i]));
					if (skill != null)
					{
						skills.merge(skill, asInt(statLevels[i]), Math::max);
					}
				}

				List<Quest> prereqs = new ArrayList<>();
				for (Object o : field(row, DBTableID.Quest.COL_REQUIREMENT_QUESTS, 0))
				{
					Quest q = byRow.get(asInt(o));
					if (q != null)
					{
						prereqs.add(q);
					}
				}

				out.put(quest, QuestRequirement.builder()
					.quest(quest)
					.members(truthy(first(row, DBTableID.Quest.COL_MEMBERS)))
					.skills(skills)
					.quests(prereqs)
					.questPoints(asInt(first(row, DBTableID.Quest.COL_REQUIREMENT_QUESTPOINTS)))
					.combatLevel(asInt(first(row, DBTableID.Quest.COL_REQUIREMENT_COMBAT)))
					.build());
			}
			catch (RuntimeException e)
			{
				log.debug("Couldn't read requirements for {}", quest, e);
			}
		}
		if (log.isDebugEnabled())
		{
			out.values().forEach(r -> log.debug("Quest requirement: {}", r));
		}
		log.debug("Loaded requirements for {} of {} quests", out.size(), Quest.values().length);
		return out;
	}

	private Object[] field(int row, int column, int tupleIndex)
	{
		Object[] values = client.getDBTableField(row, column, tupleIndex);
		return values == null ? new Object[0] : values;
	}

	private Object first(int row, int column)
	{
		Object[] values = field(row, column, 0);
		return values.length > 0 ? values[0] : null;
	}

	private static Skill skill(int statId)
	{
		// Skill's declaration order matches the game's stat ids; OVERALL is last and has no id.
		Skill[] skills = Skill.values();
		return statId >= 0 && statId < skills.length && skills[statId] != Skill.OVERALL ? skills[statId] : null;
	}

	private static int asInt(Object o)
	{
		if (o instanceof Number)
		{
			return ((Number) o).intValue();
		}
		if (o instanceof Boolean)
		{
			return (Boolean) o ? 1 : 0;
		}
		return 0;
	}

	private static boolean truthy(Object o)
	{
		return asInt(o) != 0;
	}
}

package com.dailytaskgenerator.store;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.model.DailyTaskSet;
import com.dailytaskgenerator.model.TaskType;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Saves the day's tasks in the RuneScape-profile config, so each account (and world type,
 * e.g. seasonal) keeps its own list across relogs and client restarts.
 */
@Slf4j
@Singleton
public class TaskStore
{
	static final String TASK_SET_KEY = "taskSet";

	private final ConfigManager configManager;
	private final Gson gson;

	@Inject
	TaskStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	/**
	 * True once the logged-in account's profile is known; until then reads and writes would hit
	 * no profile.
	 */
	public boolean isProfileReady()
	{
		return configManager.getRSProfileKey() != null;
	}

	@Nullable
	public DailyTaskSet load()
	{
		String json = configManager.getRSProfileConfiguration(DailyTaskGeneratorConfig.GROUP, TASK_SET_KEY);
		return parse(gson, json);
	}

	public void save(DailyTaskSet set)
	{
		configManager.setRSProfileConfiguration(DailyTaskGeneratorConfig.GROUP, TASK_SET_KEY, gson.toJson(set));
	}

	@Nullable
	static DailyTaskSet parse(Gson gson, @Nullable String json)
	{
		if (json == null || json.isEmpty())
		{
			return null;
		}
		DailyTaskSet set;
		try
		{
			set = gson.fromJson(json, DailyTaskSet.class);
		}
		catch (JsonParseException e)
		{
			log.debug("Discarding unreadable saved tasks", e);
			return null;
		}
		if (set == null || set.getDate() == null)
		{
			return null;
		}
		// Drop tasks from types or skills a later version no longer knows about.
		set.setTasks(set.getTasks() == null ? new ArrayList<>() : set.getTasks().stream()
			.filter(t -> t != null && t.getId() != null && t.getType() != null)
			.filter(t -> t.getType() != TaskType.SKILL || t.getSkill() != null)
			.filter(t -> t.getType() != TaskType.QUEST || t.getQuest() != null)
			.filter(t -> t.getType() != TaskType.COMBAT_ACHIEVEMENT || t.getCaTaskId() != null)
			.filter(t -> t.getType() != TaskType.DIARY || (t.getDiaryRegion() != null && t.getDiaryTier() != null))
			.collect(Collectors.toCollection(ArrayList::new)));
		return set;
	}
}

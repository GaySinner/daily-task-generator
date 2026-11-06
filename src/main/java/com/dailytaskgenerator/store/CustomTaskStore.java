package com.dailytaskgenerator.store;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.CustomTask;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Keeps the imported custom tasks in the client config, shared by all accounts. The imported file
 * itself is only read once, at import time.
 */
@Slf4j
@Singleton
public class CustomTaskStore
{
	static final String KEY = "customTaskList";

	private final ConfigManager configManager;
	private final Gson gson;

	@Inject
	CustomTaskStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	public List<CustomTask> load()
	{
		String json = configManager.getConfiguration(DailyTaskGeneratorConfig.GROUP, KEY);
		if (json == null || json.isEmpty())
		{
			return Collections.emptyList();
		}
		try
		{
			List<CustomTask> tasks = gson.fromJson(json, new TypeToken<List<CustomTask>>()
			{
			}.getType());
			return tasks == null ? Collections.emptyList() : tasks;
		}
		catch (JsonParseException e)
		{
			log.debug("Could not read saved custom tasks", e);
			return Collections.emptyList();
		}
	}

	public void save(List<CustomTask> tasks)
	{
		configManager.setConfiguration(DailyTaskGeneratorConfig.GROUP, KEY, gson.toJson(tasks));
	}
}

package com.dailytaskgenerator;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Daily Task Generator",
	description = "Generates daily tasks tailored to your character",
	tags = {"daily", "tasks", "goals", "quests", "diary", "combat achievements"}
)
public class DailyTaskGeneratorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private DailyTaskGeneratorConfig config;

	@Override
	protected void startUp()
	{
		log.debug("Daily Task Generator started");
	}

	@Override
	protected void shutDown()
	{
		log.debug("Daily Task Generator stopped");
	}

	@Provides
	DailyTaskGeneratorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DailyTaskGeneratorConfig.class);
	}
}

package com.dailytaskgenerator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;

@ConfigGroup(DailyTaskGeneratorConfig.GROUP)
public interface DailyTaskGeneratorConfig extends Config
{
	String GROUP = "daily-task-generator";
}

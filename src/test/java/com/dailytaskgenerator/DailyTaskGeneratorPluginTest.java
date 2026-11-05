package com.dailytaskgenerator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DailyTaskGeneratorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DailyTaskGeneratorPlugin.class);
		RuneLite.main(args);
	}
}

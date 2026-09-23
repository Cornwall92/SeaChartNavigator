package com.jamescornwell.seachartnavigator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Launches a development RuneLite client with this plugin loaded. */
public class SeaChartNavigatorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SeaChartNavigatorPlugin.class);
		RuneLite.main(args);
	}
}

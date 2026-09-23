package com.jamescornwell.seachartnavigator;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;

@ConfigGroup(SeaChartNavigatorConfig.GROUP)
public interface SeaChartNavigatorConfig extends Config
{
	String GROUP = "seaChartNavigator";

	@ConfigItem(
		keyName = "navigatorEnabled",
		name = "Enable navigator",
		description = "Show the nearest eligible sea charting task."
	)
	default boolean navigatorEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeCompleted",
		name = "Include completed tasks",
		description = "Allow already-charted tasks to be selected as the target."
	)
	default boolean includeCompleted()
	{
		return false;
	}

	@ConfigItem(
		keyName = "useBoostedSailingLevel",
		name = "Use boosted Sailing level",
		description = "Use the temporarily boosted Sailing level instead of the real level."
	)
	default boolean useBoostedSailingLevel()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showWorldMapPin",
		name = "Show world-map pin",
		description = "Show the selected target on the world map."
	)
	default boolean showWorldMapPin()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useNativeHintArrow",
		name = "Use native hint arrow",
		description = "Also use RuneScape's hint arrow. This can conflict with other content or plugins."
	)
	default boolean useNativeHintArrow()
	{
		return false;
	}

	@Range(min = 1, max = 64)
	@ConfigItem(
		keyName = "arrivalRadius",
		name = "Arrival radius",
		description = "Tile distance at which an arrival notification can be sent."
	)
	default int arrivalRadius()
	{
		return 8;
	}

	@Range(min = 0, max = 64)
	@ConfigItem(
		keyName = "switchHysteresis",
		name = "Target switch tolerance",
		description = "Keep the current target until another target is this many tiles closer."
	)
	default int switchHysteresis()
	{
		return 12;
	}

	@ConfigItem(
		keyName = "targetNotifications",
		name = "New target notification",
		description = "Notification sent when the selected target changes."
	)
	default Notification targetNotifications()
	{
		return Notification.OFF;
	}

	@ConfigItem(
		keyName = "arrivalNotifications",
		name = "Arrival notification",
		description = "Notification sent once when the player reaches the selected target."
	)
	default Notification arrivalNotifications()
	{
		return Notification.OFF;
	}

	@ConfigItem(
		keyName = "arrowColor",
		name = "Arrow colour",
		description = "Colour of the directional arrow."
	)
	default Color arrowColor()
	{
		return new Color(78, 219, 178);
	}

	@ConfigItem(
		keyName = "backgroundColor",
		name = "Overlay background",
		description = "Background colour of the navigation overlay."
	)
	default Color backgroundColor()
	{
		return new Color(0, 0, 0, 190);
	}
}


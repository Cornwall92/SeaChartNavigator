package com.jamescornwell.seachartnavigator;

import com.google.inject.Provides;
import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.service.ChartingTaskRepository;
import com.jamescornwell.seachartnavigator.service.NavigationState;
import com.jamescornwell.seachartnavigator.service.SailingState;
import com.jamescornwell.seachartnavigator.service.TargetSelector;
import com.jamescornwell.seachartnavigator.ui.SeaChartNavigationOverlay;
import com.jamescornwell.seachartnavigator.ui.TargetMapPoint;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;

@PluginDescriptor(
	name = "Sea Chart Navigator",
	description = "Points to the nearest eligible sea charting task.",
	tags = {"sailing", "charting", "navigation", "sea"}
)
public class SeaChartNavigatorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private SeaChartNavigatorConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SeaChartNavigationOverlay navigationOverlay;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Inject
	private ChartingTaskRepository taskRepository;

	@Inject
	private TargetSelector targetSelector;

	@Inject
	private NavigationState navigationState;

	@Inject
	private Notifier notifier;

	private TargetMapPoint mapPoint;
	private boolean nativeHintArrowSet;
	private boolean arrivalNotificationSent;

	@Provides
	SeaChartNavigatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SeaChartNavigatorConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(navigationOverlay);
		recalculateTarget(false, true);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(navigationOverlay);
		clearNavigation();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		recalculateTarget(true, false);
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() == Skill.SAILING)
		{
			recalculateTarget(true, false);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (taskRepository.isCompletionVarbit(event.getVarbitId()))
		{
			recalculateTarget(true, false);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			recalculateTarget(false, true);
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			clearNavigation();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (SeaChartNavigatorConfig.GROUP.equals(event.getGroup()))
		{
			recalculateTarget(false, true);
		}
	}

	private void recalculateTarget(boolean notifyOnChange, boolean forceDecorationRefresh)
	{
		if (!config.navigatorEnabled() || client.getGameState() != GameState.LOGGED_IN)
		{
			clearNavigation();
			return;
		}

		WorldPoint playerLocation = SailingState.getTopLevelWorldPoint(client);
		if (playerLocation == null)
		{
			clearNavigation();
			return;
		}

		int sailingLevel = config.useBoostedSailingLevel()
			? client.getBoostedSkillLevel(Skill.SAILING)
			: client.getRealSkillLevel(Skill.SAILING);

		ChartingTask previousTarget = navigationState.getTarget();
		ChartingTask selectedTarget = targetSelector.selectClosest(
			taskRepository.getTasks(),
			playerLocation,
			task -> task.hasSailingLevel(sailingLevel) && (config.includeCompleted() || !task.isComplete(client))
		);

		boolean targetChanged = previousTarget != selectedTarget;
		if (selectedTarget == null)
		{
			clearNavigation();
			return;
		}

		int distance = targetSelector.distance(playerLocation, selectedTarget.getLocation());
		navigationState.setTarget(selectedTarget);
		updateDecorations(selectedTarget, targetChanged || forceDecorationRefresh);

		if (targetChanged)
		{
			arrivalNotificationSent = false;
			if (notifyOnChange && config.targetNotifications().isEnabled())
			{
				notifier.notify(config.targetNotifications(), "Sea Chart Navigator: " + selectedTarget.getTitle());
			}
		}

		if (!arrivalNotificationSent && distance <= config.arrivalRadius())
		{
			arrivalNotificationSent = true;
			if (config.arrivalNotifications().isEnabled())
			{
				notifier.notify(config.arrivalNotifications(), "Sea Chart Navigator: You have reached " + selectedTarget.getTitle());
			}
		}
	}

	private void updateDecorations(ChartingTask target, boolean forceRefresh)
	{
		if (config.showWorldMapPin())
		{
			if (forceRefresh || mapPoint == null || mapPoint.getTaskId() != target.getId())
			{
				removeMapPoint();
				mapPoint = new TargetMapPoint(target);
				worldMapPointManager.add(mapPoint);
			}
		}
		else
		{
			removeMapPoint();
		}

		if (config.useNativeHintArrow())
		{
			if (forceRefresh || !nativeHintArrowSet)
			{
				client.setHintArrow(target.getLocation());
				nativeHintArrowSet = true;
			}
		}
		else
		{
			clearNativeHintArrow();
		}
	}

	private void clearNavigation()
	{
		navigationState.clear();
		arrivalNotificationSent = false;
		removeMapPoint();
		clearNativeHintArrow();
	}

	private void removeMapPoint()
	{
		if (mapPoint != null)
		{
			worldMapPointManager.remove(mapPoint);
			mapPoint = null;
		}
	}

	private void clearNativeHintArrow()
	{
		if (nativeHintArrowSet)
		{
			client.clearHintArrow();
			nativeHintArrowSet = false;
		}
	}
}

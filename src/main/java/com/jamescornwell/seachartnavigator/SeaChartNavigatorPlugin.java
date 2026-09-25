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
import net.runelite.api.HintArrowType;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
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
	private ClientThread clientThread;

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
	private WorldPoint nativeHintArrowLocation;
	private boolean arrivalNotificationSent;
	// A new identity on every enable prevents callbacks from an old session
	// from changing navigation after disable or a quick disable/re-enable.
	private volatile Object activeSession;

	@Provides
	SeaChartNavigatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SeaChartNavigatorConfig.class);
	}

	@Override
	protected void startUp()
	{
		Object session = new Object();
		activeSession = session;
		overlayManager.add(navigationOverlay);
		// RuneLite may call startUp from its Swing settings thread. Reading the
		// world view is only permitted from the game client thread.
		clientThread.invokeLater(() ->
		{
			if (activeSession == session)
			{
				clearNavigation();
				recalculateTarget(false, true);
			}
		});
	}

	@Override
	protected void shutDown()
	{
		activeSession = null;
		overlayManager.remove(navigationOverlay);
		clientThread.invoke(() ->
		{
			if (activeSession == null)
			{
				clearNavigation();
			}
		});
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		runOnClientThread(() -> recalculateTarget(true, false));
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() == Skill.SAILING)
		{
			runOnClientThread(() -> recalculateTarget(true, false));
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (taskRepository.isCompletionVarbit(event.getVarbitId()))
		{
			runOnClientThread(() -> recalculateTarget(true, false));
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		runOnClientThread(() ->
		{
			if (event.getGameState() == GameState.LOGGED_IN)
			{
				recalculateTarget(false, true);
			}
			else
			{
				clearNavigation();
			}
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (SeaChartNavigatorConfig.GROUP.equals(event.getGroup()))
		{
			runOnClientThread(() -> recalculateTarget(false, true));
		}
	}

	private void runOnClientThread(Runnable action)
	{
		Object session = activeSession;
		if (session == null)
		{
			return;
		}
		clientThread.invoke(() ->
		{
			if (activeSession == session)
			{
				action.run();
			}
		});
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
			// RuneScape has one shared hint arrow. Yield if game content or
			// another plugin has replaced ours, and resume once it is free.
			if (client.getHintArrowType() != HintArrowType.NONE && !isOurNativeHintArrow())
			{
				nativeHintArrowLocation = null;
				return;
			}
			if (!target.getLocation().equals(nativeHintArrowLocation)
				|| client.getHintArrowType() == HintArrowType.NONE)
			{
				client.setHintArrow(target.getLocation());
				nativeHintArrowLocation = target.getLocation();
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
		if (isOurNativeHintArrow())
		{
			client.clearHintArrow();
		}
		nativeHintArrowLocation = null;
	}

	private boolean isOurNativeHintArrow()
	{
		if (nativeHintArrowLocation == null || client.getHintArrowType() != HintArrowType.COORDINATE)
		{
			return false;
		}
		WorldPoint current = client.getHintArrowPoint();
		// Coordinate hint arrows store x/y, not a target plane.
		return current != null && current.getX() == nativeHintArrowLocation.getX()
			&& current.getY() == nativeHintArrowLocation.getY();
	}
}

package com.jamescornwell.seachartnavigator;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.model.ChartingTaskType;
import com.jamescornwell.seachartnavigator.service.ChartingTaskRepository;
import com.jamescornwell.seachartnavigator.service.NavigationState;
import com.jamescornwell.seachartnavigator.service.TargetSelector;
import com.jamescornwell.seachartnavigator.ui.SeaChartNavigationOverlay;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.HintArrowType;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.Notification;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class SeaChartNavigatorLifecycleTest
{
	@Mock private Client client;
	@Mock private ClientThread clientThread;
	@Mock private SeaChartNavigatorConfig config;
	@Mock private OverlayManager overlayManager;
	@Mock private SeaChartNavigationOverlay navigationOverlay;
	@Mock private WorldMapPointManager worldMapPointManager;
	@Mock private ChartingTaskRepository taskRepository;
	@Mock private Notifier notifier;
	@Mock private Player player;
	@Mock private WorldView worldView;
	@Spy private NavigationState navigationState = new NavigationState();
	@Spy private TargetSelector targetSelector = new TargetSelector();
	@InjectMocks private SeaChartNavigatorPlugin plugin;

	private final Queue<Runnable> callbacks = new ArrayDeque<>();
	private final ChartingTask target = new ChartingTask(1, "Test task", ChartingTaskType.GENERIC,
		18574, new WorldPoint(105, 100, 0), 1);
	private AutoCloseable mocks;
	private boolean onClientThread;
	private GameState gameState = GameState.LOGGED_IN;
	private int hintType = HintArrowType.NONE;
	private WorldPoint hintPoint;

	@Before
	public void setUp()
	{
		mocks = MockitoAnnotations.openMocks(this);
		when(config.navigatorEnabled()).thenReturn(true);
		when(config.showWorldMapPin()).thenReturn(true);
		when(config.useNativeHintArrow()).thenReturn(true);
		when(config.arrivalRadius()).thenReturn(8);
		when(config.targetNotifications()).thenReturn(Notification.OFF);
		when(config.arrivalNotifications()).thenReturn(Notification.OFF);
		when(taskRepository.getTasks()).thenReturn(Collections.singletonList(target));
		when(client.getGameState()).thenAnswer(invocation -> gameState);
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getTopLevelWorldView()).thenReturn(worldView);
		when(client.getWorldView(anyInt())).thenReturn(worldView);
		when(client.getRealSkillLevel(Skill.SAILING)).thenReturn(99);
		when(player.getLocalLocation()).thenReturn(new LocalPoint(64, 64, WorldView.TOPLEVEL));
		when(player.getWorldView()).thenAnswer(invocation ->
		{
			requireClientThread();
			return worldView;
		});
		when(worldView.isTopLevel()).thenReturn(true);
		when(worldView.getBaseX()).thenReturn(100);
		when(worldView.getBaseY()).thenReturn(100);
		when(client.getHintArrowType()).thenAnswer(invocation -> hintType);
		when(client.getHintArrowPoint()).thenAnswer(invocation -> hintPoint);
		doAnswer(invocation ->
		{
			requireClientThread();
			hintType = HintArrowType.COORDINATE;
			hintPoint = invocation.getArgument(0);
			return null;
		}).when(client).setHintArrow(any(WorldPoint.class));
		doAnswer(invocation ->
		{
			requireClientThread();
			hintType = HintArrowType.NONE;
			hintPoint = null;
			return null;
		}).when(client).clearHintArrow();
		doAnswer(invocation ->
		{
			Runnable action = invocation.getArgument(0);
			if (onClientThread)
			{
				action.run();
			}
			else
			{
				callbacks.add(action);
			}
			return null;
		}).when(clientThread).invoke(any(Runnable.class));
		doAnswer(invocation ->
		{
			callbacks.add(invocation.getArgument(0));
			return null;
		}).when(clientThread).invokeLater(any(Runnable.class));
	}

	@After
	public void tearDown() throws Exception
	{
		mocks.close();
	}

	@Test
	public void settingsChangedOnSwingThreadAreDeferred() throws Exception
	{
		enable();
		clearInvocations(player);
		SwingUtilities.invokeAndWait(() -> plugin.onConfigChanged(configChanged()));
		verifyNoInteractions(player);
		drainCallbacks();
		assertSame(target, navigationState.getTarget());
		verify(player).getWorldView();
	}

	@Test
	public void shutdownOnSwingThreadClearsArrowOnClientThread() throws Exception
	{
		enable();
		SwingUtilities.invokeAndWait(plugin::shutDown);
		assertEquals(target.getLocation(), hintPoint);
		drainCallbacks();
		assertNull(navigationState.getTarget());
		assertEquals(HintArrowType.NONE, hintType);
	}

	@Test
	public void pendingStartupCannotRecreateNavigationAfterDisable()
	{
		plugin.startUp();
		plugin.shutDown();
		drainCallbacks();
		assertNull(navigationState.getTarget());
		verify(client, never()).setHintArrow(any(WorldPoint.class));
		verify(worldMapPointManager, never()).add(any());
	}

	@Test
	public void callbacksFromOldSessionCannotAffectQuickReenable()
	{
		plugin.startUp();
		plugin.onConfigChanged(configChanged());
		plugin.shutDown();
		plugin.startUp();
		drainCallbacks();
		assertSame(target, navigationState.getTarget());
		verify(client, times(1)).setHintArrow(target.getLocation());
		verify(worldMapPointManager, times(1)).add(any());
	}

	@Test
	public void queuedConfigurationChangeIsDiscardedAfterDisable()
	{
		enable();
		clearInvocations(player);
		plugin.onConfigChanged(configChanged());
		plugin.shutDown();
		drainCallbacks();
		verifyNoInteractions(player);
		assertNull(navigationState.getTarget());
	}

	@Test
	public void existingNpcArrowIsPreservedAndNavigationResumesWhenItClears()
	{
		hintType = HintArrowType.NPC;
		enable();
		assertSame(target, navigationState.getTarget());
		assertEquals(HintArrowType.NPC, hintType);
		verify(client, never()).setHintArrow(any(WorldPoint.class));
		hintType = HintArrowType.NONE;
		tick();
		assertEquals(target.getLocation(), hintPoint);
	}

	@Test
	public void shutdownPreservesAReplacementCoordinateArrow()
	{
		enable();
		WorldPoint otherTarget = new WorldPoint(200, 200, 0);
		hintPoint = otherTarget;
		plugin.shutDown();
		drainCallbacks();
		assertEquals(otherTarget, hintPoint);
		verify(client, never()).clearHintArrow();
	}

	@Test
	public void settingsRefreshDoesNotOverwriteAnotherPluginsArrow()
	{
		enable();
		hintType = HintArrowType.NPC;
		hintPoint = null;
		plugin.onConfigChanged(configChanged());
		drainCallbacks();
		assertEquals(HintArrowType.NPC, hintType);
		plugin.shutDown();
		drainCallbacks();
		assertEquals(HintArrowType.NPC, hintType);
		verify(client, never()).clearHintArrow();
	}

	@Test
	public void loadingAndDisconnectClearStaleNavigation()
	{
		enable();
		for (GameState state : new GameState[]{GameState.LOADING, GameState.CONNECTION_LOST,
			GameState.HOPPING, GameState.LOGIN_SCREEN})
		{
			changeGameState(state);
			assertNull(navigationState.getTarget());
			assertEquals(HintArrowType.NONE, hintType);
			changeGameState(GameState.LOGGED_IN);
			assertSame(target, navigationState.getTarget());
		}
	}

	@Test
	public void disablingNavigatorSettingRemovesNavigation()
	{
		enable();
		when(config.navigatorEnabled()).thenReturn(false);
		plugin.onConfigChanged(configChanged());
		drainCallbacks();
		assertNull(navigationState.getTarget());
		assertEquals(HintArrowType.NONE, hintType);
		verify(worldMapPointManager).remove(any());
	}

	@Test
	public void completedTargetIsRemovedOnTheNextTick()
	{
		enable();
		when(client.getVarbitValue(18574)).thenReturn(1);
		tick();
		assertNull(navigationState.getTarget());
		assertEquals(HintArrowType.NONE, hintType);
	}

	@Test
	public void unrelatedSettingsDoNotScheduleNavigationWork()
	{
		enable();
		ConfigChanged event = configChanged();
		event.setGroup("anotherPlugin");
		plugin.onConfigChanged(event);
		assertTrue(callbacks.isEmpty());
	}

	private void enable()
	{
		plugin.startUp();
		drainCallbacks();
	}

	private void tick()
	{
		onClientThread = true;
		try
		{
			plugin.onGameTick(new GameTick());
		}
		finally
		{
			onClientThread = false;
		}
		drainCallbacks();
	}

	private void changeGameState(GameState state)
	{
		gameState = state;
		GameStateChanged event = new GameStateChanged();
		event.setGameState(state);
		plugin.onGameStateChanged(event);
		drainCallbacks();
	}

	private ConfigChanged configChanged()
	{
		ConfigChanged event = new ConfigChanged();
		event.setGroup(SeaChartNavigatorConfig.GROUP);
		event.setKey("hudVisibility");
		return event;
	}

	private void drainCallbacks()
	{
		onClientThread = true;
		try
		{
			while (!callbacks.isEmpty())
			{
				callbacks.remove().run();
			}
		}
		finally
		{
			onClientThread = false;
		}
	}

	private void requireClientThread()
	{
		if (!onClientThread)
		{
			throw new IllegalStateException("must be called on client thread");
		}
	}
}

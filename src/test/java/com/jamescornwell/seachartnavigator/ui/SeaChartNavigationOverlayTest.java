package com.jamescornwell.seachartnavigator.ui;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

import com.jamescornwell.seachartnavigator.HudVisibility;
import com.jamescornwell.seachartnavigator.SeaChartNavigatorConfig;
import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.model.ChartingTaskType;
import com.jamescornwell.seachartnavigator.service.NavigationState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

public class SeaChartNavigationOverlayTest
{
	@Test
	public void doesNotReadPlayerWhileLoadingEvenWithAnOldTarget()
	{
		Client client = mock(Client.class);
		when(client.getGameState()).thenReturn(GameState.LOADING);
		NavigationState state = new NavigationState();
		state.setTarget(new ChartingTask(1, "Test", ChartingTaskType.GENERIC, 1, new WorldPoint(100, 100, 0), 1));
		assertNoPlayerRead(client, state);
	}

	@Test
	public void doesNotReadPlayerWhenThereIsNoTarget()
	{
		Client client = mock(Client.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		assertNoPlayerRead(client, new NavigationState());
	}

	@Test
	public void exactLocationShowsNeutralMarkerWithoutReadingCamera()
	{
		Client client = clientAt(100, 100);
		SeaChartNavigationOverlay overlay = overlay(client, new WorldPoint(100, 100, 0));
		BufferedImage north = renderImage(overlay, "At location");
		when(client.getCameraYaw()).thenReturn(4096);
		BufferedImage west = renderImage(overlay, "At location");

		assertArrayEquals(pixels(north), pixels(west));
		verify(client, never()).getCameraYaw();
	}

	@Test
	public void nearbyTargetStillTracksCameraOnEveryRender()
	{
		Client client = clientAt(100, 100);
		SeaChartNavigationOverlay overlay = overlay(client, new WorldPoint(100, 105, 0));
		BufferedImage north = renderImage(overlay, "5 tiles away");
		when(client.getCameraYaw()).thenReturn(4096);
		BufferedImage west = renderImage(overlay, "5 tiles away");

		assertFalse(Arrays.equals(pixels(north), pixels(west)));
		verify(client, times(2)).getCameraYaw();
	}

	@Test
	public void leavingTargetRestoresDirectionAndLiveDistance()
	{
		Client client = clientAt(100, 100);
		SeaChartNavigationOverlay overlay = overlay(client, new WorldPoint(100, 100, 0));
		renderImage(overlay, "At location");
		when(client.getLocalPlayer().getLocalLocation()).thenReturn(new LocalPoint(64, 192, WorldView.TOPLEVEL));
		renderImage(overlay, "1 tile away");
		verify(client, times(1)).getCameraYaw();
	}

	private Client clientAt(int x, int y)
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		WorldView worldView = mock(WorldView.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getTopLevelWorldView()).thenReturn(worldView);
		when(player.getWorldView()).thenReturn(worldView);
		when(player.getLocalLocation()).thenReturn(new LocalPoint(64, 64, WorldView.TOPLEVEL));
		when(worldView.isTopLevel()).thenReturn(true);
		when(worldView.getBaseX()).thenReturn(x);
		when(worldView.getBaseY()).thenReturn(y);
		return client;
	}

	private SeaChartNavigationOverlay overlay(Client client, WorldPoint target)
	{
		NavigationState state = new NavigationState();
		state.setTarget(new ChartingTask(1, "Test", ChartingTaskType.GENERIC, 1, target, 1));
		return new SeaChartNavigationOverlay(client, alwaysVisibleConfig(), state);
	}

	private BufferedImage renderImage(SeaChartNavigationOverlay overlay, String expectedDistance)
	{
		BufferedImage image = new BufferedImage(500, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = mock(Graphics2D.class, delegatesTo(image.createGraphics()));
		try
		{
			assertNotNull(overlay.render(graphics));
			verify(graphics).drawString(eq(expectedDistance), anyInt(), anyInt());
			return image;
		}
		finally
		{
			graphics.dispose();
		}
	}

	private int[] pixels(BufferedImage image)
	{
		return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
	}

	private SeaChartNavigatorConfig alwaysVisibleConfig()
	{
		return new SeaChartNavigatorConfig()
		{
			@Override
			public HudVisibility hudVisibility()
			{
				return HudVisibility.ALWAYS;
			}
		};
	}

	private void assertNoPlayerRead(Client client, NavigationState state)
	{
		SeaChartNavigationOverlay overlay = new SeaChartNavigationOverlay(client, alwaysVisibleConfig(), state);
		Graphics2D graphics = new BufferedImage(500, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			assertNull(overlay.render(graphics));
			verify(client, never()).getLocalPlayer();
		}
		finally
		{
			graphics.dispose();
		}
	}
}

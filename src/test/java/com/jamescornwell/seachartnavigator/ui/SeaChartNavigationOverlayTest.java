package com.jamescornwell.seachartnavigator.ui;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import com.jamescornwell.seachartnavigator.HudVisibility;
import com.jamescornwell.seachartnavigator.SeaChartNavigatorConfig;
import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.model.ChartingTaskType;
import com.jamescornwell.seachartnavigator.service.NavigationState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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

	private void assertNoPlayerRead(Client client, NavigationState state)
	{
		SeaChartNavigatorConfig config = new SeaChartNavigatorConfig()
		{
			@Override
			public HudVisibility hudVisibility()
			{
				return HudVisibility.ALWAYS;
			}
		};
		SeaChartNavigationOverlay overlay = new SeaChartNavigationOverlay(client, config, state);
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

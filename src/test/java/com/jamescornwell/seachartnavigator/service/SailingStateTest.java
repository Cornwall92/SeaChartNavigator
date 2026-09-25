package com.jamescornwell.seachartnavigator.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import net.runelite.api.Client;
import net.runelite.api.IndexedObjectSet;
import net.runelite.api.Player;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import org.junit.Before;
import org.junit.Test;

public class SailingStateTest
{
	private final Client client = mock(Client.class);
	private final Player player = mock(Player.class);
	private final WorldView topLevel = mock(WorldView.class);
	private final WorldView boatView = mock(WorldView.class);
	private final WorldEntity boat = mock(WorldEntity.class);
	private final LocalPoint local = new LocalPoint(10 * 128 + 64, 20 * 128 + 64, WorldView.TOPLEVEL);

	@Before
	public void setUp()
	{
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getTopLevelWorldView()).thenReturn(topLevel);
		when(client.getWorldView(WorldView.TOPLEVEL)).thenReturn(topLevel);
		when(player.getWorldView()).thenReturn(topLevel);
		when(player.getLocalLocation()).thenReturn(local);
		when(topLevel.isTopLevel()).thenReturn(true);
		when(topLevel.getBaseX()).thenReturn(3100);
		when(topLevel.getBaseY()).thenReturn(3200);
	}

	@Test
	public void resolvesOrdinaryWorldPosition()
	{
		assertEquals(new WorldPoint(3110, 3220, 0), SailingState.getTopLevelWorldPoint(client));
		assertFalse(SailingState.isSailing(client));
	}

	@Test
	public void missingPlayerReturnsNoPosition()
	{
		when(client.getLocalPlayer()).thenReturn(null);
		assertNull(SailingState.getTopLevelWorldPoint(client));
		assertFalse(SailingState.isSailing(client));
	}

	@Test
	public void missingPlayerWorldViewReturnsNoPosition()
	{
		when(player.getWorldView()).thenReturn(null);
		assertNull(SailingState.getTopLevelWorldPoint(client));
		assertFalse(SailingState.isSailing(client));
	}

	@Test
	public void missingLocalPositionReturnsNoPosition()
	{
		when(player.getLocalLocation()).thenReturn(null);
		assertNull(SailingState.getTopLevelWorldPoint(client));
	}

	@Test
	public void missingTopLevelDuringBoatUnloadReturnsNoPosition()
	{
		when(player.getWorldView()).thenReturn(boatView);
		when(client.getTopLevelWorldView()).thenReturn(null);
		assertNull(SailingState.getTopLevelWorldPoint(client));
	}

	@Test
	public void resolvesBoatPositionInTheTopLevelWorld()
	{
		boardBoat();
		when(boat.transformToMainWorld(local)).thenReturn(new LocalPoint(50 * 128 + 64, 60 * 128 + 64, WorldView.TOPLEVEL));
		assertEquals(new WorldPoint(3150, 3260, 0), SailingState.getTopLevelWorldPoint(client));
		assertTrue(SailingState.isSailing(client));
		verify(boat).transformToMainWorld(local);
	}

	@Test
	public void missingBoatEntityReturnsNoPosition()
	{
		boardBoat();
		when(topLevel.worldEntities().byIndex(5)).thenReturn(null);
		assertNull(SailingState.getTopLevelWorldPoint(client));
	}

	@Test
	public void unavailableBoatTransformReturnsNoPosition()
	{
		boardBoat();
		when(boat.transformToMainWorld(local)).thenReturn(null);
		assertNull(SailingState.getTopLevelWorldPoint(client));
	}

	@SuppressWarnings("unchecked")
	private void boardBoat()
	{
		IndexedObjectSet<WorldEntity> entities = mock(IndexedObjectSet.class);
		doReturn(entities).when(topLevel).worldEntities();
		when(player.getWorldView()).thenReturn(boatView);
		when(boatView.getId()).thenReturn(5);
		when(entities.byIndex(5)).thenReturn(boat);
	}
}

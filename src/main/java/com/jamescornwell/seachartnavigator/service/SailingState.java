package com.jamescornwell.seachartnavigator.service;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/** Identifies a player aboard a Sailing boat and resolves their real map position. */
public final class SailingState
{
	private SailingState()
	{
	}

	/**
	 * Sailing places a boarded player inside their boat's non-top-level WorldView.
	 */
	public static boolean isSailing(Client client)
	{
		Player player = client.getLocalPlayer();
		return player != null && player.getWorldView() != null && !player.getWorldView().isTopLevel();
	}

	/**
	 * Converts the local boat position back into the main world-map coordinate system.
	 */
	public static WorldPoint getTopLevelWorldPoint(Client client)
	{
		Player player = client.getLocalPlayer();
		if (player == null || player.getWorldView() == null || player.getLocalLocation() == null)
		{
			return null;
		}

		WorldView worldView = player.getWorldView();
		LocalPoint localPoint = player.getLocalLocation();
		if (!worldView.isTopLevel())
		{
			WorldEntity boat = client.getTopLevelWorldView()
				.worldEntities()
				.byIndex(worldView.getId());
			if (boat == null)
			{
				return null;
			}
			localPoint = boat.transformToMainWorld(localPoint);
		}

		return WorldPoint.fromLocal(client, localPoint);
	}
}

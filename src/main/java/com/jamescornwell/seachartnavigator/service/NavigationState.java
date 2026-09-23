package com.jamescornwell.seachartnavigator.service;

import javax.inject.Singleton;
import com.jamescornwell.seachartnavigator.model.ChartingTask;
import net.runelite.api.coords.WorldPoint;

/** Current navigation target and the last calculated distance to it. */
@Singleton
public class NavigationState
{
	private ChartingTask target;
	private WorldPoint playerLocation;
	private int distance;

	public void setTarget(ChartingTask target, WorldPoint playerLocation, int distance)
	{
		this.target = target;
		this.playerLocation = playerLocation;
		this.distance = distance;
	}

	public void clear()
	{
		target = null;
		playerLocation = null;
		distance = 0;
	}

	public ChartingTask getTarget()
	{
		return target;
	}

	public WorldPoint getPlayerLocation()
	{
		return playerLocation;
	}

	public int getDistance()
	{
		return distance;
	}
}


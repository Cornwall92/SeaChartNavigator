package com.jamescornwell.seachartnavigator.service;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import java.util.Collection;
import java.util.function.Predicate;
import javax.inject.Singleton;
import net.runelite.api.coords.WorldPoint;

/** Finds the closest eligible target. */
@Singleton
public class TargetSelector
{
	public ChartingTask selectClosest(
		Collection<ChartingTask> tasks,
		WorldPoint playerLocation,
		Predicate<ChartingTask> isEligible
	)
	{
		ChartingTask closest = null;
		int closestDistance = Integer.MAX_VALUE;

		for (ChartingTask task : tasks)
		{
			if (!isEligible.test(task))
			{
				continue;
			}

			int distance = distance(playerLocation, task.getLocation());
			if (distance < closestDistance)
			{
				closest = task;
				closestDistance = distance;
			}
		}

		return closest;
	}

	public int distance(WorldPoint from, WorldPoint to)
	{
		return NavigationMath.tileDistance(from, to);
	}
}

package com.jamescornwell.seachartnavigator.service;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import java.util.Collection;
import java.util.function.Predicate;
import javax.inject.Singleton;
import net.runelite.api.coords.WorldPoint;

/** Finds the closest eligible target and prevents needless target flicker. */
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

	public ChartingTask selectWithHysteresis(
		Collection<ChartingTask> tasks,
		WorldPoint playerLocation,
		Predicate<ChartingTask> isEligible,
		ChartingTask currentTarget,
		int switchHysteresis
	)
	{
		ChartingTask closest = selectClosest(tasks, playerLocation, isEligible);
		if (currentTarget == null || closest == null || currentTarget == closest || !isEligible.test(currentTarget))
		{
			return closest;
		}

		int currentDistance = distance(playerLocation, currentTarget.getLocation());
		int closestDistance = distance(playerLocation, closest.getLocation());
		if (currentDistance <= closestDistance + Math.max(0, switchHysteresis))
		{
			return currentTarget;
		}

		return closest;
	}

	public int distance(WorldPoint from, WorldPoint to)
	{
		if (from == null || to == null)
		{
			return Integer.MAX_VALUE;
		}

		return Math.max(Math.abs(from.getX() - to.getX()), Math.abs(from.getY() - to.getY()));
	}
}


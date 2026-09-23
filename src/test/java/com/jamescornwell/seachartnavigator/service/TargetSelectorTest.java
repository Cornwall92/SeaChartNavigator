package com.jamescornwell.seachartnavigator.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.model.ChartingTaskType;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import net.runelite.api.coords.WorldPoint;

public class TargetSelectorTest
{
	private final TargetSelector selector = new TargetSelector();

	@Test
	public void selectsTheClosestEligibleTask()
	{
		ChartingTask near = task(1, 101, 105, 1);
		ChartingTask far = task(2, 160, 160, 1);
		ChartingTask locked = task(3, 100, 101, 99);
		List<ChartingTask> tasks = Arrays.asList(near, far, locked);

		ChartingTask selected = selector.selectClosest(tasks, new WorldPoint(100, 100, 0), task -> task.hasSailingLevel(20));

		assertSame(near, selected);
	}

	@Test
	public void retainsCurrentTargetUntilTheNewOneIsClearlyCloser()
	{
		ChartingTask current = task(1, 110, 100, 1);
		ChartingTask slightlyCloser = task(2, 105, 100, 1);
		List<ChartingTask> tasks = Arrays.asList(current, slightlyCloser);

		ChartingTask selected = selector.selectWithHysteresis(
			tasks,
			new WorldPoint(100, 100, 0),
			task -> true,
			current,
			6
		);

		assertSame(current, selected);
	}

	@Test
	public void calculatesChebyshevTileDistance()
	{
		assertEquals(8, selector.distance(new WorldPoint(100, 100, 0), new WorldPoint(108, 103, 0)));
	}

	private ChartingTask task(int id, int x, int y, int level)
	{
		return new ChartingTask(id, "Task " + id, ChartingTaskType.GENERIC, id, new WorldPoint(x, y, 0), level);
	}
}


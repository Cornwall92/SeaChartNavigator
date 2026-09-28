package com.jamescornwell.seachartnavigator.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNull;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.model.ChartingTaskType;
import java.util.Arrays;
import java.util.Collections;
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
	public void calculatesChebyshevTileDistance()
	{
		assertEquals(8, selector.distance(new WorldPoint(100, 100, 0), new WorldPoint(108, 103, 0)));
	}

	@Test
	public void selectsANewTargetAfterThePlayerMoves()
	{
		ChartingTask west = task(1, 100, 100, 1);
		ChartingTask east = task(2, 120, 100, 1);
		List<ChartingTask> tasks = Arrays.asList(west, east);
		assertSame(west, selector.selectClosest(tasks, new WorldPoint(101, 100, 0), task -> true));
		assertSame(east, selector.selectClosest(tasks, new WorldPoint(119, 100, 0), task -> true));
	}

	@Test
	public void keepsDatasetOrderForEqualDistances()
	{
		ChartingTask first = task(1, 95, 100, 1);
		ChartingTask second = task(2, 105, 100, 1);
		assertSame(first, selector.selectClosest(Arrays.asList(first, second),
			new WorldPoint(100, 100, 0), task -> true));
	}

	@Test
	public void noEligibleTasksOrNoPositionReturnsNoTarget()
	{
		List<ChartingTask> tasks = Collections.singletonList(task(1, 100, 100, 20));
		WorldPoint player = new WorldPoint(100, 100, 0);
		assertNull(selector.selectClosest(tasks, player, task -> task.hasSailingLevel(19)));
		assertNull(selector.selectClosest(tasks, null, task -> true));
		assertNull(selector.selectClosest(Collections.emptyList(), player, task -> true));
	}

	@Test
	public void exactRequiredLevelUnlocksTheTask()
	{
		ChartingTask target = task(1, 100, 100, 20);
		assertSame(target, selector.selectClosest(Collections.singletonList(target),
			new WorldPoint(100, 100, 0), task -> task.hasSailingLevel(20)));
	}

	private ChartingTask task(int id, int x, int y, int level)
	{
		return new ChartingTask(id, "Task " + id, ChartingTaskType.GENERIC, id, new WorldPoint(x, y, 0), level);
	}
}

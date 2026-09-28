package com.jamescornwell.seachartnavigator.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.model.ChartingTaskType;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class ChartingTaskRepositoryTest
{
	@Test
	public void loadsEveryBundledChartingTask()
	{
		ChartingTaskRepository repository = new ChartingTaskRepository();

		assertEquals(358, repository.getTasks().size());
		assertTrue(repository.isCompletionVarbit(18574));
		assertNotNull(repository.getTasks().get(0).getLocation());
	}

	@Test
	public void everyBundledTaskHasValidUniqueMetadata()
	{
		ChartingTaskRepository repository = new ChartingTaskRepository();
		Set<Integer> ids = new HashSet<>();
		Set<Integer> varbits = new HashSet<>();
		Set<ChartingTaskType> types = EnumSet.noneOf(ChartingTaskType.class);
		for (ChartingTask task : repository.getTasks())
		{
			assertTrue("Duplicate task ID: " + task.getId(), ids.add(task.getId()));
			assertTrue("Duplicate completion varbit: " + task.getCompletionVarbit(),
				varbits.add(task.getCompletionVarbit()));
			assertTrue(task.getCompletionVarbit() > 0);
			assertTrue(repository.isCompletionVarbit(task.getCompletionVarbit()));
			assertTrue(!task.getTitle().trim().isEmpty());
			assertTrue(task.getRequiredSailingLevel() >= 1 && task.getRequiredSailingLevel() <= 99);
			assertTrue(task.getLocation().getX() > 0 && task.getLocation().getY() > 0);
			assertEquals(0, task.getLocation().getPlane());
			types.add(task.getType());
		}
		assertEquals(EnumSet.allOf(ChartingTaskType.class), types);
	}
}

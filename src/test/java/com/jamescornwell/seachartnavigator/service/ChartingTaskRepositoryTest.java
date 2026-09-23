package com.jamescornwell.seachartnavigator.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
}


package com.jamescornwell.seachartnavigator.service;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.model.ChartingTaskType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Singleton;
import net.runelite.api.coords.WorldPoint;

/** Loads the bundled task coordinates and completion varbits once per client session. */
@Singleton
public class ChartingTaskRepository
{
	private static final String DATA_RESOURCE = "/com/jamescornwell/seachartnavigator/charting-tasks.tsv";

	private final List<ChartingTask> tasks;
	private final Set<Integer> completionVarbits;

	public ChartingTaskRepository()
	{
		tasks = Collections.unmodifiableList(loadTasks());
		Set<Integer> varbits = new HashSet<>();
		for (ChartingTask task : tasks)
		{
			varbits.add(task.getCompletionVarbit());
		}
		completionVarbits = Collections.unmodifiableSet(varbits);
	}

	public List<ChartingTask> getTasks()
	{
		return tasks;
	}

	public boolean isCompletionVarbit(int varbitId)
	{
		return completionVarbits.contains(varbitId);
	}

	private List<ChartingTask> loadTasks()
	{
		InputStream stream = ChartingTaskRepository.class.getResourceAsStream(DATA_RESOURCE);
		if (stream == null)
		{
			throw new IllegalStateException("Missing charting task data: " + DATA_RESOURCE);
		}

		List<ChartingTask> result = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
		{
			String line;
			int lineNumber = 0;
			while ((line = reader.readLine()) != null)
			{
				lineNumber++;
				if (lineNumber == 1 || line.trim().isEmpty())
				{
					continue;
				}

				String[] fields = line.split("\\t", -1);
				if (fields.length != 8)
				{
					throw new IllegalStateException("Invalid charting data at line " + lineNumber);
				}

				result.add(new ChartingTask(
					Integer.parseInt(fields[0]),
					fields[1],
					ChartingTaskType.valueOf(fields[2]),
					Integer.parseInt(fields[3]),
					new WorldPoint(Integer.parseInt(fields[4]), Integer.parseInt(fields[5]), Integer.parseInt(fields[6])),
					Integer.parseInt(fields[7])
				));
			}
		}
		catch (IOException | IllegalArgumentException exception)
		{
			throw new IllegalStateException("Unable to read charting task data", exception);
		}

		if (result.isEmpty())
		{
			throw new IllegalStateException("No charting task data was loaded");
		}

		return result;
	}
}

package com.jamescornwell.seachartnavigator.model;

import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

/** Immutable information needed to navigate to one sea charting task. */
public final class ChartingTask
{
	private final int id;
	private final String title;
	private final ChartingTaskType type;
	private final int completionVarbit;
	private final WorldPoint location;
	private final int requiredSailingLevel;

	public ChartingTask(
		int id,
		String title,
		ChartingTaskType type,
		int completionVarbit,
		WorldPoint location,
		int requiredSailingLevel
	)
	{
		this.id = id;
		this.title = title;
		this.type = type;
		this.completionVarbit = completionVarbit;
		this.location = location;
		this.requiredSailingLevel = requiredSailingLevel;
	}

	public int getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public ChartingTaskType getType()
	{
		return type;
	}

	public int getCompletionVarbit()
	{
		return completionVarbit;
	}

	public WorldPoint getLocation()
	{
		return location;
	}

	public int getRequiredSailingLevel()
	{
		return requiredSailingLevel;
	}

	public boolean isComplete(Client client)
	{
		return client.getVarbitValue(completionVarbit) != 0;
	}

	public boolean hasSailingLevel(int sailingLevel)
	{
		return sailingLevel >= requiredSailingLevel;
	}
}


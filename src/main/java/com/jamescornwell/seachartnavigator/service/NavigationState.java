package com.jamescornwell.seachartnavigator.service;

import javax.inject.Singleton;
import com.jamescornwell.seachartnavigator.model.ChartingTask;
/** Current navigation target. Position and distance are read live by the HUD. */
@Singleton
public class NavigationState
{
	private ChartingTask target;

	public void setTarget(ChartingTask target)
	{
		this.target = target;
	}

	public void clear()
	{
		target = null;
	}

	public ChartingTask getTarget()
	{
		return target;
	}

}

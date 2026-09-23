package com.jamescornwell.seachartnavigator.ui;

import com.jamescornwell.seachartnavigator.model.ChartingTask;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

/** A single map marker for the current nearest target. */
public final class TargetMapPoint extends WorldMapPoint
{
	private final int taskId;

	public TargetMapPoint(ChartingTask task)
	{
		super(WorldMapPoint.builder()
			.worldPoint(task.getLocation())
			.image(createIcon())
			.tooltip("Nearest sea charting task: " + task.getTitle()));
		this.taskId = task.getId();
	}

	public int getTaskId()
	{
		return taskId;
	}

	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(18, 18, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setColor(new Color(0, 0, 0, 180));
			graphics.fillOval(1, 1, 16, 16);
			graphics.setStroke(new BasicStroke(2f));
			graphics.setColor(new Color(78, 219, 178));
			graphics.drawOval(2, 2, 14, 14);
			graphics.fillOval(7, 7, 4, 4);
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}
}


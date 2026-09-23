package com.jamescornwell.seachartnavigator.ui;

import com.jamescornwell.seachartnavigator.SeaChartNavigatorConfig;
import com.jamescornwell.seachartnavigator.HudVisibility;
import com.jamescornwell.seachartnavigator.model.ChartingTask;
import com.jamescornwell.seachartnavigator.service.NavigationMath;
import com.jamescornwell.seachartnavigator.service.NavigationState;
import com.jamescornwell.seachartnavigator.service.SailingState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** Live directional overlay for the currently selected sea charting task. */
@Singleton
public class SeaChartNavigationOverlay extends Overlay
{
	private static final int ARROW_SIZE = 28;
	private static final int PADDING = 7;

	private final Client client;
	private final SeaChartNavigatorConfig config;
	private final NavigationState navigationState;

	@Inject
	public SeaChartNavigationOverlay(Client client, SeaChartNavigatorConfig config, NavigationState navigationState)
	{
		this.client = client;
		this.config = config;
		this.navigationState = navigationState;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(true);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.navigatorEnabled() || !shouldRenderHud())
		{
			return null;
		}

		ChartingTask target = navigationState.getTarget();
		WorldPoint playerLocation = SailingState.getTopLevelWorldPoint(client);
		if (target == null || playerLocation == null)
		{
			return null;
		}

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics metrics = graphics.getFontMetrics();
		String title = target.getTitle();
		String activity = "Type: " + target.getType().getDisplayName() + " \u00b7 Sailing " + target.getRequiredSailingLevel();
		String distance = NavigationMath.tileDistance(playerLocation, target.getLocation()) + " tiles away";

		int textWidth = Math.max(metrics.stringWidth(title), Math.max(metrics.stringWidth(activity), metrics.stringWidth(distance)));
		int height = Math.max(ARROW_SIZE + (PADDING * 2), (metrics.getHeight() * 3) + (PADDING * 2));
		int width = ARROW_SIZE + (PADDING * 3) + textWidth;

		graphics.setColor(config.backgroundColor());
		graphics.fillRoundRect(0, 0, width, height, 8, 8);
		drawDirectionArrow(graphics, playerLocation, target.getLocation(), height);

		int textX = ARROW_SIZE + (PADDING * 2);
		int textY = PADDING + metrics.getAscent();
		graphics.setColor(Color.WHITE);
		graphics.drawString(title, textX, textY);
		graphics.setColor(new Color(208, 221, 216));
		graphics.drawString(activity, textX, textY + metrics.getHeight());
		graphics.drawString(distance, textX, textY + (metrics.getHeight() * 2));

		return new Dimension(width, height);
	}

	private boolean shouldRenderHud()
	{
		HudVisibility visibility = config.hudVisibility();
		return visibility == HudVisibility.ALWAYS ||
			(visibility == HudVisibility.WHEN_SAILING && SailingState.isSailing(client));
	}

	private void drawDirectionArrow(Graphics2D graphics, WorldPoint from, WorldPoint to, int height)
	{
		double angle = NavigationMath.cameraRelativeAngle(from, to, client.getCameraYaw());

		Graphics2D arrowGraphics = (Graphics2D) graphics.create();
		try
		{
			arrowGraphics.translate(PADDING + (ARROW_SIZE / 2.0), height / 2.0);
			arrowGraphics.rotate(angle);
			arrowGraphics.setColor(config.arrowColor());
			int halfWidth = ARROW_SIZE / 3;
			Polygon arrow = new Polygon(
				new int[]{0, -halfWidth, -halfWidth / 2, halfWidth / 2, halfWidth},
				new int[]{-ARROW_SIZE / 2, ARROW_SIZE / 3, ARROW_SIZE / 6, ARROW_SIZE / 6, ARROW_SIZE / 3},
				5
			);
			arrowGraphics.fillPolygon(arrow);
		}
		finally
		{
			arrowGraphics.dispose();
		}
	}
}

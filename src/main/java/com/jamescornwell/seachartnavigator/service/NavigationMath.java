package com.jamescornwell.seachartnavigator.service;

import net.runelite.api.coords.WorldPoint;

/** Small, testable calculations used by the live navigation HUD. */
public final class NavigationMath
{
	private static final double FULL_CIRCLE_RADIANS = Math.PI * 2.0;
	private static final int CAMERA_YAW_UNITS = 2048;

	private NavigationMath()
	{
	}

	public static int tileDistance(WorldPoint from, WorldPoint to)
	{
		if (from == null || to == null)
		{
			return Integer.MAX_VALUE;
		}

		return Math.max(Math.abs(from.getX() - to.getX()), Math.abs(from.getY() - to.getY()));
	}

	/**
	 * Returns the target direction relative to the current camera view.
	 * A result of zero points straight up on screen.
	 */
	public static double cameraRelativeAngle(WorldPoint from, WorldPoint to, int cameraYaw)
	{
		if (from == null || to == null)
		{
			return 0.0;
		}

		double worldBearing = Math.atan2(to.getX() - from.getX(), to.getY() - from.getY());
		double cameraRadians = (cameraYaw & (CAMERA_YAW_UNITS - 1))
			* (FULL_CIRCLE_RADIANS / CAMERA_YAW_UNITS);
		double relativeBearing = worldBearing + cameraRadians;
		return Math.atan2(Math.sin(relativeBearing), Math.cos(relativeBearing));
	}
}

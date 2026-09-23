package com.jamescornwell.seachartnavigator.service;

import static org.junit.Assert.assertEquals;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

public class NavigationMathTest
{
	@Test
	public void northIsStraightAheadWhenTheCameraFacesNorth()
	{
		assertEquals(
			0.0,
			NavigationMath.cameraRelativeAngle(new WorldPoint(100, 100, 0), new WorldPoint(100, 101, 0), 0),
			0.0001
		);
	}

	@Test
	public void eastIsStraightAheadWhenTheCameraFacesEast()
	{
		assertEquals(
			0.0,
			NavigationMath.cameraRelativeAngle(new WorldPoint(100, 100, 0), new WorldPoint(101, 100, 0), 1536),
			0.0001
		);
	}

	@Test
	public void northIsToTheRightWhenTheCameraFacesWest()
	{
		assertEquals(
			Math.PI / 2.0,
			NavigationMath.cameraRelativeAngle(new WorldPoint(100, 100, 0), new WorldPoint(100, 101, 0), 512),
			0.0001
		);
	}

	@Test
	public void calculatesChebyshevTileDistance()
	{
		assertEquals(8, NavigationMath.tileDistance(new WorldPoint(100, 100, 0), new WorldPoint(108, 103, 0)));
	}
}

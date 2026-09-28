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
			NavigationMath.cameraRelativeAngle(new WorldPoint(100, 100, 0), new WorldPoint(101, 100, 0), 12288),
			0.0001
		);
	}

	@Test
	public void northIsToTheRightWhenTheCameraFacesWest()
	{
		assertEquals(
			Math.PI / 2.0,
			NavigationMath.cameraRelativeAngle(new WorldPoint(100, 100, 0), new WorldPoint(100, 101, 0), 4096),
			0.0001
		);
	}

	@Test
	public void yawUsesTheFullFourteenBitAngleRange()
	{
		assertEquals(
			0.0,
			NavigationMath.cameraRelativeAngle(new WorldPoint(100, 100, 0), new WorldPoint(100, 101, 0), 16384),
			0.0001
		);
	}

	@Test
	public void everyCameraYawRotatesNorthAndEastBearingsOncePerTurn()
	{
		WorldPoint origin = new WorldPoint(100, 100, 0);
		WorldPoint north = new WorldPoint(100, 101, 0);
		WorldPoint east = new WorldPoint(101, 100, 0);
		for (int yaw = 0; yaw < 16384; yaw++)
		{
			double radians = yaw * Math.PI * 2.0 / 16384.0;
			double northAngle = NavigationMath.cameraRelativeAngle(origin, north, yaw);
			double eastAngle = NavigationMath.cameraRelativeAngle(origin, east, yaw);
			assertEquals(Math.sin(radians), Math.sin(northAngle), 0.000001);
			assertEquals(Math.cos(radians), Math.cos(northAngle), 0.000001);
			assertEquals(Math.cos(radians), Math.sin(eastAngle), 0.000001);
			assertEquals(-Math.sin(radians), Math.cos(eastAngle), 0.000001);
			assertEquals(northAngle, NavigationMath.cameraRelativeAngle(origin, north, yaw + 16384), 0.000001);
			assertEquals(northAngle, NavigationMath.cameraRelativeAngle(origin, north, yaw - 16384), 0.000001);
		}
	}

	@Test
	public void calculatesChebyshevTileDistance()
	{
		assertEquals(8, NavigationMath.tileDistance(new WorldPoint(100, 100, 0), new WorldPoint(108, 103, 0)));
	}
}

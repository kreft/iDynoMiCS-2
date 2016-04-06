package testJUnit;

import org.junit.Test;

import grid.subgrid.CoordinateMap;
import utility.ExtraMath;

import static org.junit.Assert.assertTrue;

import static testJUnit.AllTests.TOLERANCE;

public class CoordinateMapTest
{
	@Test
	public void mapShouldHandleKeys()
	{
		CoordinateMap map = new CoordinateMap();
		int[] coordA = {1, 2, 3};
		int[] coordB = {1, 2, 3};
		int[] coordC = {0, 0, 0};
		
		map.put(coordA, 1.0);
		map.put(coordB, 1.5);
		map.put(coordC, 2.0);
		assertTrue(ExtraMath.areEqual(map.get(coordA), 1.5, TOLERANCE));
		assertTrue(ExtraMath.areEqual(map.get(coordB), 1.5, TOLERANCE));
		assertTrue(ExtraMath.areEqual(map.get(coordC), 2.0, TOLERANCE));
		assertTrue(ExtraMath.areEqual(map.getTotal(), 3.5, TOLERANCE));
		
		map.clear();
		assertTrue(ExtraMath.areEqual(map.getTotal(), 0.0, TOLERANCE));
	}
}

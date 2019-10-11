package test.junit.oldTests;

import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE;

import org.junit.Test;

import shape.subvoxel.CoordinateMap;
import utility.ExtraMath;

/**
 * \brief Unit test checking that Coordinate Maps behave as they should,
 * assigning {@code double} values to coordinates that can be updated reliably.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class CoordinateMapTest
{
	@Test
	public void mapShouldBehaveItself()
	{
		CoordinateMap map = new CoordinateMap();
		/*
		 * Make coordA and coordB identical, but coordC different.
		 */
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
		/*
		 * Check the increase method works.
		 */
		map.increase(coordA, 6.0);
		map.increase(coordC, 0.5);
		assertTrue(ExtraMath.areEqual(map.get(coordB), 7.5, TOLERANCE));
		assertTrue(ExtraMath.areEqual(map.get(coordC), 2.5, TOLERANCE));
		assertTrue(ExtraMath.areEqual(map.getTotal(), 10.0, TOLERANCE));
		/*
		 * Check the scaling methods.
		 */
		map.scale();
		assertTrue(ExtraMath.areEqual(map.getTotal(), 1.0, TOLERANCE));
		map.scale(5.8);
		assertTrue(ExtraMath.areEqual(map.getTotal(), 5.8, TOLERANCE));
		/*
		 * Finally, check that the map clears correctly.
		 */
		map.clear();
		assertTrue(ExtraMath.areEqual(map.getTotal(), 0.0, TOLERANCE));
		assertTrue(map.keySet().isEmpty());
	}
}

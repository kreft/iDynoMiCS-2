/**
 * 
 */
package test;

import java.util.Arrays;

import grid.CartesianGrid;
import linearAlgebra.Vector;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public class SpatialGridTest
{
	
	
	
	/**\brief TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		iteratorTest();
	}
	
	private static void iteratorTest()
	{
		int nDim = 3;
		int sideLength = 3;
		boolean inclIndirectNeighbors = true;
		double resolution = 1.0;
		
		int[] nVoxel = Vector.vector(3, 1);
		for ( int i = 0; i < nDim; i++ )
			nVoxel[i] = sideLength;
		CartesianGrid grid = new CartesianGrid(nVoxel, resolution);
		
		int[] coord = grid.resetIterator();
		int[] nbh;
		int nbhCounter;
		
		while ( grid.isIteratorValid() )
		{
			System.out.println("Looking at coordinate "+Arrays.toString(coord));
			nbh = grid.resetNbhIterator(inclIndirectNeighbors);
			nbhCounter = 0;
			while ( grid.isNbhIteratorValid() )
			{
				System.out.println("\t"+Arrays.toString(nbh)+" is a neighbor");
				nbh = grid.nbhIteratorNext();
				nbhCounter++;
			}
			System.out.println("\t"+nbhCounter+" neighbors");
			coord = grid.iteratorNext();
		}
	}
}
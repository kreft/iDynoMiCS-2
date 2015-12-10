/**
 * 
 */
package test;

import java.util.Arrays;

import boundary.Boundary;
import boundary.BoundaryFixed;
import grid.GridBoundary;
import grid.CartesianGrid;
import grid.SpatialGrid;
import idynomics.Compartment;
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
		//comaprtmentIteratorTest();
	}
	
	private static void iteratorTest()
	{
		int nDim = 2;
		int sideLength = 3;
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
			nbh = grid.resetNbhIterator();
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
	
	private static void comaprtmentIteratorTest()
	{
		Compartment aCompartment = new Compartment("rectangle");
		aCompartment.setSideLengths(new double[] {3.0, 3.0, 1.0});
		aCompartment.addSolute("test");
		Boundary xmin = new BoundaryFixed();
		xmin.setGridMethod("test", GridBoundary.constantDirichlet(1.0));
		aCompartment.addBoundary("xmin", xmin);
		Boundary xmax = new BoundaryFixed();
		xmax.setGridMethod("test", GridBoundary.constantDirichlet(0.0));
		aCompartment.addBoundary("xmax", xmax);
		aCompartment.init();
		
		SpatialGrid grid = aCompartment.getSolute("test");
		System.out.println("grid size: "+Arrays.toString(grid.getNumVoxels()));
		int[] current, nbh;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				  current = grid.iteratorNext())
		{
			System.out.println("current: "+Arrays.toString(current));
			for ( nbh = grid.resetNbhIterator(); 
					grid.isNbhIteratorValid(); nbh = grid.nbhIteratorNext() )
			{
				System.out.println("\tnbh: "+Arrays.toString(nbh));
			}
		}
	}
}
/**
 * 
 */
package test;

import java.util.Arrays;

import boundary.Boundary;
import boundary.BoundaryFixed;
import boundary.grid.GridMethodLibrary;
import grid.CartesianGrid;
import grid.SpatialGrid;
import idynomics.Compartment;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 * @deprecated
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
		
		double[] totalLength = Vector.onesDbl(3);
		for ( int i = 0; i < nDim; i++ )
			totalLength[i] = sideLength;
		CartesianGrid grid = new CartesianGrid(totalLength);
		
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
		/* Set up the dimensions and boundaries. */
		Boundary xmin = new BoundaryFixed();
		GridMethodLibrary.ConstantDirichlet testXmin = new GridMethodLibrary.ConstantDirichlet();
		testXmin.setValue(1.0);
		xmin.setGridMethod("test", testXmin);
		aCompartment.getShape().setBoundary(DimName.X, 0, xmin);
		Boundary xmax = new BoundaryFixed();
		GridMethodLibrary.ConstantDirichlet testXmax = new GridMethodLibrary.ConstantDirichlet();
		testXmax.setValue(0.0);
		xmax.setGridMethod("test", testXmax);
		aCompartment.getShape().setBoundary(DimName.X, 1, xmax);
		aCompartment.setSideLengths(new double[] {3.0, 3.0, 1.0});
		aCompartment.addSolute("test");
		aCompartment.init();
		
		SpatialGrid grid = aCompartment.getSolute("test");
		int[] current, nbh;
		current = grid.resetIterator();
		System.out.println("grid size: "+Arrays.toString(grid.getCurrentNVoxel()));
		for ( ; grid.isIteratorValid(); current = grid.iteratorNext())
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
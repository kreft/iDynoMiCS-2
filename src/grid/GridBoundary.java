/**
 * 
 */
package grid;

import grid.SpatialGrid.ArrayType;

/**
 * @author cleggrj
 *
 */
public final class GridBoundary
{
	/**
	 * Interface detailing what should be done at a boundary. Typical examples
	 * include Dirichlet and Neumann boundary conditions. 
	 */
	public interface GridMethod
	{
		double getBoundaryFlux(SpatialGrid grid);
	}
	
	
	
	/*************************************************************************
	 * COMMON GRIDMETHODS
	 ************************************************************************/
	
	public static double calcFlux(double bndryConcn, double gridConcn,
										double diffusivity, double surfaceArea)
	{
		return (bndryConcn - gridConcn) * diffusivity * surfaceArea;
	}
	
	public static GridMethod constantDirichlet(double value)
	{
		return new GridMethod()
		{
			@Override
			public double getBoundaryFlux(SpatialGrid grid)
			{
				return calcFlux(value, 
								grid.getValueAtCurrent(ArrayType.CONCN),
								grid.getValueAtCurrent(ArrayType.DIFFUSIVITY),
								grid.getNbhSharedSurfaceArea());
			}
		};
	}
	
	public static GridMethod constantNeumann(double gradient)
	{
		return new GridMethod()
		{
			public double getBoundaryFlux(SpatialGrid grid)
			{
				return gradient;
			}
			
		};
	}
	
	public static GridMethod zeroFlux()
	{
		return constantNeumann(0.0);
	}
	
	public static GridMethod cyclic()
	{
		return new GridMethod()
		{
			public double getBoundaryFlux(SpatialGrid grid)
			{
				int[] nbh = grid.cyclicTransform(grid.neighborCurrent());
				double d = 0.5*(grid.getValueAtCurrent(ArrayType.DIFFUSIVITY)+
								 grid.getValueAt(ArrayType.DIFFUSIVITY, nbh));
				return calcFlux(grid.getValueAt(ArrayType.CONCN, nbh),
								grid.getValueAtCurrent(ArrayType.CONCN),
								d, grid.getNbhSharedSurfaceArea());
			}
		};
	}
}

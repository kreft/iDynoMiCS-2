package gridMethod;

import boundary.Boundary;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;

public class Cyclic implements GridMethod {

	public double getBoundaryFlux(SpatialGrid grid)
	{
		int[] nbh = grid.cyclicTransform(grid.neighborCurrent());
		double d = 0.5*(grid.getValueAtCurrent(ArrayType.DIFFUSIVITY)+
						 grid.getValueAt(ArrayType.DIFFUSIVITY, nbh));
		return Boundary.calcFlux(grid.getValueAt(ArrayType.CONCN, nbh),
						grid.getValueAtCurrent(ArrayType.CONCN),
						d, grid.getNbhSharedSurfaceArea());
	}
}

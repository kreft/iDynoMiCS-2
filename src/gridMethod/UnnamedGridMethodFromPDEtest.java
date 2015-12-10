package gridMethod;

import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import linearAlgebra.Vector;
import boundary.Boundary;

public class UnnamedGridMethodFromPDEtest {

	public double getBoundaryFlux(SpatialGrid grid)
	{
		int[] current = grid.iteratorCurrent();
		return Boundary.calcFlux(
						Vector.sum(current)/4.0, 
						grid.getValueAtCurrent(ArrayType.CONCN),
						grid.getValueAtCurrent(ArrayType.DIFFUSIVITY),
						grid.getNbhSharedSurfaceArea());
	}
}

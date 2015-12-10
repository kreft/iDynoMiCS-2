package gridMethod;

import boundary.Boundary;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;

public class ConstantDirichlet implements GridMethod {
	
	// or should this be final?
	protected double value;
	
	ConstantDirichlet(double value)
	{
		this.value = value;
	}

	public double getBoundaryFlux(SpatialGrid grid)
	{
		return Boundary.calcFlux(value, 
						grid.getValueAtCurrent(ArrayType.CONCN),
						grid.getValueAtCurrent(ArrayType.DIFFUSIVITY),
						grid.getNbhSharedSurfaceArea());
	}
}

package gridMethod;

import grid.SpatialGrid;

public class ConstantNeumann implements GridMethod {
	
	protected double gradient;
	
	ConstantNeumann(double gradient)
	{
		this.gradient = gradient;
	}
	
	public double getBoundaryFlux(SpatialGrid grid)
	{
		return gradient;
	}
}

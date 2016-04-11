package grid.subgrid;

import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SubgridPoint
{
	public double[] internalLocation;
	
	public double[] realLocation;
	
	public double volume;
	
	public SubgridPoint(double[] internal)
	{
		this.internalLocation = internal;
	}
	
	public SubgridPoint()
	{
		this(Vector.zerosDbl(3));
	}
	
	public SubgridPoint getNeighbor(int dim, double newInternal)
	{
		double[] newLoc = Vector.copy(this.internalLocation);
		newLoc[dim] = newInternal;
		return new SubgridPoint(newLoc);
	}
	
	public double[] getRealLocation(int numberOfDimensions)
	{
		return Vector.subset(this.realLocation, numberOfDimensions);
	}
}

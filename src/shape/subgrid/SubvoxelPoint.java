package shape.subgrid;

import linearAlgebra.Vector;

/**
 * \brief A spatial point belonging to a voxel.
 * 
 * <p>Used for estimating how much of an {@code Agent} lies in each voxel.</p>
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SubvoxelPoint
{
	/**
	 * The relative location of this point within the voxel it belongs to.
	 */
	public double[] internalLocation;
	
	/**
	 * The location of this point in, e.g., the {@code Compartment}.
	 */
	public double[] realLocation;
	
	/**
	 * The volume this point represents.
	 */
	public double volume;
	
	/**
	 * \brief Construct a sub-voxel point with known relative location.
	 * 
	 * @param internal Relative location of this point within the voxel it
	 * belongs to.
	 */
	public SubvoxelPoint(double[] internal)
	{
		this.internalLocation = internal;
	}
	
	/**
	 * \brief Construct a sub-voxel point at the origin of the voxel it belongs
	 * to.
	 * 
	 * <p>Useful if the point should be at this location, or if its location is
	 * not yet known.</p>
	 */
	public SubvoxelPoint()
	{
		this(Vector.zerosDbl(3));
	}
	
	/**
	 * \brief Construct a neighbouring sub-voxel point to this one, with its
	 * location shifted along one dimension.
	 * 
	 * @param dim The dimension to change.
	 * @param newInternal The new relative location to take for this dimension.
	 * @return A new sub-voxel point.
	 */
	public SubvoxelPoint getNeighbor(int dim, double newInternal)
	{
		double[] newLoc = Vector.copy(this.internalLocation);
		newLoc[dim] = newInternal;
		return new SubvoxelPoint(newLoc);
	}
	
	/**
	 * @param numberOfDimensions Number of dimensions required.
	 * @return New vector, that is a copy of this point's real location but
	 * with only as many dimensions as requested.
	 */
	public double[] getRealLocation(int numberOfDimensions)
	{
		return Vector.subset(this.realLocation, numberOfDimensions);
	}
}

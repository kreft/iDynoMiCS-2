package grid;

import shape.Shape;

/**
 * \brief Subclass of SpatialGrid that discretises space into rectilinear
 * voxels. Uses the (X, Y, Z) dimension system.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class CartesianGrid extends SpatialGrid
{	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public CartesianGrid(Shape shape){
		super(shape);
	}
}
/**
 * 
 */
package boundary;

import grid.SpatialGrid.GridMethod;

/**
 * \brief Abstract subclass of Boundary that defines boundaries inside a
 * single Compartment.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class BoundaryInternal extends Boundary
{
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**\brief TODO
	 * 
	 */
	public BoundaryInternal()
	{
		/*
		 * Internal boundaries should make no difference to the grid.
		 */
		this._gridMethod = new GridMethod()
		{
			@Override
			public int[] getCorrectCoord(int[] coord)
			{ return coord; }
		};
	}

}

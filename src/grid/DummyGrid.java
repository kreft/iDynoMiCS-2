/**
 * 
 */
package grid;

import shape.Shape;

/**
 * \brief A dummy grid for use by dimensionless shapes (e.g. for a chemostat).
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class DummyGrid extends SpatialGrid
{
	/**
	 * The volume of the single voxel in this grid.
	 */
	protected double _volume;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief New {@code DummyGrid} object, where the volume must be specified.
	 */
	public DummyGrid(Shape shape, double volume)
	{
		super(shape);
		this._volume = volume;
	}
	
	/*************************************************************************
	 * USEFUL METHODS
	 ************************************************************************/
	
	@Override
	public String arrayAsText(ArrayType type)
	{
		return String.valueOf(this._array.get(type)[0][0][0]);
	}
}

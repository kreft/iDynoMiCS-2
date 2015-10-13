/**
 * 
 */
package boundary;

import grid.SpatialGrid.GridMethod;
import shape.Shape;

/**
 * \brief Abstract class of boundary for a Compartment.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Boundary
{
	/**
	 * The shape this Boundary takes (e.g. Plane, Sphere).
	 */
	protected Shape _shape;
	
	protected GridMethod _gridMethod;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 */
	public Boundary()
	{
		
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Shape getShape()
	{
		return this._shape;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param position
	 * @return
	 */
	public boolean isOutside(double[] position)
	{
		return this._shape.isOutside(position);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param position
	 * @return
	 */
	public double distance(double[] position)
	{
		return this._shape.distance(position);
	}
}

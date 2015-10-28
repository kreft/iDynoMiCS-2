/**
 * 
 */
package boundary;

import grid.SpatialGrid;
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
		/*
		 * This is the default grid method: any coordinate outside of the
		 * shape is disallowed. 
		 */
		this._gridMethod = zeroFlux();
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
	 * @param aShape
	 */
	public void setShape(Shape aShape)
	{
		this._shape = aShape;
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
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public GridMethod getGridMethod()
	{
		return this._gridMethod;
	}
	
	/*************************************************************************
	 * COMMON GRIDMETHODS
	 ************************************************************************/
	
	protected static GridMethod dirichlet(double value)
	{
		return new GridMethod()
		{
			@Override
			public int[] getCorrectCoord(int[] coord) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public double getConcnGradient(String sName, SpatialGrid grid)
			{
				return 0;
			}
		};
	}
	
	protected static GridMethod neumann(double gradient)
	{
		return new GridMethod()
		{
			@Override
			public int[] getCorrectCoord(int[] coord) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public double getConcnGradient(String sName, SpatialGrid grid)
			{
				return gradient;
			}
			
		};
	}
	
	protected static GridMethod zeroFlux()
	{
		return neumann(0.0);
	}
	
}

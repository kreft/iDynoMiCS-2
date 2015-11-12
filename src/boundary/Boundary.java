/**
 * 
 */
package boundary;

import java.util.HashMap;

import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
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
	
	/**
	 * 
	 */
	protected GridMethod _defaultGridMethod;
	
	/**
	 * 
	 */
	protected HashMap<String,GridMethod> _gridMethods = 
											new HashMap<String,GridMethod>();
	
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
	
	public void setGridMethod(String soluteName, GridMethod aMethod)
	{
		this._gridMethods.put(soluteName, aMethod);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public GridMethod getGridMethod(String soluteName)
	{
		//System.out.println("Looking for "+soluteName); //bughunt
		if ( this._gridMethods.containsKey(soluteName) )
			return this._gridMethods.get(soluteName);
		else
			return this._defaultGridMethod;
	}
	
	/*************************************************************************
	 * COMMON GRIDMETHODS
	 ************************************************************************/
	
	public static GridMethod constantDirichlet(double value)
	{
		return new GridMethod()
		{
			@Override
			public int[] getCorrectCoord(int[] coord) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public double getBoundaryFlux(SpatialGrid grid)
			{
				double out = value - grid.getValueAtCurrent(ArrayType.CONCN);
				out *= grid.getValueAtCurrent(ArrayType.DIFFUSIVITY);
				out *= Math.pow(grid.getResolution(), -2.0);
				return out;
			}
		};
	}
	
	public static GridMethod constantNeumann(double gradient)
	{
		return new GridMethod()
		{
			@Override
			public int[] getCorrectCoord(int[] coord) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public double getBoundaryFlux(SpatialGrid grid)
			{
				return gradient;
			}
			
		};
	}
	
	public static GridMethod zeroFlux()
	{
		return constantNeumann(0.0);
	}
	
}

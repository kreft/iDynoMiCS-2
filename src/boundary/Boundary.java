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
public class Boundary
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
	
	public static double calcFlux(double bndryConcn, double gridConcn,
										double diffusivity, double resolution)
	{
		return (bndryConcn - gridConcn) * diffusivity
										* Math.pow(resolution,-2.0);
	}
	
	public static GridMethod constantDirichlet(double value)
	{
		return new GridMethod()
		{
			@Override
			public double getBoundaryFlux(SpatialGrid grid)
			{
				return calcFlux(value, 
								grid.getValueAtCurrent(ArrayType.CONCN),
								grid.getValueAtCurrent(ArrayType.DIFFUSIVITY),
								grid.getResolution());
			}
		};
	}
	
	public static GridMethod constantNeumann(double gradient)
	{
		return new GridMethod()
		{
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

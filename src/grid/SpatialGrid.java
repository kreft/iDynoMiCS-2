package grid;

import java.util.HashMap;

import boundary.Boundary.GridMethod;

public abstract class SpatialGrid
{
	public enum BoundarySide
	{
		TOP, BOTTOM, LEFT, RIGHT, FRONT, BACK
	};
	
	protected HashMap<BoundarySide,GridMethod> _boundaries;
	
	/**
	 * Standard names for SpatialGrid arrays that are used in various places.
	 */
	public static final String concn = "concentration",
								diff = "diffusivity",
								domain = "domain", 
								reac = "reacRate", 
								dReac = "diffReacRate";
	
	public abstract void newArray(String name, double initialValues);
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 */
	public void newArray(String name)
	{
		this.newArray(name, 0.0);
	}
	
	public abstract double getMax(String name);
	
	public abstract double getMin(String name);
	
	public abstract double getValueAt(String name, double[] location);
	
	public void addBoundary(BoundarySide side, GridMethod method)
	{
		this._boundaries.put(side, method);
	}
}

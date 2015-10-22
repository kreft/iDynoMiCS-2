package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;

public abstract class SpatialGrid
{
	public interface GridMethod
	{
		int[] getCorrectCoord(int[] coord);
		
		double getConcnGradient(int[] coord);
	}
	
	public enum ArrayType
	{
		/*
		 * The solute concentration.
		 * TODO Change to VARIABLE to make more general?
		 */
		CONCN, 
		
		DIFFUSIVITY, DOMAIN, 
		
		PRODUCTIONRATE, DIFFPRODUCTIONRATE, LOPERATOR;
	}
	
	protected HashMap<BoundarySide,GridMethod> _boundaries;
	
	public abstract void newArray(ArrayType type, double initialValues);
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 */
	public void newArray(ArrayType type)
	{
		this.newArray(type, 0.0);
	}
	
	public abstract double getMax(ArrayType type);
	
	public abstract double getMin(ArrayType type);
	
	public abstract double getValueAt(ArrayType type, double[] location);
	
	public void addBoundary(BoundarySide side, GridMethod method)
	{
		this._boundaries.put(side, method);
	}
	
	//public abstract ArrayList<BoundarySide> boundariesNextTo(int[] coords);
}

package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;

public abstract class SpatialGrid
{
	public interface GridMethod
	{
		int[] getCorrectCoord(int[] coord);
		
		double getConcnGradient(String sName, SpatialGrid grid);
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
	
	/**
	 * TODO
	 */
	protected HashMap<ArrayType, double[][][]> _array;
	
	/**
	 * TODO
	 */
	protected int[] _nVoxel;
	
	/**
	 * Grid resolution, i.e. the side length of each voxel in this grid.
	 */
	protected double _res;
	
	protected HashMap<BoundarySide,GridMethod> _boundaries = 
									new HashMap<BoundarySide,GridMethod>();
	
	/**
	 * Current coordinate considered by the internal iterator.
	 */
	protected int[] _currentCoord;
	
	/**
	 * Current neighbour coordinate considered by the neighbor iterator.
	 */
	protected int[] _currentNeighbor;
	
	protected boolean _inclDiagonalNhbs;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
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
	
	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/
	
	public abstract double getResolution();
	
	public abstract double getVoxelVolume();
	
	public abstract int[] getNumVoxels();
	
	public abstract boolean[] getSignificantAxes();
	
	public abstract int numSignificantAxes();
	
	public boolean hasArray(ArrayType type)
	{
		return this._array.containsKey(type);
	}
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	/*************************************************************************
	 * COORDINATES
	 ************************************************************************/
	
	public abstract int[] getCoords(double[] location);
	
	public abstract double[] getVoxelOrigin(int[] coords);
	
	public abstract double[] getVoxelCentre(int[] coords);
	
	/*************************************************************************
	 * BOUNDARIES
	 ************************************************************************/
	
	public void addBoundary(BoundarySide side, GridMethod method)
	{
		this._boundaries.put(side, method);
	}
	
	//public abstract ArrayList<BoundarySide> boundariesNextTo(int[] coords);
	
	protected abstract BoundarySide isOutside(int[] coord);
	
	/*************************************************************************
	 * VOXEL GETTERS & SETTERS
	 ************************************************************************/
	
	public abstract double getValueAt(ArrayType type, int[] coord);
	
	public abstract void setValueAt(ArrayType type, int[] gridCoords, double value);
	
	public abstract void addValueAt(ArrayType type, int[] gridCoords, double value);
	
	public abstract void timesValueAt(ArrayType type, int[] gridCoords, double value);
	
	/*************************************************************************
	 * ARRAY SETTERS
	 ************************************************************************/
	
	public abstract void setAllTo(ArrayType type, double value);
	
	public abstract void addToAll(ArrayType type, double value);
	
	public abstract void timesAll(ArrayType type, double value);
	
	/*************************************************************************
	 * ARRAY GETTERS
	 ************************************************************************/
	
	public abstract double getMax(ArrayType type);
	
	public abstract double getMin(ArrayType type);
	
	/*************************************************************************
	 * TWO-ARRAY METHODS
	 ************************************************************************/
	
	public abstract void addArrayToArray(ArrayType destination, ArrayType source);
	
	/*************************************************************************
	 * LOCATION GETTERS
	 ************************************************************************/
	
	/**
	 * 
	 * @param name
	 * @param location
	 * @return
	 */
	public double getValueAt(ArrayType type, double[] location)
	{
		return this.getValueAt(type, this.getCoords(location));
	}
	
	/*************************************************************************
	 * COORDINATE ITERATOR
	 ************************************************************************/
	
	public abstract int[] resetIterator();
	
	public abstract boolean isIteratorValid();
	
	public abstract int[] iteratorNext();
	
	/*************************************************************************
	 * NEIGHBOR ITERATOR
	 ************************************************************************/
	
	public abstract int[] resetNbhIterator(boolean inclDiagonalNbhs);
	
	public abstract boolean isNbhIteratorValid();
	
	public abstract int[] nbhIteratorNext();
	
	public abstract GridMethod nbhIteratorIsOutside();
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public abstract String arrayAsText(ArrayType type);
}

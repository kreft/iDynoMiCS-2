package grid;

import java.util.HashMap;

import idynomics.Compartment.BoundarySide;

public abstract class SpatialGrid
{
	public interface GridGetter
	{
		SpatialGrid newGrid(int[] nVoxel, double resolution);
	};
	
	public interface GridMethod
	{
		double getBoundaryFlux(SpatialGrid grid);
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
		//System.out.println("Adding method to "+side.name()+" boundary"); //bughunt
		this._boundaries.put(side, method);
	}
	
	//public abstract ArrayList<BoundarySide> boundariesNextTo(int[] coords);
	
	protected abstract BoundarySide isOutside(int[] coord);
	
	public abstract int[] cyclicTransform(int[] coord);
	
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
	
	public int[] iteratorCurrent()
	{
		return this._currentCoord;
	}
	
	public abstract int[] iteratorNext();
	
	public abstract double getValueAtCurrent(ArrayType type);
	
	public abstract void setValueAtCurrent(ArrayType type, double value);
	
	/*************************************************************************
	 * NEIGHBOR ITERATOR
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract int[] resetNbhIterator();
	
	public abstract boolean isNbhIteratorValid();
	
	public int[] neighborCurrent()
	{
		return this._currentNeighbor;
	}
	
	public abstract int[] nbhIteratorNext();
	
	public abstract GridMethod nbhIteratorIsOutside();
	
	/**
	 * 
	 * TODO safety if neighbor iterator or arrays are not initialised.
	 * 
	 * @return
	 */
	public double getFluxWithNeighbor(String soluteName)
	{
		GridMethod aMethod = this.nbhIteratorIsOutside();
		if( aMethod == null )
		{
			double out = this.getValueAt(ArrayType.CONCN, this._currentNeighbor)
					- this.getValueAtCurrent(ArrayType.CONCN);
			out *= this.getValueAtCurrent(ArrayType.DIFFUSIVITY)
			  + this.getValueAt(ArrayType.DIFFUSIVITY, this._currentNeighbor);
			/*
			 * Here we assume that all voxels are the same size.
			 */
			out *= 0.5 * Math.pow(this.getResolution(), -2.0);
			//System.out.println("normal: "+out); //bughunt
			return out;
		}
		else
		{
			double flux = aMethod.getBoundaryFlux(this);
			//System.out.println("method: "+flux); //bughunt
			return flux;
		}
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public abstract String arrayAsText(ArrayType type);
}

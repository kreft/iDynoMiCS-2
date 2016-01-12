package grid;

import java.util.HashMap;

import grid.GridBoundary.GridMethod;
import idynomics.Compartment.BoundarySide;

/**
 * \brief A SpatialGrid stores information about a variable over space.
 * 
 * <p>A typical example is the concentration of a dissolved compound, often
 * referred to as a solute. A spatial grid describing the concentration of a
 * solute may also describe the diffusivity of this solute, as well as any
 * other necessary information about it that varies in space. Each type of
 * information, including concentration, is kept in a separate array. All
 * these arrays have identical dimensions and resolutions, so that voxels
 * overlap exactly.</p>
 * 
 * <p>Since all the arrays in a SpatialGrid line up, it is possible to iterate
 * over all voxels in a straightforward manner. On top of this, we can also
 * iterate over all neighbouring voxels of the voxel the main iterator is
 * currently focused on.</p>
 * 
 * <p>On the boundaries of the grid, </p>
 * 
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public abstract class SpatialGrid
{
	/**
	 * Simple interface for getting a particular type of grid, i.e. a subclass
	 * of SpatialGrid. This will typically depend on the shape of the
	 * compartment it belongs to.
	 */
	public interface GridGetter
	{
		SpatialGrid newGrid(int[] nVoxel, double resolution);
	};
	
	/**
	 * Label for an array. 
	 */
	public enum ArrayType
	{
		/**
		 * The concentration of, e.g., a solute.
		 */
		CONCN, 
		
		/**
		 * The diffusion coefficient of a solute. For example, this may be
		 * lower inside a biofilm than in the surrounding water.
		 */
		DIFFUSIVITY,
		
		/**
		 * The domain dictates where the diffusion is actually happening. For
		 * example, when modelling a biofilm it may be assumed that liquid
		 * outside the boundary layer is well-mixed.
		 */
		DOMAIN, 
		
		/**
		 * The rate of production of this solute. Consumption is described by
		 * negative production.
		 */
		PRODUCTIONRATE,
		
		/**
		 * The differential of production rate with respect to its
		 * concentration.
		 */
		DIFFPRODUCTIONRATE,
		
		/**
		 * Laplacian operator.
		 */
		LOPERATOR;
	}
	
	/**
	 * Dictionary of arrays according to their type. Note that not all types
	 * may be occupied.
	 */
	protected HashMap<ArrayType, double[][][]> _array;
	
	/**
	 * The number of voxels this grid has in each of the three spatial 
	 * dimensions. Note that some of these may be 1 if the grid is not three-
	 * dimensional.
	 * 
	 * <p>For example, a 3 by 2 rectangle would have _nVoxel = [3, 2, 1].</p> 
	 */
	protected int[] _nVoxel;
	
	/**
	 * Grid resolution, i.e. the side length of each voxel in this grid. This
	 * has three rows, one for each dimension. Each row has length of its
	 * corresponding position in _nVoxel.
	 * 
	 * <p>For example, a 3 by 2 rectangle might have _res = 
	 * [[1.0, 1.0, 1.0], [1.0, 1.0], [1.0]]</p>
	 */
	protected double[][] _res;
	
	/**
	 * Smallest distance between the centres of two neighbouring voxels in
	 * this grid. 
	 */
	protected double _minVoxVoxDist;
	
	/**
	 * Smallest shared surface area between two neighbouring voxels in this
	 * grid. 
	 */
	protected double _minVoxVoxSurfArea;
	
	/**
	 * Smallest volume of a voxel in this grid.
	 */
	protected double _minVoxelVolume;
	
	/**
	 * Dictionary of methods to use on this grid when solving partial
	 * differential equations (PDEs).  
	 */
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
	
	public abstract void calcMinVoxVoxResSq();
	
	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/
	
	public abstract int[] getNumVoxels();
	
	public abstract boolean[] getSignificantAxes();
	
	public abstract int numSignificantAxes();
	
	public boolean hasArray(ArrayType type)
	{
		return this._array.containsKey(type);
	}
	
	public double getMinVoxelVoxelSurfaceArea()
	{
		return this._minVoxVoxSurfArea;
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO This needs some serious looking into! 
	 * 
	 * @return
	 */
	public double getMinVoxVoxResSq()
	{
		if ( this._minVoxVoxDist == 0.0 )
			this.calcMinVoxVoxResSq();
		return this._minVoxVoxDist;
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
	
	protected abstract BoundarySide isOutside(int[] coord);
	
	public abstract int[] cyclicTransform(int[] coord);
	
	/*************************************************************************
	 * VOXEL GETTERS & SETTERS
	 ************************************************************************/
	
	public abstract double getVoxelVolume(int[] coord);
	
	public abstract double getValueAt(ArrayType type, int[] coord);
	
	public abstract void setValueAt(ArrayType type, int[] coord, double value);
	
	public abstract void addValueAt(ArrayType type, int[] coord, double value);
	
	public abstract void timesValueAt(ArrayType type, int[] coord, double value);
	
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
	
	public abstract double getNbhSharedSurfaceArea();
	
	public abstract double getCurrentNbhResSq();
	
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
			/*
			 * First find the difference in concentration.
			 */
			double out = this.getValueAt(ArrayType.CONCN, this._currentNeighbor)
					- this.getValueAtCurrent(ArrayType.CONCN);
			/*
			 * Then multiply this by the average diffusivity.
			 */
			out *= 0.5 * (this.getValueAtCurrent(ArrayType.DIFFUSIVITY) +
			   this.getValueAt(ArrayType.DIFFUSIVITY, this._currentNeighbor));
			/*
			 * Finally, multiply by the surface are the two voxels share (in
			 * square microns).
			 */
			out /= this.getNbhSharedSurfaceArea();
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

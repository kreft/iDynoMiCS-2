package grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import dataIO.ObjectFactory;
import grid.GridBoundary.GridMethod;
import grid.resolution.ResolutionCalculator.ResCalc;
import grid.subgrid.SubgridPoint;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import shape.ShapeConventions.CyclicGrid;
import shape.ShapeConventions.DimName;

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
		SpatialGrid newGrid(double[] totalLength, Node node);
		
		// TODO
		//SpatialGrid newGrid(ResCalc[] resolutionCalculator);
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
		 * A measure of how well-mixed a solute is. A diffusion-reaction should
		 * ignore where this is above a certain threshold.   
		 */
		WELLMIXED,
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
	protected HashMap<ArrayType, double[][][]> _array
									= new HashMap<ArrayType, double[][][]>();
	
	/**
	 * Array of the names of each dimension. For example, a Cartesian grid has
	 * (X, Y, Z).
	 */
	protected DimName[] _dimName = new DimName[3];
	
	protected int[] _currentNVoxel = new int[3];
	
	/**
	 * Array of the boundaries at each dimension's extremes. The three rows
	 * correspond to the dimension names in {@link #_dimName}, and the two
	 * elements in each row correspond to the minimum (0) and maximum (1)
	 * extremes of each dimension.
	 */
	protected GridMethod[][] _dimBoundaries = new GridMethod[3][2];
	
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
	 * Current coordinate considered by the internal iterator.
	 */
	protected int[] _currentCoord;
	
	/**
	 * Current neighbour coordinate considered by the neighbor iterator.
	 */
	protected int[] _currentNeighbor;
	
	/**
	 * Whether the neighbor iterator is currently valid (true) or invalid
	 * (false).
	 */
	protected boolean _nbhValid;
	
	/**
	 * A helper vector for finding the location of the origin of a voxel.
	 */
	protected final double[] VOXEL_ORIGIN_HELPER = Vector.vector(3, 0.0);
	/**
	 * A helper vector for finding the location of the centre of a voxel.
	 */
	protected final double[] VOXEL_CENTRE_HELPER = Vector.vector(3, 0.5);
	/**
	 * A helper vector for finding the 'upper most' location of a voxel.
	 */
	protected final double[] VOXEL_All_ONE_HELPER = Vector.vector(3, 1.0);
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Initialise an array of the given <b>type</b> and fill all voxels
	 * with <b>initialValues</b>.
	 * 
	 * @param type {@code ArrayType} for the new array.
	 * @param initialValues {@code double} for every voxel to take.
	 */
	public abstract void newArray(ArrayType type, double initialValues);
	
	/**
	 * \brief Initialise an array of the given <b>type</b> and fill it with
	 * zeros.
	 * 
	 * @param type {@code ArrayType} for the new array.
	 */
	public void newArray(ArrayType type)
	{
		this.newArray(type, 0.0);
	}
	
	/**
	 * \brief Calculate the smallest centre-centre distance between neighboring
	 * voxels in this grid, and store the result in {@link #_minVoxVoxDist}.
	 */
	public abstract void calcMinVoxVoxResSq();
	
	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Get the list of dimension names for this grid.
	 * 
	 * @return Vector of dimension names that will have 3 elements.
	 * @see #indexFor(DimName)
	 */
	public DimName[] getDimensionNames()
	{
		return this._dimName;
	}
	
	/**
	 * \brief Find the index of the given dimension name.
	 * 
	 * @param dim Name of the dimension being sought.
	 * @return {@code int} taking value of 0, 1, 2 (recognised names) or -1
	 * (unrecognised name).
	 * @see #getDimensionNames()
	 */
	public int indexFor(DimName dim)
	{
		for ( int i = 0; i < 3; i++ )
			if ( dim == this._dimName[i] )
				return i;
		return -1;
	}
	
	/**
	 * \brief Get a list of which axes are significant.
	 * 
	 * @return {@code boolean} array of which axes are significant (true) or
	 * insignificant (false).
	 * @see #numSignificantAxes()
	 */
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		for ( int dim = 0; dim < 3; dim++ )
		{
			out[dim] = this.isBoundaryDefined(dim, 0) || 
												this.isBoundaryDefined(dim, 1);
		}
		return out;
	}
	
	/**
	 * \brief Count how many axes are significant.
	 * 
	 * @return Number of significant axes. Will be between 0 and 3 (inclusive).
	 * @see #getSignificantAxes()
	 */
	public int numSignificantAxes()
	{
		int out = 0;
		for ( int dim = 0; dim < 3; dim++ )
			if ( this.isBoundaryDefined(dim, 0) || 
											this.isBoundaryDefined(dim, 1) )
			{
				out++;
			}
		return out;
	}
	
	/**
	 * \brief Whether this grid has an array of the type specified.
	 * 
	 * @param type Type of array sought (e.g. CONCN).
	 * @return {@code true} if this array is already initialised in this grid,
	 * {@code false} otherwise.
	 */
	public boolean hasArray(ArrayType type)
	{
		return this._array.containsKey(type);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
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
	 * COORDINATES
	 ************************************************************************/
	
	/**
	 * \brief Find the coordinates of the voxel that encloses the given
	 * <b>location</b>.
	 * 
	 * @param location Continuous location within the shape.
	 * @return Discrete coordinates within this grid.
	 */
	public int[] getCoords(double[] loc)
	{
		return getCoords(loc, null);
	}
	
	/**
	 * \brief Transforms a given location into array-coordinates and 
	 * computes sub-coordinates inside the grid element if inside != null. 
	 * 
	 * @param loc - a location in simulated space.
	 * @param inside - array to write sub-coordinates into, can be null.
	 * @return - the array coordinates corresponding to location loc.
	 */
	public int[] getCoords(double[] loc, double[] inside)
	{
		int[] coord = new int[3];
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			coord[dim] = rC.getVoxelIndex(loc[dim]);
			if ( inside != null )
			{
				inside[dim] = loc[dim] - 
								rC.getCumulativeResolution(coord[dim] - 1);
			}
		}
		return coord;
	}
	
	/**
	 * \brief Converts a coordinate in the grid's array to a location in simulated 
	 * space. 
	 * 
	 * 'Subcoordinates' can be transformed using the 'inside' array.
	 * For example type getLocation(coord, new double[]{0.5,0.5,0.5})
	 * to get the center point of the grid cell defined by 'coord'.
	 * 
	 * @param coord - a coordinate in the grid's array.
	 * @param inside - relative position inside the grid cell.
	 * @return - the location in simulation space.
	 */
	public double[] getLocation(int[] coord, double[] inside)
	{
		double[] loc = Vector.copy(inside);
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			loc[dim] *= rC.getResolution(coord[dim]);
			loc[dim] += rC.getCumulativeResolution(coord[dim] - 1);
		}
		return loc;
	}
	
	/**
	 * \brief Find the location of the lower corner of the voxel specified by
	 * the given coordinates.
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return Continuous location of the lower corner of this voxel.
	 */
	public double[] getVoxelOrigin(int[] coord)
	{
		return getLocation(coord, VOXEL_ORIGIN_HELPER);
	}
	
	/**
	 * \brief Find the location of the centre of the voxel specified by the
	 * given coordinates.
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return Continuous location of the centre of this voxel.
	 */
	public double[] getVoxelCentre(int[] coord)
	{
		return getLocation(coord, VOXEL_CENTRE_HELPER);
	}
	
	/**
	 * \brief Get the corner farthest from the origin of the voxel specified. 
	 * 
	 * @param coord
	 * @return
	 */
	protected double[] getVoxelUpperCorner(int[] coord)
	{
		return getLocation(coord, VOXEL_All_ONE_HELPER);
	}
	
	/**
	 * \brief Get the side lengths of the voxel given by the <b>coord</b>.
	 * Write the result into <b>destination</b>.
	 * 
	 * @param destination
	 * @param coord
	 */
	public void getVoxelSideLengthsTo(double[] destination, int[] coord)
	{
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			destination[dim] = rC.getResolution(coord[dim]);
		}
	}
	
	/**
	 * \brief Get the number of voxels in each dimension for the current
	 * coordinates.
	 * 
	 * <p>For {@code CartesianGrid} the value of <b>coords</b> will be
	 * irrelevant, but it will make a difference in the polar grids.</p>
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return A 3-vector of the number of voxels in each dimension.
	 */
	public int[] updateCurrentNVoxel()
	{
		return getNVoxel(this._currentCoord, this._currentNVoxel);
	}
	
	public int[] getCurrentNVoxel()
	{
		return this._currentNVoxel;
	}
	
	public int[] getNVoxel(int[] coords)
	{
		return getNVoxel(coords, null);
	}
	
	/**
	 * //TODO: make permanent vector in the grids.
	 * @param dim - a dimension index.
	 * @return The total length of the grid along the given dimension.
	 */
	public abstract double getTotalLength(int dim);
	
	public double[] getTotalLength(){
		//TODO: make permanent vector in the grids.
		return new double[]{
				getTotalLength(0), 
				getTotalLength(1), 
				getTotalLength(2)};
	}
	
	/**
	 * \brief Get the number of voxels in each dimension for the given
	 * coordinates.
	 * 
	 * <p>For {@code CartesianGrid} the value of <b>coords</b> will be
	 * irrelevant, but it will make a difference in the polar grids.</p>
	 * 
	 * @param coords Discrete coordinates of a voxel on this grid.
	 * @return A 3-vector of the number of voxels in each dimension.
	 */
	protected abstract int[] getNVoxel(int[] coords, int[] outNVoxel);
	
	/**
	 * \brief TODO
	 * 
	 * @param coord
	 * @param axis
	 * @return
	 */
	protected abstract ResCalc getResolutionCalculator(int[] coord, int axis);
	
	/*************************************************************************
	 * BOUNDARIES
	 ************************************************************************/
	
	/**
	 * \brief Tell this grid what to do at a boundary.
	 * 
	 * @param dim The name of the dimension.
	 * @param index The index of the extreme: 0 for the minimum extreme, 1 for
	 * the maximum extreme.
	 * @param method The grid method to use at this boundary.
	 */
	public void addBoundary(DimName dim, int index, GridMethod method)
	{
		int dimIndex = indexFor(dim);
		if ( dimIndex == -1 )
		{
			// TODO safety
		}
		else if ( index < 0 || index > 1 )
		{
			// TODO safety
		}
		else
		{
			this._dimBoundaries[dimIndex][index] = method;
		}
	}
	
	/**
	 * \brief Checks if the given boundary is defined, i.e. not {@code null}.
	 * 
	 * @param dim TODO
	 * @param index
	 * @return
	 */
	public boolean isBoundaryDefined(int dim, int index)
	{
		return this._dimBoundaries[dim][index] != null;
	}
	
	/**
	 * \brief Checks if the given boundary is cyclic.
	 * 
	 * TODO We shouldn't need index here, but check this.
	 * 
	 * @param dim TODO
	 * @param index
	 * @return
	 */
	public boolean isBoundaryCyclic(int dim, int index)
	{
		return this._dimBoundaries[dim][index] instanceof CyclicGrid;
	}
	
	/**
	 * \brief Check if a given coordinate belongs in this grid. If it is not, 
	 * return the {@code GridMethod} that should be used.
	 * 
	 * @param coord Discrete coordinates of a voxel on this grid.
	 * @return A @{@code GridMethod} to use if the coordinates are outside this
	 * grid. {@code null} if the coordinates are inside.
	 */ 
	protected GridMethod isOutside(int[] coord)
	{
		int[] nVoxel = this.getNVoxel(coord);
		GridMethod out = null;
		int c, n;
		/*
		 * For each dimension, check if the coordinate lies outside the grid.
		 * If the boundary there is not null, return it.
		 */
		for ( int dim = 0; dim < 3; dim++ )
		{
			c = coord[dim];
			if ( c < 0 && (out = this._dimBoundaries[dim][0]) != null)
				break;
			n = nVoxel[dim];
			if ( c >= n && (out = this._dimBoundaries[dim][1]) != null)
				break;
		}
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * TODO this should really only be done for cyclic dimensions
	 * 
	 * TODO Rob [16Nov2015]: This is far from ideal, but I can't currently see
	 * a better way of doing it.
	 * 
	 * @param coord Discrete coordinates of a voxel on this grid.
	 * @return New 3-vector to use instead.
	 */
	public int[] cyclicTransform(int[] coord)
	{
		int[] transformed = new int[3];
		for ( int dim = 0; dim < 3; dim++ )
			this.cyclicTransfom(transformed, coord, dim);
		return transformed;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param destination
	 * @param coord
	 * @param dim
	 */
	protected void cyclicTransfom(int[] destination, int[] coord, int dim)
	{
		int nVoxel = this.getResolutionCalculator(coord, dim).getNVoxel();
		destination[dim] = Math.floorMod(coord[dim], nVoxel);
	}
	
	/*************************************************************************
	 * VOXEL GETTERS & SETTERS
	 ************************************************************************/
	
	/**
	 * \brief Calculate the volume of the voxel specified by the given
	 * coordinates.
	 * 
	 * @param coord Discrete coordinates of a voxel on this grid.
	 * @return Volume of this voxel.
	 */
	public abstract double getVoxelVolume(int[] coord);
	
	/**
	 * \brief Gets the value of one coordinate on the given array type.
	 * 
	 * @param type Type of array to get from.
	 * @param coord Coordinate on this array to get.
	 * @return double value at this coordinate on this array.
	 */
	public double getValueAt(ArrayType type, int[] coord)
	{
		if ( this._array.containsKey(type) )
			return this._array.get(type)[coord[0]][coord[1]][coord[2]];
		else
			return Double.NaN;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param coord
	 * @param value
	 */
	public void setValueAt(ArrayType type, int[] coord, double value)
	{
		if ( this._array.containsKey(type) )
			this._array.get(type)[coord[0]][coord[1]][coord[2]] = value;
		// TODO safety?
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param coord
	 * @param value
	 */
	public void addValueAt(ArrayType type, int[] coord, double value)
	{
		if ( this._array.containsKey(type) )
			this._array.get(type)[coord[0]][coord[1]][coord[2]] += value;
		// TODO safety?
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param coord
	 * @param value
	 */
	public void timesValueAt(ArrayType type, int[] coord, double value)
	{
		if ( this._array.containsKey(type) )
			this._array.get(type)[coord[0]][coord[1]][coord[2]] *= value;
		// TODO safety?
	}
	
	/*************************************************************************
	 * ARRAY SETTERS
	 ************************************************************************/
	
	/**
	 * \brief Set all values in the array specified to the <b>value</b> given.
	 * 
	 * @param type Type of the array to set.
	 * @param value New value for all elements of this array.
	 */
	public void setAllTo(ArrayType type, double value)
	{
		Array.setAll(this._array.get(type), value);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param array
	 */
	public void setTo(ArrayType type, double[][][] array)
	{
		Array.setAll(this._array.get(type), array);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 */
	public void makeNonnegative(ArrayType type)
	{
		Array.makeNonnegative(this._array.get(type));
	}
	
	/**
	 * \brief Increase all values in the array specified by the <b>value</b>
	 * given.
	 * 
	 * <p>To decrease all elements of this array (i.e. subtract), simply use
	 * {@code addToAll(type, -value)}.</p>
	 * 
	 * @param type Type of the array to use.
	 * @param value New value to add to all elements of this array.
	 */
	public void addToAll(ArrayType type, double value)
	{
		Array.add(this._array.get(type), value);
	}
	
	/**
	 * \brief Multiply all values in the array specified by the <b>value</b>
	 * given.
	 * 
	 * <p>To divide all elements of this array, simply use
	 * {@code timesAll(type, 1.0/value)}.</p>
	 * 
	 * @param type Type of the array to use.
	 * @param value New value with which to multiply all elements of this array.
	 */
	public void timesAll(ArrayType type, double value)
	{
		Array.times(this._array.get(type), value);
	}
	
	/*************************************************************************
	 * ARRAY GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Get the greatest value in the given array.
	 * 
	 * @param type Type of the array to use.
	 * @return Greatest value of all the elements of the array <b>type</b>.
	 */
	public double getMax(ArrayType type)
	{
		return Array.max(this._array.get(type));
	}
	
	/**
	 * \brief Get the least value in the given array.
	 * 
	 * @param type Type of the array to use.
	 * @return Least value of all the elements of the array <b>type</b>.
	 */
	public double getMin(ArrayType type)
	{
		return Array.min(this._array.get(type));
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @return
	 */
	public double getAverage(ArrayType type)
	{
		return Array.meanArith(this._array.get(type));
	}
	
	/*************************************************************************
	 * TWO-ARRAY METHODS
	 ************************************************************************/
	
	public void addArrayToArray(ArrayType destination, ArrayType source)
	{
		Array.add(this._array.get(destination), this._array.get(source));
	}
	
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
	
	/**
	 * \brief Return the coordinate iterator to its initial state.
	 * 
	 * @return The value of the coordinate iterator.
	 */
	public int[] resetIterator()
	{
		if ( this._currentCoord == null )
			this._currentCoord = Vector.zerosInt(3);
		else
			Vector.reset(this._currentCoord);
		return this._currentCoord;
	}
	
	/**
	 * \brief Determine whether the current coordinate of the iterator is
	 * outside the grid in the dimension specified.
	 * 
	 * @param axis Index of the dimension to look at.
	 * @return Whether the coordinate iterator is inside (false) or outside
	 * (true) the grid along this dimension.
	 */
	protected boolean iteratorExceeds(int axis)
	{
		return this._currentCoord[axis] >= this._currentNVoxel[axis];
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public boolean isIteratorValid()
	{
		for ( int axis = 0; axis < 3; axis++ )
			if ( this._currentCoord[axis] >= this._currentNVoxel[axis] )
				return false;
		return true;
	}
	
	/**
	 * \brief Get the current state of the coordinate iterator.
	 * 
	 * @return The value of the coordinate iterator.
	 */
	public int[] iteratorCurrent()
	{
		return this._currentCoord;
	}
	
	/**
	 * \brief Step the coordinate iterator forward once.
	 * 
	 * @return The new value of the coordinate iterator.
	 */
	public int[] iteratorNext()
	{
		/*
		 * We have to step through last dimension first, because we use jagged 
		 * arrays in the PolarGrids.
		 */
		_currentCoord[2]++;
		if ( this.iteratorExceeds(2) )
		{
			_currentCoord[2] = 0;
			_currentCoord[1]++;
			if ( this.iteratorExceeds(1) )
			{
				_currentCoord[1] = 0;
				_currentCoord[0]++;
			}
		}
		return _currentCoord;
	}
	
	/**
	 * \brief Get the value of the given array in the 
	 * 
	 * @param type
	 * @return
	 */
	public double getValueAtCurrent(ArrayType type)
	{
		return this.getValueAt(type, this.iteratorCurrent());
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param value
	 */
	public void setValueAtCurrent(ArrayType type, double value)
	{
		this.setValueAt(type, this.iteratorCurrent(), value);
	}
	
	/**
	 * return all voxel coordinates for this solute grid.
	 * @return
	 */
	public LinkedList<int[]> getAllCoords()
	{
		LinkedList<int[]> coords = new LinkedList<int[]>();
		for ( int[] coord = resetIterator(); 
				isIteratorValid(); 
					coord = iteratorNext())
		{
			coords.add((int[]) ObjectFactory.copy(coord));
		}
		return coords;
	}
	
	/*************************************************************************
	 * NEIGHBOR ITERATOR
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract int[] resetNbhIterator();
	
	/**
	 * \brief Check if the neighbor iterator takes a valid coordinate.
	 * 
	 * @return {@code boolean true} if it is valid, {@code false} if it is not.
	 */
	public boolean isNbhIteratorValid()
	{
		return this._nbhValid;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public int[] neighborCurrent()
	{
		return this._currentNeighbor;
	}
	
	/**
	 * \brief Move the neighbor iterator to the current coordinate, 
	 * and make the index at <b>dim</b> one less.
	 * 
	 * @return {@code boolean} reporting whether this is valid.
	 */
	protected boolean moveNbhToMinus(int dim)
	{
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[dim]--;
		
		return (this._currentNeighbor[dim] >= 0) || this.isBoundaryDefined(dim, 0);
	}
	
	/**
	 * \brief Try to increase the neighbor iterator from the minus-side of the
	 * current coordinate to the plus-side.
	 * 
	 * <p>For use on linear dimensions (X, Y, Z, R) and not on angular ones
	 * (THETA, PHI).</p>
	 * 
	 * @param dim Index of the dimension to move in.
	 * @return Whether the increase was successful (true) or a failure (false).
	 */
	protected boolean nbhJumpOverCurrent(int dim)
	{
		if ( this._currentNeighbor[dim] < this._currentCoord[dim] )
		{
			ResCalc rC = this.getResolutionCalculator(
												   this._currentNeighbor, dim);
			if ( this._currentCoord[dim] < rC.getNVoxel() - 1 || 
											this.isBoundaryDefined(dim, 1) )
			{
				this._currentNeighbor[dim] = this._currentCoord[dim] + 1;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract int[] nbhIteratorNext();
	
	public abstract double getNbhSharedSurfaceArea();
	
	//public abstract double getCurrentNbhResSq();
	
	public GridMethod nbhIteratorIsOutside()
	{
		return this.isOutside(this._currentNeighbor);
	}
	
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
			out *= meanDiffusivity(
				this.getValueAtCurrent(ArrayType.DIFFUSIVITY),
				this.getValueAt(ArrayType.DIFFUSIVITY, this._currentNeighbor));
			/*
			 * Finally, multiply by the surface are the two voxels share (in
			 * square microns).
			 */
			// TODO Rob: I need to change this
			out /= this.getNbhSharedSurfaceArea();
			return out;
		}
		else if ( aMethod instanceof CyclicGrid )
		{
			/*
			 * Use the cyclic-transformed voxel coordinate for 
			 */
			int[] cyclicNbh = this.cyclicTransform(this._currentNeighbor);
			/*
			 * First find the difference in concentration.
			 */
			double out = this.getValueAt(ArrayType.CONCN, cyclicNbh)
					- this.getValueAtCurrent(ArrayType.CONCN);
			/*
			 * Then multiply this by the average diffusivity.
			 */
			out *= meanDiffusivity(
							this.getValueAtCurrent(ArrayType.DIFFUSIVITY),
							this.getValueAt(ArrayType.DIFFUSIVITY, cyclicNbh));
			/*
			 * Finally, multiply by the surface are the two voxels share (in
			 * square microns).
			 * 
			 * Note that we use the ghost coordinate outside the grid for this.
			 */
			// TODO Rob: I need to change this
			out /= this.getNbhSharedSurfaceArea();
			return out;
		}
		else
		{
			double flux = aMethod.getBoundaryFlux(this);
			//System.out.println("method: "+flux); //bughunt
			return flux;
		}
	}
	
	private static double meanDiffusivity(double a, double b)
	{
		if ( a == 0.0 || b == 0.0 )
			return 0.0;
		if ( a == Double.POSITIVE_INFINITY )
			return b;
		if ( b == Double.POSITIVE_INFINITY )
			return a;
		/*
		 * This is a computationally nicer way of getting the harmonic mean:
		 * 2 / ( (1/a) + (1/b))
		 */
		return 2.0 * ( a * b ) / ( a + b );
	}
	
	/*************************************************************************
	 * SUBGRID POINTS
	 ************************************************************************/
	
	/**
	 * \brief List of sub-grid points at the current coordinate.
	 * 
	 * <p>Useful for distributing agent-mediated reactions over the grid.</p>
	 * 
	 * @param targetRes
	 * @return
	 */
	public List<SubgridPoint> getCurrentSubgridPoints(double targetRes)
	{
		/* 
		 * Initialise the list and add a point at the origin.
		 */
		ArrayList<SubgridPoint> out = new ArrayList<SubgridPoint>();
		SubgridPoint current = new SubgridPoint();
		out.add(current);
		/*
		 * For each dimension, work out how many new points are needed and get
		 * these for each point already in the list.
		 */
		int nP, nCurrent;
		ResCalc rC;
		for ( int dim = 0; dim < 3; dim++ )
		{
			// TODO Rob[17Feb2016]: This will need improving for polar grids...
			// I think maybe would should introduce a subclass of Dimension for
			// angular dimensions.
			rC = this.getResolutionCalculator(this._currentCoord, dim);
			nP = (int) (rC.getResolution(this._currentCoord[dim])/targetRes);
			nCurrent = out.size();
			for ( int j = 0; j < nCurrent; j++ )
			{
				current = out.get(j);
				/* Shift this point up by half a sub-resolution. */
				current.internalLocation[dim] += (0.5/nP);
				/* Now add extra points at sub-resolution distances. */
				for ( double i = 1.0; i < nP; i++ )
					out.add(current.getNeighbor(dim, i/nP));
			}
		}
		/* Now find the real locations and scale the volumes. */
		// TODO this probably needs to be slightly different in polar grids
		// to be completely accurate
		double volume = this.getVoxelVolume(this._currentCoord) / out.size();
		for ( SubgridPoint aSgP : out )
		{
			aSgP.realLocation = this.getLocation(this._currentCoord,
													aSgP.internalLocation);
			aSgP.volume = volume;
		}
		return out;
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public abstract String arrayAsText(ArrayType type);
}

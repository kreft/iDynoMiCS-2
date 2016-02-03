package grid;

import java.util.ArrayList;
import java.util.function.DoubleFunction;

import boundary.BoundaryCyclic;
import dataIO.LogFile;
import grid.GridBoundary.GridMethod;
import grid.ResolutionCalculator.ResCalc;
import linearAlgebra.Array;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;

/**
 * \brief Abstract super class of all polar grids (Cylindrical and Spherical).
 * 
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public abstract class PolarGrid extends SpatialGrid
{
	/**
	 * The starting point for the r-axis. Default value is zero, and may never
	 * be negative.
	 */
	protected double _rMin = 0.0;
	/**
	 * Current index of iterator.
	 */
	protected int _nbhIdx;
	/**
	 * Current index of neighborhood iterator.
	 */
	protected int _subNbhIdx;
	/**
	 * A set to store (maybe multiple) neighbors for the current neighbor
	 * direction. It will have size one in r and z dimensions and 
	 * 1 <= size <= 3 in azimuthal dimension. The iterators next() function will
	 * iterate over this set while it has more elements or (re) populate it if 
	 * it is empty and the iterator is valid.
	 */
	protected ArrayList<int[]> _subNbhSet;	
	protected ArrayList<Double> _subNbhSharedAreaSet;	
	
	protected double _currentNbhSharedSufaceArea;
	
	/**
	 * Total size in each dimension
	 */
	protected double[] _radSize;
	/**
	 * Factor scaling polar dimensions to have one grid cell per 90? 
	 * (4 grid cells for a full circle) for 0 <= radius < 1
	 */
	protected double[] _ires;
	/**
	 * Predefined array of relative neighbor directions of a grid coordinate.
	 */
	protected final int[][] NBH_DIRECS = new int[][] {
		{0,0,1}, {0,0,-1},{0,1,0}, {0,-1,0}, {-1,-1,0}, {1,1,0}
	};
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
	 * \brief Construct a PolarGrid from a 3-vector of total dimension
	 * sizes. 
	 * 
	 * Note that resolution determination must be handled by the sub-classes!
	 * 
	 * @param totalSize
	 * @param resCalc
	 */
	public PolarGrid(double[] totalSize)
	{
		/* Polar grids always start with an R dimension. */
		this._dimName[0] = DimName.R;
		/*
		 * Initialize members
		 */
		_ires = Vector.vector(3, -1.0);
		_radSize = Vector.vector(3, -1.0);
		_subNbhSet = new ArrayList<int[]>();
		_subNbhSharedAreaSet = new ArrayList<Double>();
		
		/*
		 * Set up members
		 */
		_nbhIdx = 0;
		_subNbhIdx = 0;
		_radSize[1] = Math.toRadians(totalSize[1]%361);
		_ires[1] = PolarArray.ires(_radSize[1]);
	}
	
	/**
	 * \brief updates the current neighbor coordinate.
	 * 
	 * Called when the neighborhood iterator was manipulated.
	 */
	public void currentNbhIdxChanged()
	{
		_subNbhIdx=0;
		_subNbhSet.clear();
		_subNbhSharedAreaSet.clear();
		fillNbhSet();
//		if (_subNbhSet.isEmpty())
//			nbhIteratorNext();
		_currentNeighbor = _subNbhSet.get(0);
		_currentNbhSharedSufaceArea = _subNbhSharedAreaSet.get(0);
	}
	
	/**
	 * \brief Populates the <b>_subNbhSet</b> for the current <b>NBH_DIREC</b>.
	 * 
	 * Called when the current neighborhood index changed, 
	 * which means that <b>NBH_DIREC</b> changed, too.
	 */
	public abstract void fillNbhSet();
	
	@Override
	public int[] resetNbhIterator()
	{
		_nbhIdx=0;
		currentNbhIdxChanged();
		_currentNeighbor = transInternal(_currentNeighbor);
		return _currentNeighbor;
	}
	
	@Override
	public boolean isNbhIteratorValid(){
		if (_subNbhIdx >= _subNbhSet.size()){
			return _nbhIdx < NBH_DIRECS.length - 1;
		}
		return true;
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		this._subNbhIdx++;
		/*
		 * Iterate through _subNbhSet first
		 */
		if ( this._subNbhIdx < this._subNbhSet.size() )
		{ 
			_currentNeighbor = _subNbhSet.get(_subNbhIdx);
			_currentNbhSharedSufaceArea = _subNbhSharedAreaSet.get(_subNbhIdx);
		}
		else
		{
			/*
			 * If _subNbhSet has no more elements step into next 
			 * (orthogonal) direction and (re-)populate the _subNbhSet.
			 */
			this._nbhIdx++;
			if ( this._nbhIdx < NBH_DIRECS.length )
				currentNbhIdxChanged();
		}
		/*
		 * Transform internal boundaries with radius >= 0 automatically (cyclic)
		 */
		_currentNeighbor = transInternal(_currentNeighbor);
		return _currentNeighbor;
	}
	
	@Override
	public double getNbhSharedSurfaceArea()
	{
		return _currentNbhSharedSufaceArea;
	}
	
	/**
	 * \brief Computes isOutside for all 3 dimensions. 
	 * 
	 * Does return minimum OR maximum, not both. 
	 * Decision for min or max if the coord is both min and max 
	 * depends on the actual implementation of isOutside in the sub class.
	 * 
	 * @param coord - an array coordinate.
	 * @param out - Array of length 3 to write BoundarySides into, can be null.
	 * @return - BoundarySides at all 3 dimensions 
	 *           (array of 3 nulls if no boundary was hit at all)
	 */
	protected BoundarySide[] getBoundarySides(int[]coord, BoundarySide[] out)
	{
		if ( out == null )
			out = new BoundarySide[3];
		for ( int dim=0; dim < 3; dim++ )
			out[dim] = isOutside(coord, dim);
		return out;
	}
	
	/**
	 * \brief Performs cyclic transform for inside boundaries.
	 * 
	 * @param coord - A coordinate
	 * @return - The cyclic transformed coordinate.
	 */
	protected int[] transInternal(int[] coord)
	{
		BoundarySide[] bsa = new BoundarySide[3];
		int nc=0, ic=0; // null counter, internal counter
		bsa = getBoundarySides(coord, bsa);
		for (BoundarySide bs : bsa)
		{
			if ( bs == null )
				nc++;
			if ( bs == BoundarySide.INTERNAL )
				ic++;
		}
		if ( nc + ic == 3 )
		{
			// only null and internal -> transform
			coord = cyclicTransform(coord);
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
	public abstract double[] getLocation(int[] coord, double[] inside);
	
	@Override
	public double[] getVoxelOrigin(int[] coord)
	{
		return getLocation(coord, VOXEL_ORIGIN_HELPER);
	}
	
	@Override
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
	
	@Override
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
	public abstract int[] getCoords(double[] loc, double[] inside);
	
	/**************************************************************************/
	/************************* UTILITY METHODS ********************************/
	/**************************************************************************/
	
	/**
	 * \brief Computes a factor that scales the number of elements for
	 * increasing  radius to keep element volume fairly constant.
	 * 
	 * @param radiusIndex - radius.
	 * @return - a scaling factor for a given radius.
	 */
	protected static int s(int radiusIndex)
	{
		return ( 2 * radiusIndex ) + 1;
	}
	
	/**
	 * \brief Transforms a location on a given Cartesian axis into its 
	 * 			corresponding coordinate in the array. 
	 *  
	 * The result is written into coord_out[axis] and inside_out[axis].
	 *  
	 * @param axis - The axis to be operated on (index in output arrays).
	 * @param loc - A location on axis {@code axis}.
	 * @param resCalc - Resolution calculator for axis {@code axis}.
	 * @param coord_out - Output coordinate array.
	 * @param inside_out - Output inside array.
	 */
	public static void cartLoc2Coord(int axis, double loc, ResCalc resCalc,
									 int[] coord_out, double[] inside_out)
	{
		//TODO: use getResolutionSum(i)
		coord_out[axis] = 0; 
		double cumRes_prev = 0;
		while (cumRes_prev < loc)
		{
			cumRes_prev = resCalc.getCumResSum(coord_out[axis]);
			coord_out[axis]++;
		}
		if ( inside_out != null ) 
		{
			inside_out[axis] = (loc - cumRes_prev) 
								/ resCalc.getResolution(coord_out[axis]);
		}
	}
	
	/**
	 * \brief Transforms a location on a given polar axis into its 
	 * 			corresponding coordinate in the array. 
	 * 
	 * The result is written into coord_out[axis] and inside_out[axis].
	 * 
	 * @param axis - The axis to be operated on (index in output arrays).
	 * @param loc - A location in one dimension.
	 * @param arcLength - The arcLength in that dimension.
	 * @param idx_out - Index for output.
	 * @param coord_out - Output coordinate array.
	 * @param inside_out - Output inside array.
	 */
	public static void polarLoc2Coord(int axis, double loc, double rad_size, 
						ResCalc resCalc, int[] coord_out, double[] inside_out)
	{
		final double arcLength = rad_size / resCalc.getTotalLength();
		int c = 0; 
		double length = resCalc.getCumResSum(c) * arcLength;
		while (length <= loc){
			c++;
			length = resCalc.getCumResSum(c) * arcLength;
//			System.out.println(length+" "+loc+" "+arcLength+" "+ resCalc.getCumResSum(c));
		}
		if ( inside_out != null ) 
//			System.out.println(length+" "+loc);
			inside_out[axis] = 1 - (length - loc) 
									/ (resCalc.getResolution(c) * arcLength);
		coord_out[axis] = c;
	}
	
	/**
	 * \brief Transforms an array coordinate on a given Cartesian axis into its 
	 * 			corresponding location in space. 
	 * 
	 * The result is written into loc_out[axis].
	 * 
	 * @param coord - A coordinate in one dimension.
	 * @param resCalc - The resolution calculator for the given dimension.
	 * @param inside - The subcoordinate inside the grid cell.
	 * @param axis - The axis to be operated on (index in output arrays).
	 * @param loc_out - Output location array.
	 */
	public static void cartCoord2Loc(int axis, int coord, ResCalc resCalc,
								double inside, double[] loc_out)
	{
		loc_out[axis] = resCalc.getCumResSum(coord-1);
		loc_out[axis] += inside * resCalc.getResolution(coord);
	}
	
	/**
	 * \brief Transforms a location on a given polar axis into its 
	 * 			corresponding coordinate in the array. 
	 * 
	 * The result is written into loc_out[axis].
	 * 
	 * @param coord - A coordinate in one dimension.
	 * @param arcLength - The arcLength in that dimension.
	 * @param inside - The subcoordinate inside the grid cell.
	 * @param axis - The axis to be operated on (index in output arrays).
	 * @param loc_out - Output location array.
	 */
	public static void polarCoord2Loc(int axis, int coord, double radSize, 
								ResCalc resCalc, double inside, double[] loc_out)
	{
		loc_out[axis] = resCalc.getCumResSum(coord-1) 
							/ resCalc.getTotalLength() 
							* radSize;
		loc_out[axis] += inside * resCalc.getResolution(coord) 
								* (radSize / resCalc.getTotalLength());
	}
	
	protected static double getSharedArea(int d, double len_cur, 
			double[] bounds, double[] bounds_nbh, double len_nbh){
		boolean is_right, is_left, is_inBetween;
		double sA=0;

		// t1 of nbh <= t1 of cc (right, counter-clockwise)
		double len_s;
		if (d < 0){
			is_right = bounds_nbh[0] <= bounds[0];
			is_left = bounds_nbh[1] >= bounds[1];
			is_inBetween = is_left && is_right;
			len_s = len_cur;
		}else{
			is_right = bounds_nbh[0] < bounds[0];
			is_left = bounds_nbh[1] > bounds[1];
			is_inBetween = !(is_left || is_right);
			len_s = len_nbh;
		}

		if (is_inBetween) sA = 1;
		else if (is_right) sA = (bounds_nbh[1]-bounds[0])/len_s;
		else sA = (bounds[1]-bounds_nbh[0])/len_s; // is_left
		return sA;
}

	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void rowToBuffer(double[] row, StringBuffer buffer)
	{
		for ( int i = 0; i < row.length - 1; i++ )
			buffer.append(row[i]+", ");
		buffer.append(row[row.length-1]);
	}
	
	public void matrixToBuffer(double[][] matrix, StringBuffer buffer)
	{
		for ( int i = 0; i < matrix.length - 1; i++ )
		{
			if ( matrix[i].length == 1 )
				buffer.append(matrix[i][0]+", ");
			else
			{
				rowToBuffer(matrix[i], buffer);
				buffer.append(";\n");
			}
		}
		rowToBuffer(matrix[matrix.length - 1], buffer);
	}
	
	public StringBuffer arrayAsBuffer(ArrayType type)
	{
		StringBuffer out = new StringBuffer();
		double[][][] array = this._array.get(type);
		for ( int i = 0; i < array.length - 1; i++ )
		{
			matrixToBuffer(array[i], out);
			if ( array[i].length == 1 )
				out.append(", ");
			else
				out.append("\n");
		}
		matrixToBuffer(array[array.length - 1], out);
		return out;
	}
	
	public String arrayAsText(ArrayType type)
	{
		return this.arrayAsBuffer(type).toString();
	}
}

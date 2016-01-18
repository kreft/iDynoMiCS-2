package grid;

import java.util.ArrayList;
import java.util.function.DoubleFunction;

import boundary.BoundaryCyclic;
import dataIO.LogFile;
import grid.GridBoundary.GridMethod;
import grid.ResolutionCalculator.ResCalc;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

/**
 * \brief Abstract super class of all polar grids (Cylindrical and Spherical).
 * 
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public abstract class PolarGrid extends SpatialGrid
{
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
	/**
	 * Total size in each dimension
	 */
	protected double[] _radSize;
	/**
	 * factor scaling polar dimensions to have one grid cell per 90° 
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
		/*
		 * Initialize members
		 */
		_ires = Vector.vector(3, -1.0);
		_radSize = Vector.vector(3, -1.0);
		_subNbhSet = new ArrayList<int[]>();
		
		/*
		 * Set up members
		 */
		_nbhIdx = 0;
		_subNbhIdx = 0;
		_radSize[1] = Math.toRadians(totalSize[1]%361);
		_ires[1] = PolarArray.ires(_radSize[1]);  
		addBoundary(BoundarySide.INTERNAL,
				new BoundaryCyclic().getGridMethod(""));
	}
	
	@Override
	@Deprecated // we have to talk about _nVoxel or _totalLength beeing standard 
	public int[] getNumVoxels()
	{
		return null;
	}

	@Override
	public double getValueAt(ArrayType type, int[] coord)
	{
		if ( this._array.containsKey(type) )
			return this._array.get(type)[coord[0]][coord[1]][coord[2]];
		else
			return Double.NaN;
	}

	/**
	 * \brief Change the value of one coordinate on the given array type.
	 * 
	 * @param type Type of array to be set.
	 * @param coord Coordinate on this array to set.
	 * @param newValue New value with which to overwrite the array.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	public void setValueAtNew(ArrayType type, int[] coord, double newValue)
	{
		if ( ! this._array.containsKey(type) )
		{
			LogFile.writeLog("Warning: tried to set coordinate in "+
							type.toString()+" array before initialisation.");
			this.newArray(type);
		}
		this._array.get(type)[coord[0]][coord[1]][coord[2]] = newValue;
	}

	/**
	 * \brief Applies the given function to the array element at the given
	 * <b>voxel</b> coordinates (assumed adjusted for padding). 
	 * 
	 * @param name String name of the array.
	 * @param aC Internal array coordinates of the voxel required. 
	 * @param f DoubleFunction to apply to the array element at <b>voxel</b>.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double applyToVoxel(ArrayType type, int[] aC,
			DoubleFunction<Double> f)
	{
		try
		{
			double[][][] array = this._array.get(type);
			return array[aC[0]][aC[1]][aC[2]] = 
					f.apply(array[aC[0]][aC[1]][aC[2]]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			throw new ArrayIndexOutOfBoundsException(
					"Voxel coordinates must be inside array: "
							+aC[0]+", "+aC[1]+", "+aC[2]);
		}
	}

	/**
	 * \brief TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param f
	 */
	protected double applyToCoord(ArrayType type, int[] gridCoords,
			DoubleFunction<Double> f)
	{
		return this.applyToVoxel(type, gridCoords, f);
	}

	/**
	 * TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param value
	 */
	public void setValueAt(ArrayType type, int[] gridCoords, double value)
	{
		this.applyToVoxel(type, gridCoords, (double v)->{return value;});
	}

	/**
	 * TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param value
	 */
	public void addValueAt(ArrayType type, int[] gridCoords, double value)
	{
		this.applyToVoxel(type, gridCoords, (double v)->{return v + value;});
	}

	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param gridCoords
	 * @param value
	 */
	public void timesValueAt(ArrayType type, int[] gridCoords, double value)
	{
		this.applyToVoxel(type, gridCoords, (double v)->{return v * value;});
	}

	@Override
	public void setAllTo(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), ()->{return value;});
	}

	@Override
	public void addToAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v+value;});
	}

	@Override
	public void timesAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v*value;});
	}

	@Override
	public double getMax(ArrayType type) {
		final double[] max=new double[]{Double.NEGATIVE_INFINITY};
		PolarArray.applyToAll(
				_array.get(type),(double v)->{max[0]=v>max[0] ? v : max[0];}
		);
		return max[0];
	}

	@Override
	public double getMin(ArrayType type) {
		final double[] min=new double[]{Double.POSITIVE_INFINITY};
		PolarArray.applyToAll(
				_array.get(type),(double v)->{min[0]=v<min[0] ? v : min[0];}
		);
		return min[0];
	}

	@Override
	public void addArrayToArray(ArrayType destination, ArrayType source) {
		PolarArray.applyToAll(
				_array.get(destination), 
				_array.get(source), 
				(double vd, double vs)->{return vd+vs;}
		);
	}

	/**
	 * \brief Discard the iterative coordinate.
	 */
	public void closeIterator()
	{
		this._currentCoord = null;
	}
	
	public double getValueAtCurrent(ArrayType type)
	{
		return this.getValueAt(type, this._currentCoord);
	}
	public void setValueAtCurrent(ArrayType type, double value)
	{
		this.setValueAt(type, this._currentCoord, value);
	}

	public GridMethod nbhIteratorIsOutside()
	{
		BoundarySide bSide = this.isOutside(this._currentNeighbor);
		if ( bSide == null )
			return null;
		GridMethod m = this._boundaries.get(bSide);
		//TODO: throw IllegalAccessError ?
		if (m==null){ 
			System.err.println(
				"trying to access non-existent boundary side "+bSide.toString());
		}
		return m;
	}
	
	/**
	 * \brief updates the current neighbor coordinate.
	 * 
	 * Called when the neighborhood iterator was manipulated.
	 */
	public void currentNbhIdxChanged(){
		_subNbhIdx=0;
		_subNbhSet.clear();
		fillNbhSet();
//		if (_subNbhSet.isEmpty())
//			nbhIteratorNext();
		_currentNeighbor = _subNbhSet.get(0);
	}
	
	/**
	 * \brief Populates the <b>_subNbhSet</b> for the current <b>NBH_DIREC</b>.
	 * 
	 * Called when the current neighborhood index changed, 
	 * which means that <b>NBH_DIREC</b> changed, too.
	 */
	public abstract void fillNbhSet();

	@Override
	public int[] resetIterator()
	{
		if ( this._currentCoord == null )
			this._currentCoord = Vector.zerosInt(3);
		else
			for ( int i = 0; i < 3; i++ )
				this._currentCoord[i] = 0;
		return this._currentCoord;
	}
	
	/**
	 * \brief Returns a boolean indicating whether the iterator exceeds <b>axis</b>.
	 * 
	 * @param axis - An axis with 0 <= axis < 3.
	 * @return - A boolean indicating whether the iterator exceeds <b>axis</b>.
	 */
	protected abstract boolean iteratorExceeds(int axis);
	
	/**
	 * TODO
	 * 
	 * @return int[3] coordinates of next position.
	 * @exception IllegalStateException Iterator exceeds boundaries.
	 */
	public int[] iteratorNext()
	{
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
//		System.out.println(Arrays.toString(_currentCoord));
		return _currentCoord;
	}
	
	@Override
	public int[] resetNbhIterator(){
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
	public int[] nbhIteratorNext(){
		_subNbhIdx++;
		/*
		 * iterate through _subNbhSet first
		 */
		if (_subNbhIdx < _subNbhSet.size()){ 
			_currentNeighbor = _subNbhSet.get(_subNbhIdx);
		}else{
			/*
			 * if _subNbhSet has no more elements step into next 
			 * (orthogonal) direction and (re-)populate the _subNbhSet.
			 */
			_nbhIdx++;
			if (_nbhIdx < NBH_DIRECS.length) currentNbhIdxChanged();
		}
		/*
		 * Transform internal boundaries with radius >= 0 automatically (cyclic)
		 */
		_currentNeighbor = transInternal(_currentNeighbor);
		return _currentNeighbor;
	}
	
	/**
	 * \brief Checks if the given coordinate is outside the grid 
	 * in dimension <b>dim</b>.
	 * 
	 * @param coord - A coordinate.
	 * @param dim - A dimension with 0 <= dim < 3.
	 * @return - A BoundarySide if the coordinate is outside in dimension 
	 * 			<b>dim</b>. Or null if the coordinate is not outside.
	 */
	protected abstract BoundarySide isOutside(int[] coord, int dim);
	
	@Override
	protected BoundarySide isOutside(int[] coord) {
		for (int dim=0; dim<3; ++dim){
			BoundarySide bs = isOutside(coord,dim);
			if (bs!=null) return bs;
		}
		return null;
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
	protected BoundarySide[] getBoundarySides(int[]coord, BoundarySide[] out){
		if (out==null) out = new BoundarySide[3];
		for (int dim=0; dim<3; ++dim)
			out[dim]=isOutside(coord, dim);
		return out;
	}
	
	/**
	 * \brief Performs cyclic transform for inside boundaries with radius >= 0.
	 * 
	 * @param coord - A coordinate
	 * @return - The cyclic transformed coordinate.
	 */
	protected int[] transInternal(int[] coord){
		BoundarySide[] bsa = new BoundarySide[3];
		int nc=0, ic=0; // null counter and internal with r>=0 counter
		bsa = getBoundarySides(coord, bsa);
		for (BoundarySide bs : bsa){
			if (bs==null) nc++;
			if (bs==BoundarySide.INTERNAL && coord[0]>=0) ic++;
		}
		if (nc+ic==3) {			
			// only null and internal with r>=0 -> transform
			coord=cyclicTransform(coord);
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
	public double[] getVoxelOrigin(int[] coord) {
		return getLocation(coord, VOXEL_ORIGIN_HELPER);
	}
	
	@Override
	public double[] getVoxelCentre(int[] coord)
	{
		return getLocation(coord, VOXEL_CENTRE_HELPER);
	}
	
	@Override
	public int[] getCoords(double[] loc) {
		return getCoords(loc,null);
	}
	
	/**
	 * \brief Transforms a given location into array-coordinates and 
	 * computes sub-coordinates inside the grid element if inside != null. 
	 * 
	 * @param loc - a location in simpulated space.
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
	 * @param r - radius.
	 * @return - a scaling factor for a given radius.
	 */
	protected static int s(int r)
	{
		return ( 2 * r) + 1;
		}
	
	/**
	 * \brief Transforms a Cartesian location into a coordinate in the array. 
	 * 
	 * The result is written into coord_out[idx_out] and inside_out[idx_out].
	 *  
	 * @param loc - A location in one dimension.
	 * @param nVoxel - Number of voxels in that dimension.
	 * @param res -  The resolution of the coordinate.
	 * @param idx_out - Index for output.
	 * @param coord_out - Output coordinate array.
	 * @param inside_out - Output inside array.
	 */
	public static void cartLoc2Coord(double loc, ResCalc resCalc,
							int idx_out, int[] coord_out, double[] inside_out)
	{
		//TODO: use getResolutionSum(i)
		double counter = 0.0;
		countLoop: for ( int i = 0; i < resCalc.getNVoxel(); i++ )
		{
			if ( counter >= loc)
			{
				coord_out[idx_out] = i;
				if ( inside_out != null ) 
					inside_out[idx_out] = counter-loc;
				break countLoop;
			}
			counter += resCalc.getResolution(i);
		}
	}
	
	/**
	 * \brief Transforms a polar location into a coordinate in the array.
	 * 
	 * The result is written into coord_out[idx_out] and inside_out[idx_out].
	 * 
	 * @param loc - A location in one dimension.
	 * @param arcLength - The arcLength in that dimension.
	 * @param idx_out - Index for output.
	 * @param coord_out - Output coordinate array.
	 * @param inside_out - Output inside array.
	 */
	public static void polarLoc2Coord(double loc, double arcLength, 
							int idx_out, int[] coord_out, double[] inside_out)
	{
		double c = loc/arcLength;
		coord_out[idx_out] = (int)(c);
		if ( inside_out != null ) 
			inside_out[idx_out] = Math.abs( c - coord_out[idx_out] );
	}
	
	/**
	 * \brief Transforms a Cartesian coordinate into a location in space.
	 * 
	 * The result is written into loc_out[idx_out].
	 * 
	 * @param coord - A coordinate in one dimension.
	 * @param resCalc - The resolution calculator for the given dimension.
	 * @param inside - The subcoordinate inside the grid cell.
	 * @param idx_out - Index for output.
	 * @param loc_out - Output location array.
	 */
	public static void cartCoord2Loc(int coord, ResCalc resCalc,
								double inside, int idx_out, double[] loc_out)
	{
		//TODO: use getResolutionSum(i)
		for ( int i = 0; i < coord; i++ )
			loc_out[idx_out] += resCalc.getResolution(i);
		loc_out[idx_out] += inside * resCalc.getResolution(coord);
	}
	
	/**
	 * \brief Transforms a polar coordinate into a location in space.
	 * 
	 * The result is written into loc_out[idx_out].
	 * 
	 * @param coord - A coordinate in one dimension.
	 * @param arcLength - The arcLength in that dimension.
	 * @param inside - The subcoordinate inside the grid cell.
	 * @param idx_out - Index for output.
	 * @param loc_out - Output location array.
	 */
	public static void polarCoord2Loc(int coord, double arcLength, 
								double inside, int idx_out, double[] loc_out)
	{
		loc_out[idx_out] = ( coord + inside ) * arcLength;
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

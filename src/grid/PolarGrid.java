package grid;

import java.util.ArrayList;
import java.util.function.DoubleFunction;

import dataIO.LogFile;
import grid.GridBoundary.GridMethod;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

/**
 * \brief Abstract super class of all polar grids (Cylindrical and Spherical).
 * 
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
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
	 * Predefined array of relative neighbors of a grid coordinate.
	 */
	protected int[][] _nbhs;
	/**
	 * _nVoxel[1] in radian -> length in theta, currently only multiples of Pi/2
	 * 
	 * TODO Rob [11Jan2016]: What does this mean?
	 */
	protected double _nt_rad;
	/**
	 * Inner resolutions (number of quadrants) for polar dimensions.
	 * 
	 * TODO Rob [11Jan2016]: Needs explaining better.
	 */
	protected double[] _ires;
	/**
	 * Set to store (maybe multiple) neighbors for the current neighbor
	 * direction.
	 * TODO Rob [11Jan2016]: Needs explaining better.
	 */
	protected ArrayList<int[]> _subNbhSet;	
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
	 * \brief Shared constructor commands. Initializes all members and resets
	 * iterators.
	 * 
	 * TODO Rob [11Jan2016]: Am I right in thinking that all angle must be
	 * input in degrees, and are then converted into radians?
	 * 
	 * TODO Rob [11Jan2016]: using nVoxel to input the angular dimensions is
	 * the wrong way to go about things, but this probably stems from how I set
	 * up SpatialGrid. We need to discuss this.
	 * 
	 * @param nVoxel Number of voxels in each dimension.
	 * @param resolution Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	private void init(int[] nVoxel, double[][] resolution)
	{
		/*
		 * Theta periodic in 1..360
		 */
		nVoxel[1] = Math.floorMod(nVoxel[1], 361);
		/*
		 * [r theta z], r=0 || theta=0 -> no grid, z=0 -> polar grid
		 */
		_nVoxel = Vector.copy(nVoxel);  
		/*
		 * Scales r but not ires. 
		 */
		_res = resolution;				
		 /*
		  * Central angle of the arc (theta) in radians.
		  * 
		  * TODO Rob [11Jan2016]: "Central angle of the arc" is correct
		  * terminology for the cylinder, but not for the sphere. We need to
		  * think about this!
		  */
		_nt_rad = Math.toRadians(nVoxel[1]);
		_ires = new double[3];
		/*
		 * Determine inner resolution in theta automatically for all
		 * polarGrids.
		 */
		_ires[1] = PolarArray.ires(nVoxel[0], _nt_rad, _res[1][0]);  
		 /*
		  * Neighbours (r, t, p)
		  * 
		  * TODO Rob [11Jan2016]: why are these coordinates chosen?
		  */
		_nbhs = new int[][] { {0,0,1}, {0,0,-1},
							  {0,1,0}, {0,-1,0}, 
							  {-1,-1,0}, {1,1,0} };
		_subNbhSet = new ArrayList<int[]>();
	}

	/**
	 * TODO \brief Constructor for a PolarGrid, using ... as arguments.
	 * 
	 * @param nVoxel - length in each dimension
	 * @param resolution - Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	public PolarGrid(int[] nVoxel, double[][] resolution)
	{
		init(nVoxel, resolution);
	}
	
	/**
	 * TODO \brief Constructor for a PolarGrid, using ... as arguments.
	 * 
	 * @param nVoxel - length in each dimension
	 * @param resolution -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	public PolarGrid(int[] nVoxel, double[] resolution)
	{
		double[][] res = convertResolution(nVoxel, resolution);
		init(nVoxel,res);
	}
	
	/**
	 * \brief Convert resolution from a one-dimensional vector to a
	 * 2-dimensional array.
	 * 
	 * <p>The way this will be done depends on the type of grid, i.e.
	 * Cylindrical or Spherical.</p>
	 * 
	 * @param nVoxel TODO
	 * @param oldRes
	 * @return
	 */
	protected abstract double[][] convertResolution(int[] nVoxel,
															double[] oldRes);
	
	@Override
	public int[] getNumVoxels()
	{
		return Vector.copy(this._nVoxel);
	}
	
	@Override
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		for ( int axis = 0; axis < 3; axis++ )
			// since periodicity is handled in constructor 
			// this should work for all PolarGrids
			out[axis] = ( this._nVoxel[axis] > 1 );  
		return out;
	}

	@Override
	public int numSignificantAxes()
	{
		int out = 0;
		for ( int axis = 0; axis < 3; axis++ )
			out += ( this._nVoxel[axis] > 1 ) ? 1 : 0;
		return out;
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getValueAt(grid.SpatialGrid.ArrayType, int[])
	 */
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

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#setAllTo(grid.SpatialGrid.ArrayType, double)
	 */
	@Override
	public void setAllTo(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), ()->{return value;});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#addToAll(grid.SpatialGrid.ArrayType, double)
	 */
	@Override
	public void addToAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v+value;});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#timesAll(grid.SpatialGrid.ArrayType, double)
	 */
	@Override
	public void timesAll(ArrayType type, double value) {
		PolarArray.applyToAll(_array.get(type), (double v)->{return v*value;});
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getMax(grid.SpatialGrid.ArrayType)
	 */
	@Override
	public double getMax(ArrayType type) {
		final double[] max=new double[]{Double.NEGATIVE_INFINITY};
		PolarArray.applyToAll(
				_array.get(type),(double v)->{max[0]=v>max[0] ? v : max[0];}
		);
		return max[0];
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getMin(grid.SpatialGrid.ArrayType)
	 */
	@Override
	public double getMin(ArrayType type) {
		final double[] min=new double[]{Double.POSITIVE_INFINITY};
		PolarArray.applyToAll(
				_array.get(type),(double v)->{min[0]=v<min[0] ? v : min[0];}
		);
		return min[0];
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#addArrayToArray(grid.SpatialGrid.ArrayType, grid.SpatialGrid.ArrayType)
	 */
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
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getValueAtCurrent(grid.SpatialGrid.ArrayType)
	 */
	public double getValueAtCurrent(ArrayType type)
	{
		return this.getValueAt(type, this._currentCoord);
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#setValueAtCurrent(grid.SpatialGrid.ArrayType, double)
	 */
	public void setValueAtCurrent(ArrayType type, double value)
	{
		this.setValueAt(type, this._currentCoord, value);
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#nbhIteratorIsOutside()
	 */
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
	 * updates the current neighbor coordinate
	 * called when the neighborhood iterator was manipulated.
	 */
	public void currentNbhIdxChanged(){
		_subNbhIdx=0;
		_subNbhSet.clear();
		fillNbhSet();
//		if (_subNbhSet.isEmpty())
//			nbhIteratorNext();
		_currentNeighbor = _subNbhSet.get(0);
	}
	
	public abstract void fillNbhSet();
	
//	/**
//	 * converts (r,t,p) or (r,t,z) coordinates into an index 
//	 * (for SphericalGrid and CylindricalGrid, respectively). 
//	 * 
//	 * @param coord - a (r,t,p) or (r,t,z) coordinate
//	 * @return the corresponding index
//	 */
//	public abstract int coord2idx(int[] coord);
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isIteratorValid()
	 */
	@Override
	public boolean isIteratorValid() {return _currentCoord[0] < _nVoxel[0];}
	
	public boolean iteratorHasNext() {
		/*
		 * iterator is valid and not on second last item
		 */
		return isIteratorValid() 
				&& !(_currentCoord[0] == _nVoxel[0]-1 
				&& _currentCoord[1] == nRows(_currentCoord[0])-1
				&& _currentCoord[2] 
						== nCols(_currentCoord[0],_currentCoord[1])-1);
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#resetIterator()
	 */
	public int[] resetIterator()
	{
		if ( this._currentCoord == null )
			this._currentCoord = Vector.zerosInt(3);
		else
			for ( int i = 0; i < 3; i++ )
				this._currentCoord[i] = 0;
		return this._currentCoord;
	}
	
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
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#resetNbhIterator()
	 */
	public int[] resetNbhIterator(){
		_nbhIdx=0;
		currentNbhIdxChanged();
		_currentNeighbor = transInternal(_currentNeighbor);
		return _currentNeighbor;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isNbhIteratorValid()
	 */
	public boolean isNbhIteratorValid(){
		if (_subNbhIdx >= _subNbhSet.size()){
			return _nbhIdx < _nbhs.length - 1;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#nbhIteratorNext()
	 */
	public int[] nbhIteratorNext(){
		_subNbhIdx++;
		if (_subNbhIdx < _subNbhSet.size()){ // subiterator has next
			_currentNeighbor = _subNbhSet.get(_subNbhIdx);
		}else{
			_nbhIdx++;
			if (_nbhIdx < _nbhs.length) currentNbhIdxChanged();
		}
		_currentNeighbor = transInternal(_currentNeighbor);
		return _currentNeighbor;
	}
	
	protected abstract BoundarySide isOutside(int[] coord, int dim);
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isOutside(int[])
	 */
	@Override
	protected BoundarySide isOutside(int[] coord) {
		for (int dim=0; dim<3; ++dim){
			BoundarySide bs = isOutside(coord,dim);
			if (bs!=null) return bs;
		}
		return null;
	}
	
	/**
	 * Computes isOutside for all 3 dimensions. Does return minimum OR maximum, 
	 * not both. Decision for min or max if the coord is both min and max 
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
	 * Performs cyclic transform for inside boundaries.
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
	
//	/**
//	 * @return the length of the grid (maximum index)
//	 */
//	public abstract int length();
	
	/**
	 * Converts a coordinate in the grid's array to a location in simulated 
	 * space. 
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
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getCoords(double[])
	 */
	@Override
	public int[] getCoords(double[] loc) {
		return getCoords(loc,null);
	}
	
	/**
	 * Transforms a given location into array-coordinates and 
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
	 * @param x - any double
	 * @return - rounded value with 1e-10 precision
	 */
	protected double round10(double x){return Math.round(x*1e10)*1e-10;}
	
	/**
	 * @param x - any double
	 * @return - rounded value with 1e-100 precision
	 */
	protected double round100(double x){return Math.round(x*1e16)*1e-16;}
	
	/**
	 * Computes a factor that scales the number of elements for increasing 
	 * radius to keep element volume fairly constant.
	 * 
	 * @param r - radius.
	 * @return - a scaling factor for a given radius.
	 */
	protected int s(int r){return 2*r+1;}
	
	/**
	 * computes the number of elements in one triangle for radius r
	 * 
	 * @param r - radius
	 * @return - the number of elements in one triangle for radius r.
	 */
	@Deprecated
	protected  int sn(int r){
		return (int)(_ires[1]*s(r)*(r+1));
	}
	
	/**
	 * \brief computes the number of rows in matrix i.
	 * 
	 * @param i - matrix index
	 * @return - the number of rows for a given radius.
	 */
	public int nRows(int i) {
		return (int)_ires[2]*s(i);
	}
	
	/**
	 * \brief Computes the number of columns in matrix i
	 * 
	 * @param i - matrix index
	 * @param j - row index
	 * @return - the number of elements in row j
	 */
	public int nCols(int i, int j){
		double t_scale=(Math.PI/2)/(s(i)-0.5);
		double np=_ires[1]+(s(i)-1)*_ires[1]*Math.sin(j*t_scale*_res[2][0]);
//		System.out.println(np);
		return (int)Math.round(np);
	}
	
	/**
	 * \brief computes the number of elements in the matrix with index i 
	 * 		  until but excluding row j
	 * 
	 * @param i - matrix index
	 * @param j - row index
	 * @return - the number of elements in matrix i until but excluding row j
	 */
	public int n(int i, int j){
		double x=2*_ires[1]*i*Math.sin((Math.PI*_res[2][0])/(2*(4*i+1)));
		double y=Math.cos((Math.PI*_res[2][0])/(4*i+1))-1;
		return (int)Math.round((x*Math.cos((Math.PI*(0.5-j)*_res[2][0])/(4*i+1)))/(y)+j*_ires[1]
						-(x*Math.cos((Math.PI*_res[2][0])/(2*(4*i+1))))/(y));
		
//		return (int) Math.round(_ires[1]*j*(2*i*Math.sin((Math.PI*j)/(4*i+1))+1));
		
//		 return (int)((2*t*(-1 + Math.cos(1)) - _ires[2]*(1 + r)*Math.sin(1) 
//				 + _ires[2]*(1 + r)*(Math.sin(1 - t) 
//				 + Math.sin(t)))/(-1 + Math.cos(1)));

	}
	
	/**
	 * computes the number of elements in the grid array until and including 
	 * matrix i
	 * 
	 * @param i - matrix index
	 * @return - the number of elements until and including matrix i
	 */
	public int N(int i){
		return (int)((_ires[1]*_ires[2]*(i+1)*(i+2)*(4*i+3))/6);
	}
	
	public static void cartLoc2Coord(
			double loc, int nVoxel, double[] res,
			int idx_out, int[] coord_out, double[] inside_out){
		double counter = 0.0;
		countLoop: for ( int i = 0; i < nVoxel; i++ )
		{
			if ( counter >= loc)
			{
				coord_out[idx_out] = i;
				if (inside_out!=null) inside_out[idx_out] = counter-loc;
				break countLoop;
			}
			counter += res[i];
		}
	}
	
	public static void polarLoc2Coord(double loc, double arcLength, 
			int idx_out, int[] coord_out, double[] inside_out){
		double c = loc/arcLength;
		coord_out[idx_out] = (int)(c);
		if (inside_out != null) 
			inside_out[idx_out] = Math.abs(c-coord_out[idx_out]);
	}
	
	public static void cartCoord2Loc(int coord, double[] res, double inside,
			int idx_out, double[] loc_out){
		for ( int i = 0; i < coord; i++ ){
			loc_out[idx_out] += res[i];
		}
		loc_out[idx_out] += inside * res[coord];
	}
	
	public static void polarCoord2Loc(int coord, double arcLength, double inside,
			int idx_out, double[] loc_out){
		loc_out[idx_out]=(coord+inside)*arcLength;
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

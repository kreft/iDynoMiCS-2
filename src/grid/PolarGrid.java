package grid;

import java.lang.instrument.IllegalClassFormatException;
import java.util.LinkedList;
import java.util.function.DoubleFunction;

import dataIO.LogFile;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

/**
 * @author Stefan Lang, Friedrich-Schiller University Jena (stefan.lang@uni-jena.de)
 * 
 * abstract super class of all polar grids (Cylindrical and Spherical)
 *
 */
public abstract class PolarGrid extends SpatialGrid {
	// current index of iterator and neighborhood iterator
	protected int _nbhIdx, _idx;   
	// used to block neighborhood iterator on multiple neighbors
	protected boolean _isMultNbh;  
	// array, pre-defining neighbors of a grid cell relative to the cell
	protected int[][] _nbhs;
	// _nVoxel[1] in radian -> length in theta, currently only multiples of Pi/2 
	protected double _nt_rad;
	
	protected LinkedList<int[]> nbhq = new LinkedList<int[]>();

	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution - Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	PolarGrid(int[] nVoxel, double[][] resolution){
		init(nVoxel,resolution);
	}
	
	/**
	 * @param nVoxel - length in each dimension
	 * @param resolution -  Array of length 3 defining constant resolution
	 *  in each dimension 
	 */
	PolarGrid(int[] nVoxel, double[] resolution){
		// convert resolution to double[][]
		double [][] res = new double[3][0];
		for (int i=0; i<res.length; ++i){
			// TODO: other Polar Grids (if there are any in the future) need 
			// implement own conversions here, which is not so handy maybe.
			
			// convert dependent resolutions
			if (this instanceof CylindricalGrid)
				// only one dependent variable t for cyl. grid (const. res)
				res[i] = i==1 ? new double[1] : new double[nVoxel[i]];
			else if (this instanceof SphericalGrid)
				// two dependent variables t and p for spher. grid (const. res)
				res[i] = i==1 || i==2 ? new double[1] : new double[nVoxel[i]];
			else
				try {
					throw new IllegalClassFormatException(
							"Only spherical and cylindrical Grid is allowed here");
				} catch (IllegalClassFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			// write to new array
			for (int j=0; j<res[i].length; ++j){
				res[i][j]=resolution[i];
			}
		}
		init(nVoxel,res);
	}
	
	/**
	 * Shared constructor commands. Initializes all members and resets iterators.
	 * 
	 * @param nVoxel - length in each dimension
	 * @param resolution - Array of length 3,
	 *  containing arrays of length _nVoxel[dim] for non-dependent dimensions
	 *  (r and z) and length 1 for dependent dimensions (t and p), 
	 *  which implicitly scale with r.
	 */
	private void init(int[] nVoxel, double[][] resolution){
		// theta periodic in 1..360
		nVoxel[1] = nVoxel[1]%361; 
		// [r theta z], r=0 || theta=0 -> no grid, z=0 -> polar grid
		this._nVoxel = Vector.copy(nVoxel);  
		// scales r but not ires 
		this._res = resolution;				
		 // length in t in radian
		this._nt_rad = nVoxel[1]*Math.PI/180;
		// determine inner resolution in theta automatically for all polarGrids
		this._res[1][0] = PolarArray.computeIRES(nVoxel[0], _nt_rad);  
		 // neighbours
		_nbhs=new int[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}};
		resetIterator();
		resetNbhIterator();
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getNumVoxels()
	 */
	@Override
	public int[] getNumVoxels() {
		return Vector.copy(this._nVoxel);
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#getSignificantAxes()
	 */
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		for ( int axis = 0; axis < 3; axis++ )
			// since periodicity is handled in constructor 
			// this should work for all PolarGrids
			out[axis] = ( this._nVoxel[axis] > 1 );  
		return out;
	}

	/* (non-Javadoc)
	 * @see grid.SpatialGrid#numSignificantAxes()
	 */
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
		return this._boundaries.get(bSide);
	}
	
	/**
	 * updates the current neighbor coordinate
	 * called when the neighborhood iterator was manipulated.
	 */
	public abstract void currentNbhIdxChanged();
	
	/**
	 * converts (r,t,p) or (r,t,z) coordinates into an index 
	 * (for SphericalGrid and CylindricalGrid, respectively). 
	 * 
	 * @param coord - a (r,t,p) or (r,t,z) coordinate
	 * @return the corresponding index
	 */
	public abstract int coord2idx(int[] coord);
	
	/**
	 * converts an index into (r,t,p) or (r,t,z) coordinates.
	 * (for SphericalGrid and CylindricalGrid, respectively). 
	 * 
	 * @param idx - an index starting from 1
	 * @param coord - array of length 3 to write the result in, can be null.
	 * @return - the corresponding (r,t,p) or (r,t,z) coordinate.
	 */
	public abstract int[] idx2coord(int idx, int[] coord);
	
	/**
	 * converts current (r,t,p) or (r,t,z) coordinates into an index 
	 * and updates the index (used when _currentCoord changed)
	 */
	public void currentCoordChanged() {
		_idx = coord2idx(_currentCoord);
	}
	
	/**
	 * converts the current index into (r,t,p) or (r,t,z) coordinates.
	 * and updates the current coordinate (used when _currentIdx changed)
	 */
	public void currentIdxChanged(){
		_currentCoord=idx2coord(_idx, _currentCoord);
	}
	
	/**
	 * Used to manipulate the current coordinate. 
	 * Sends a currentCoordChanged() event.
	 * @param new_currentCoord the new current coordinate.
	 */
	public void setCurrent(int[] new_currentCoord){
		_currentCoord=new_currentCoord;
		currentCoordChanged();
	}
	
	/**
	 * Used to manipulate the current index. 
	 * Sends a currentIdxChanged() event.
	 * @param new_currentIdx the new current index.
	 */
	public void setCurrent(int new_currentIdx){
		_idx=new_currentIdx;
		currentIdxChanged();
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isIteratorValid()
	 */
	@Override
	public boolean isIteratorValid() {return _idx<=length();}
	
	/**
	 * @return the current iterator index
	 */
	public int iteratorCurrentIdx(){return _idx;}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#resetIterator()
	 */
	@Override
	public int[] resetIterator() {
		_idx=1;
		if ( this._currentCoord == null )
			this._currentCoord = Vector.zerosInt(3);
		else
			currentIdxChanged();
		return this._currentCoord;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#iteratorNext()
	 */
	@Override
	public int[] iteratorNext() {
		_idx++;
		currentIdxChanged();
		return _currentCoord;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#resetNbhIterator()
	 */
	public int[] resetNbhIterator(){
		_nbhIdx=0;
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.add(Vector.zerosInt(3),_nbhs[0]);
		else
			currentNbhIdxChanged();
		return _currentNeighbor;
	}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#isNbhIteratorValid()
	 */
	public boolean isNbhIteratorValid(){return _nbhIdx<_nbhs.length;}
	
	/* (non-Javadoc)
	 * @see grid.SpatialGrid#nbhIteratorNext()
	 */
	public int[] nbhIteratorNext(){
		_nbhIdx++;
		currentNbhIdxChanged();
		return _currentNeighbor;
	}
	
	public LinkedList<int[]> getCurrentNeighborQueue(){
		return nbhq;
	}
	
	/**
	 * @return the length of the grid (maximum index)
	 */
	public abstract int length();
	
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
	public abstract int[] getCoords(double[] loc, double[] inside);
	
	/**
	 * @param x - any double
	 * @return - rounded value with 1e-10 precision
	 */
	protected double round10(double x){return Math.round(x*1e10)*1e-10;}

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

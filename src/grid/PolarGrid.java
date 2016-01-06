package grid;

import java.lang.instrument.IllegalClassFormatException;
import java.util.function.DoubleFunction;

import dataIO.LogFile;
import grid.GridBoundary.GridMethod;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;

public abstract class PolarGrid extends SpatialGrid {
	int nbhIdx, idx;
	boolean isMultNbh;
	int[][] nbhs;
	double nt_rad;

	PolarGrid(int[] nVoxel, double[][] resolution){
		init(nVoxel,resolution);
	}
	
	PolarGrid(int[] nVoxel, double[] resolution){
		double [][] res = new double[3][0];
		for (int i=0; i<res.length; ++i){
			if (this instanceof CylindricalGrid)
				res[i] = i==1 ? new double[1] : new double[nVoxel[i]];
			else if (this instanceof SphericalGrid)
				res[i] = i==1 || i==2 ? new double[1] : new double[nVoxel[i]];
			else
				try {
					throw new IllegalClassFormatException("Only spherical and cylindrical Grid is allowed here");
				} catch (IllegalClassFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			for (int j=0; j<res[i].length; ++j){
				res[i][j]=resolution[i];
			}
		}
		init(nVoxel,res);
	}
	
	private void init(int[] nVoxel, double[][] resolution){
		nVoxel[1] = nVoxel[1]%361; // theta periodic in 1..360
		this._nVoxel = Vector.copy(nVoxel);  // [r theta z], r=0 || theta=0 -> no grid, z=0 -> polar grid
		this._res = resolution;				 // scales r but not ires 
		this.nt_rad = nVoxel[1]*Math.PI/180; // length in t in radian
		// determine inner resolution in theta automatically for all polarGrids
		this._res[1][0] = PolarArray.computeIRES(nVoxel[0], nt_rad);  
		nbhs=new int[][]{{0,0,1},{0,0,-1},{0,1,0},{0,-1,0},{-1,-1,0},{1,1,0}}; // neighbours
		resetIterator();
		resetNbhIterator();
	}

	@Override
	public int[] getNumVoxels() {
		return Vector.copy(this._nVoxel);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public boolean[] getSignificantAxes()
	{
		boolean[] out = new boolean[3];
		for ( int axis = 0; axis < 3; axis++ )
			// since periodicity is handled in constructor this should work for all PolarGrids
			out[axis] = ( this._nVoxel[axis] > 1 );  
		return out;
	}

	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public int numSignificantAxes()
	{
		int out = 0;
		for ( int axis = 0; axis < 3; axis++ )
			out += ( this._nVoxel[axis] > 1 ) ? 1 : 0;
		return out;
	}

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
			//for ( int i : aC )
			//	System.out.println(i);
			throw new ArrayIndexOutOfBoundsException(
					"Voxel coordinates must be inside array: "+aC[0]+", "+aC[1]+", "+aC[2]);
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
		PolarArray.applyToAll(_array.get(type),(double v)->{max[0]=v>max[0] ? v : max[0];});
		return max[0];
	}

	@Override
	public double getMin(ArrayType type) {
		final double[] min=new double[]{Double.POSITIVE_INFINITY};
		PolarArray.applyToAll(_array.get(type),(double v)->{min[0]=v<min[0] ? v : min[0];});
		return min[0];
	}

	@Override
	public void addArrayToArray(ArrayType destination, ArrayType source) {
		PolarArray.applyToAll(_array.get(destination), _array.get(source), (double vd, double vs)->{return vd+vs;});
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

	/**
	 * 
	 * @return
	 */
	public GridMethod nbhIteratorIsOutside()
	{
		BoundarySide bSide = this.isOutside(this._currentNeighbor);
		if ( bSide == null )
			return null;
		return this._boundaries.get(bSide);
	}
	
	public abstract void currentNbhIdxChanged();
	public abstract int coord2idx(int[] coord);
	public abstract int[] idx2coord(int idx, int[] coord);
	
	// converts current rtp or rtz coordinates to an index and updates the index (used when _currentCoord changed)
	public void currentCoordChanged() {
		idx = coord2idx(_currentCoord);
	}
	
	public void currentIdxChanged(){
		_currentCoord=idx2coord(idx, _currentCoord);
	}
	
	public void setCurrent(int[] new_currentCoord){
		_currentCoord=new_currentCoord;
		currentCoordChanged();
	}
	public void setCurrent(int new_currentIdx){
		idx=new_currentIdx;
		currentIdxChanged();
	}
	
	@Override
	public boolean isIteratorValid() {return idx<=length();}
	
	public int iteratorCurrentIdx(){return idx;}
	
	@Override
	public int[] resetIterator() {
		idx=1;
		if ( this._currentCoord == null )
			this._currentCoord = Vector.zerosInt(3);
		else
			currentIdxChanged();
		return this._currentCoord;
	}
	
	@Override
	public int[] iteratorNext() {
		idx++;
		currentIdxChanged();
		return _currentCoord;
	}
	
	public int[] resetNbhIterator(){
		nbhIdx=0;
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.zerosInt(3);
		else
			currentNbhIdxChanged();
		return _currentNeighbor;
	}
	
	public boolean isNbhIteratorValid(){return nbhIdx<nbhs.length;}
	
	public int[] nbhIteratorNext(){
		nbhIdx++;
		currentNbhIdxChanged();
		return _currentNeighbor;
	}
	
	public abstract int length();
	public abstract double[] getLocation(int[] coord, double[] inside);

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

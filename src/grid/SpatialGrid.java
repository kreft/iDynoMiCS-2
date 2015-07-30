package grid;

import java.util.HashMap;
import java.util.function.DoubleFunction;

import linearAlgebra.*;


/**
 * 
 * 
 * <p>Important note on coordinate systems:<ul><li>Array coordinates go from 0
 * to (nVoxel + 2*padding)</li><li>Grid coordinates go from -padding to
 * (nVoxel + padding)</li></ul></p>
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 *
 */
public class SpatialGrid
{
	/**
	 * TODO
	 */
	protected double[][][] _array;
	
	/**
	 * TODO
	 */
	protected int[] _nVoxel;
	
	/**
	 * TODO
	 */
	protected int[] _padding;
	
	/**
	 * Grid resolution, i.e. the side length of each voxel in this grid.
	 */
	protected double _res;
	
	/**
	 * Current coordinate considered by the internal iterator.
	 */
	protected int[] _currentCoord;
	
	/**
	 * TODO
	 */
	protected HashMap<String, double[][][]> _tempArray;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public SpatialGrid(int[] nVoxel, int[] padding, double resolution)
	{
		this._nVoxel = Vector.copy(nVoxel);
		this._padding = Vector.copy(padding);
		this._array = Array.zerosDbl(this._nVoxel[0] + 2*this._padding[0],
									this._nVoxel[1] + 2*this._padding[1],
									this._nVoxel[2] + 2*this._padding[2]);
		this._res = resolution;
	}
	
	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Makes a copy of this SpatialGrid's array.
	 * 
	 * <p>Does not include padding.</p>
	 * 
	 * @return Three-dimensional array of doubles.
	 */
	public double[][][] getCore()
	{
		return Array.subarray(this._array,
				this._padding[0], this._nVoxel[0]+ this._padding[0],
				this._padding[1], this._nVoxel[1]+ this._padding[1],
				this._padding[2], this._nVoxel[2]+ this._padding[2]);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public double[][][] getArray()
	{
		return Array.copy(this._array);
	}
	
	/**
	 * \brief Returns the grid resolution (in micrometers).
	 * 
	 * @return double value of the grid resolution (in um).
	 */
	public double getResolution()
	{
		return this._res;
	}
	
	/**
	 * \brief Returns the volume of each voxel in this grid (in cubic 
	 * micrometers).
	 * 
	 * @return double value of each voxel's volume (in um<sup>3</sup>).
	 */
	public double getVoxelVolume()
	{
		return Math.pow(this._res, 3.0);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public int[] getNumVoxels()
	{
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
	
	/*************************************************************************
	 * SIMPLE SETTERS
	 ************************************************************************/
	
	/*************************************************************************
	 * COORDINATES
	 ************************************************************************/
	
	/**
	 * \brief Applies the given function to the array element at the given
	 * <b>voxel</b> coordinates (assumed adjusted for padding). 
	 * 
	 * @param aC Internal array coordinates of the voxel required. 
	 * @param f DoubleFunction to apply to the array element at <b>voxel</b>.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double applyToVoxel(int[] aC, DoubleFunction<Double> f)
	{
		try
		{
			return this._array[aC[0]][aC[1]][aC[2]] = 
									f.apply(this._array[aC[0]][aC[1]][aC[2]]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			for ( int i : aC )
				System.out.println(i);
			throw new ArrayIndexOutOfBoundsException(
									"Voxel coordinates must be inside array");
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>Note that <b>gridCoords</b> could contain negative values, but
	 * <i>arrayCoords</i> never will.</p>
	 * 
	 * @param gridCoords
	 * @return
	 */
	private int[] arrayCoords(int[] gridCoords)
	{
		int[] out = Vector.copy(gridCoords);
		Vector.add(out, this._padding);
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param gridCoords
	 * @param f
	 */
	protected double applyToCoord(int[] gridCoords, DoubleFunction<Double> f)
	{
		return this.applyToVoxel(this.arrayCoords(gridCoords), f);
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>This method does not affect the state of <b>location</b>.</p>
	 * 
	 * @param location 
	 * @return 
	 */
	public int[] getCoords(double[] location)
	{
		return Vector.toInt(Vector.times(Vector.copy(location), 1/this._res));
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param gridCoords
	 * @return
	 */
	protected double[] getVoxelOrigin(int[] gridCoords)
	{
		int[] temp = Vector.add(Vector.copy(gridCoords), this._padding);
		return Vector.times(Vector.toDbl(temp), this._res);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param gridCoords
	 * @return
	 */
	protected double[] getVoxelCentre(int[] gridCoords)
	{
		return Vector.add(getVoxelOrigin(gridCoords), 0.5*this._res);
	}
	
	/*************************************************************************
	 * VOXEL SETTERS
	 ************************************************************************/
	
	/**
	 * TODO
	 * 
	 * @param gridCoords
	 * @param value
	 */
	protected void setValueAt(int[] gridCoords, double value)
	{
		this.applyToCoord(gridCoords, (double v) -> {return value;});
	}
	
	/**
	 * TODO
	 * 
	 * @param gridCoords
	 * @param value
	 */
	protected void addValueAt(int[] gridCoords, double value)
	{
		this.applyToCoord(gridCoords, (double v) -> {return v + value;});
	}
	
	
	
	/*************************************************************************
	 * VOXEL GETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * 
	 * @param gridCoords
	 * @return
	 */
	public double getValueAt(int[] gridCoords)
	{
		return this.applyToCoord(gridCoords, (double v) -> {return v;});
	}
	
	/**
	 * \brief TODO 
	 * 
	 * @param gridCoords
	 * @return
	 */
	public double[][] getNeighborValues(int[] gridCoords)
	{
		double[][] out = new double[3][2];
		int[] temp = Vector.copy(gridCoords);
		for ( int axis = 0; axis < 3; axis++ )
		{
			temp[axis] -= 1;
			out[axis][0] = this.getValueAt(temp);
			temp[axis] += 2;
			out[axis][1] = this.getValueAt(temp);
			temp[axis] -= 1;
		}
		return out;
	}
	
	/*************************************************************************
	 * ARRAY SETTERS
	 ************************************************************************/
	
	/**
	 * \brief Applies the given function to all array elements, excluding
	 * padding.
	 * 
	 * @param f
	 */
	private void applyToAllCore(DoubleFunction<Double> f)
	{
		for ( int i = _padding[0]; i < _padding[0] + _nVoxel[0]; i++ )
			for ( int j = _padding[1]; j < _padding[1] + _nVoxel[1]; j++ )
				for ( int k = _padding[2]; k < _padding[2] + _nVoxel[2]; k++ )
				{
					this._array[i][j][k] = f.apply(this._array[i][j][k]);
				}
	}
	
	/**
	 * \brief Set all voxels to the <b>value</b> given.
	 * 
	 * @param value double value to use.
	 */
	public void setAllTo(double value, boolean includePadding )
	{
		if ( includePadding )
			Array.setAll(this._array, value);
		else
			this.applyToAllCore((double v) -> {return value;});
	}
	
	/**
	 * TODO
	 * 
	 * @param array
	 */
	public void setTo(double[][][] array)
	{
		Array.setAll(this._array, array);
	}
	
	/**
	 * TODO
	 * 
	 * @param value
	 */
	public void addToAll(double value, boolean includePadding)
	{
		if ( includePadding )
			Array.add(this._array, value);
		else
			this.applyToAllCore((double v) -> {return v + value;});
	}
	
	/*************************************************************************
	 * ARRAY GETTERS
	 ************************************************************************/
	
	/**
	 * \brief Returns the greatest value of the voxels in this grid.
	 * 
	 * TODO exclude padding?
	 * 
	 * @return
	 */
	public double getMax()
	{
		return Array.max(this._array);
	}
	
	/**
	 * \brief Returns the least value of the voxels in this grid.
	 * 
	 * TODO exclude padding?
	 * 
	 * @return
	 */
	public double getMin()
	{
		return Array.min(this._array);
	}
	
	
	/*************************************************************************
	 * GRADIENTS
	 ************************************************************************/
	
	/**
	 * \brief Calculate the differential 
	 * 
	 * 
	 * @param gridCoords
	 * @param axis
	 * @return
	 */
	protected double differential(int[] gridCoords, int axis)
	{
		int[] temp = Vector.copy(gridCoords);
		double out = -2.0 * getValueAt(temp);
		temp[axis] += 1;
		out += getValueAt(temp);
		temp[axis] -= 2;
		out += getValueAt(temp);
		out /= 2.0 * this._res;
		return ( Double.isFinite(out) ) ? out : 0.0;
	}
	
	protected double[] gradient(int[] gridCoords)
	{
		double[] out = new double[3];
		for ( int axis = 0; axis < 3; axis++ )
			out[axis] = differential(gridCoords, axis);
		return out;
	}
	
	/*************************************************************************
	 * ITERATOR
	 ************************************************************************/
	
	/**
	 * TODO
	 * 
	 */
	public void resetIterator()
	{
		this._currentCoord = Vector.zerosInt(3);
	}
	
	/**
	 * TODO
	 * 
	 * @return
	 */
	public boolean iteratorHasNext()
	{
		for ( int axis = 0; axis < 3; axis++ )
			if ( this._currentCoord[axis] < this._nVoxel[axis] - 1)
				return true;
		return false;
		
	}
	
	/**
	 * TODO
	 * 
	 * @param axis
	 * @return
	 */
	private boolean iteratorExceeds(int axis)
	{
		return _currentCoord[axis] >= 
								this._nVoxel[axis] + this._padding[axis];
	}
	
	/**
	 * TODO
	 * 
	 * @return int[3] coordinates of next position.
	 * @exception IllegalStateException Iterator exceeds boundaries.
	 */
	public int[] iteratorNext()
	{
		_currentCoord[0]++;
		if ( this.iteratorExceeds(0) )
		{
			_currentCoord[0] = this._padding[0];
			_currentCoord[1]++;
			if ( this.iteratorExceeds(1) )
			{
				_currentCoord[1] = this._padding[1];
				_currentCoord[2]++;
				if ( this.iteratorExceeds(2) )
				{
					throw new IllegalStateException(
											"Iterator exceeds boundaries.");
				}
			}
		}
		return _currentCoord;
	}
	
	/**
	 * \brief Discard the iterative coordinate.
	 */
	public void closeIterator()
	{
		this._currentCoord = null;
	}
	
	/*************************************************************************
	 * TEMPORARY ARRAY
	 ************************************************************************/
	
	/**
	 * \brief Initialise the temporary array 
	 */
	public void resetTempArray(String name)
	{
		try
		{
			Array.setAll(this._tempArray.get(name), 0.0);
		}
		catch ( Exception e)
		{
			this._tempArray.put(name, Array.zeros(this._array));
		}
	}
	
	public void copyArrayToTemporary(String name)
	{
		this._tempArray.put(name, this.getArray());
	}
	
	/**
	 * 
	 * @param coords
	 */
	public double getTempAt(String name, int[] coords)
	{
		return this._tempArray.get(name)[coords[0]][coords[1]][coords[2]];
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param coords
	 * @param value
	 */
	public void setTempAt(String name, int[] coords, double value)
	{
		
	}
	
	/**
	 * 
	 * @param coords
	 * @param value
	 */
	public void addToTempArray(String name, int[] coords, double value)
	{
		this._tempArray.get(name)[coords[0]][coords[1]][coords[2]] += value;
	}
	
	/**
	 * \brief Overwrite all values in the array with the corresponding values
	 * in the temporary array.
	 */
	public void acceptTempArray(String name)
	{
		this.setTo(this._tempArray.get(name));
	}
	
	/**
	 * \brief Discard the temporary array to save memory.
	 */
	public void closeTempArray(String name)
	{
		this._tempArray.remove(name);
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	
	
}
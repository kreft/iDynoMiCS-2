package grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.DoubleFunction;
import java.util.function.ToDoubleBiFunction;

import linearAlgebra.*;


/**
 * 
 * 
 * <p>Important note on coordinate systems:<ul><li>Array coordinates go from 0
 * to (nVoxel + 2*padding)</li><li>Grid coordinates go from -padding to
 * (nVoxel + padding)</li></ul></p>
 * 
 * TODO Could do away with two coordinate systems by modifying coordinator and
 * sticking with it
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
	protected HashMap<String, double[][][]> _array;
	
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
	 * Standard names for SpatialGrid arrays that are used in various places.
	 */
	public static final String concn = "concentration",
								diff = "diffusivity",
								domain = "domain", 
								reac = "reacRate", 
								dReac = "diffReacRate";
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param nVoxel
	 * @param padding
	 * @param resolution
	 */
	public SpatialGrid(int[] nVoxel, int[] padding, double resolution)
	{
		this._nVoxel = Vector.copy(nVoxel);
		this._padding = Vector.copy(padding);
		this._res = resolution;
	}
	
	public SpatialGrid()
	{
		this._nVoxel = Vector.vector(3, 1);
		this._padding = Vector.zerosInt(3);
		this._res = 1.0;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 */
	public void newArray(String name)
	{
		this.newArray(name, 0.0);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param initialValues
	 */
	public void newArray(String name, double initialValues)
	{
		/*
		 * First check that the array HashMap has been created.
		 */
		try
		{ this._array.getClass(); }
		catch (NullPointerException e)
		{ this._array = new HashMap<String, double[][][]>(); }
		/*
		 * Now try resetting all values of this array. If it doesn't exist
		 * yet, make it.
		 */
		try
		{
			Array.setAll(this._array.get(name), 0.0);
		}
		catch ( Exception e )
		{
			double[][][] array = Array.array(
					this._nVoxel[0] + 2*this._padding[0],
					this._nVoxel[1] + 2*this._padding[1],
					this._nVoxel[2] + 2*this._padding[2],
					initialValues);
			this._array.put(name, array);
		}
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
	public double[][][] getCore(String name)
	{
		return Array.subarray(this._array.get(name),
				this._padding[0], this._nVoxel[0]+ this._padding[0],
				this._padding[1], this._nVoxel[1]+ this._padding[1],
				this._padding[2], this._nVoxel[2]+ this._padding[2]);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public double[][][] getArray(String name)
	{
		return Array.copy(this._array.get(name));
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
	public int[] getPadding()
	{
		return Vector.copy(this._padding);
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
	 * @param name String name of the array.
	 * @param aC Internal array coordinates of the voxel required. 
	 * @param f DoubleFunction to apply to the array element at <b>voxel</b>.
	 * @exception ArrayIndexOutOfBoundsException Voxel coordinates must be
	 * inside array.
	 */
	private double applyToVoxel(String name, int[] aC,
													DoubleFunction<Double> f)
	{
		try
		{
			double[][][] array = this._array.get(name);
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
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param f
	 */
	protected double applyToCoord(String name, int[] gridCoords,
													DoubleFunction<Double> f)
	{
		return this.applyToVoxel(name, this.arrayCoords(gridCoords), f);
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
	public double[] getVoxelOrigin(int[] gridCoords)
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
	public double[] getVoxelCentre(int[] gridCoords)
	{
		return Vector.add(getVoxelOrigin(gridCoords), 0.5*this._res);
	}
	
	/*************************************************************************
	 * VOXEL SETTERS
	 ************************************************************************/
	
	/**
	 * TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param value
	 */
	public void setValueAt(String name, int[] gridCoords, double value)
	{
		this.applyToCoord(name, gridCoords, (double v)->{return value;});
	}
	
	/**
	 * TODO
	 * 
	 * @param name String name of the array.
	 * @param gridCoords
	 * @param value
	 */
	public void addValueAt(String name, int[] gridCoords, double value)
	{
		this.applyToCoord(name, gridCoords, (double v)->{return v + value;});
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param gridCoords
	 * @param value
	 */
	public void timesValueAt(String name, int[] gridCoords, double value)
	{
		this.applyToCoord(name, gridCoords, (double v)->{return v * value;});
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
	public double getValueAt(String name, int[] gridCoords)
	{
		return this.applyToCoord(name, gridCoords, (double v)->{return v;});
	}
	
	/**
	 * \brief TODO 
	 * 
	 * Consider replacing with some sort of neighbour coordinate iterator?
	 * 
	 * @param gridCoords
	 * @return
	 */
	public ArrayList<Double> getNeighborValues(String name, int[] gridCoords)
	{
		ArrayList<Double> out = new ArrayList<Double>();
		int[] temp = Vector.copy(gridCoords);
		for ( int axis = 0; axis < 3; axis++ )
		{
			temp[axis] -= 1;
			try { out.add(this.getValueAt(name, temp)); } 
			catch (ArrayIndexOutOfBoundsException e) {}
			temp[axis] += 2;
			try { out.add(this.getValueAt(name, temp)); } 
			catch (ArrayIndexOutOfBoundsException e) {}
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
	private void applyToCore(String name, DoubleFunction<Double> f)
	{
		double[][][] array = this._array.get(name);
		for ( int i = _padding[0]; i < _padding[0] + _nVoxel[0]; i++ )
			for ( int j = _padding[1]; j < _padding[1] + _nVoxel[1]; j++ )
				for ( int k = _padding[2]; k < _padding[2] + _nVoxel[2]; k++ )
				{
					array[i][j][k] = f.apply(array[i][j][k]);
				}
	}
	
	/**
	 * \brief Set all voxels to the <b>value</b> given.
	 * 
	 * @param value double value to use.
	 */
	public void setAllTo(String name, double value, boolean includePadding )
	{
		if ( includePadding )
			Array.setAll(this._array.get(name), value);
		else
			this.applyToCore(name, (double v)->{return value;});
	}
	
	/**
	 * TODO
	 * 
	 * @param array
	 */
	public void setTo(String name, double[][][] array)
	{
		Array.setAll(this._array.get(name), array);
	}
	
	/**
	 * TODO
	 * 
	 * @param value
	 */
	public void addToAll(String name, double value, boolean includePadding)
	{
		if ( includePadding )
			Array.add(this._array.get(name), value);
		else
			this.applyToCore(name, (double v)->{return v + value;});
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param value
	 * @param includePadding
	 */
	public void timesAll(String name, double value, boolean includePadding)
	{
		if ( includePadding )
			Array.times(this._array.get(name), value);
		else
			this.applyToCore(name, (double v)->{return v * value;});
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
	public double getMax(String name)
	{
		return Array.max(this._array.get(name));
	}
	
	/**
	 * \brief Returns the least value of the voxels in this grid.
	 * 
	 * TODO exclude padding?
	 * 
	 * @return
	 */
	public double getMin(String name)
	{
		return Array.min(this._array.get(name));
	}
	
	/*************************************************************************
	 * TWO-ARRAY METHODS
	 ************************************************************************/
	
	private void applyArrayToArray(String destination, String source,
										ToDoubleBiFunction<Double, Double> f)
	{
		double[][][] dest = this._array.get(destination);
		double[][][] src = this._array.get(source);
		for ( int i = _padding[0]; i < _padding[0] + _nVoxel[0]; i++ )
			for ( int j = _padding[1]; j < _padding[1] + _nVoxel[1]; j++ )
				for ( int k = _padding[2]; k < _padding[2] + _nVoxel[2]; k++ )
				{
					dest[i][j][k] = 
								f.applyAsDouble(dest[i][j][k], src[i][j][k]);
				}
	}
	
	public void addArrayToArray(String destination, String source, 
													boolean includePadding)
	{
		if ( includePadding )
			Array.add(this._array.get(destination), this._array.get(source));
		else
		{
			this.applyArrayToArray(destination, source, 
					(Double d, Double s) -> {return d + s;});
		}
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
	protected double differential(String name, int[] gridCoords, int axis)
	{
		int[] temp = Vector.copy(gridCoords);
		double out = -2.0 * getValueAt(name, temp);
		temp[axis] += 1;
		out += getValueAt(name, temp);
		temp[axis] -= 2;
		out += getValueAt(name, temp);
		out /= 2.0 * this._res;
		return ( Double.isFinite(out) ) ? out : 0.0;
	}
	
	protected double[] gradient(String name, int[] gridCoords)
	{
		double[] out = new double[3];
		for ( int axis = 0; axis < 3; axis++ )
			out[axis] = differential(name, gridCoords, axis);
		return out;
	}
	
	/*************************************************************************
	 * ITERATOR
	 ************************************************************************/
	
	/**
	 * TODO
	 * 
	 */
	public int[] resetIterator()
	{
		this._currentCoord = Vector.zerosInt(3);
		return this._currentCoord;
	}
	
	/**
	 * TODO
	 * 
	 * @param axis
	 * @return
	 */
	private boolean iteratorExceeds(int axis)
	{
		return _currentCoord[axis] >=  this._nVoxel[axis];
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public boolean isIteratorValid()
	{
		for ( int axis = 0; axis < 3; axis++ )
			if ( iteratorExceeds(axis) )
				return false;
		return true;
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
			_currentCoord[0] = 0;
			_currentCoord[1]++;
			if ( this.iteratorExceeds(1) )
			{
				_currentCoord[1] = 0;
				_currentCoord[2]++;
				if ( this.iteratorExceeds(2) )
				{
					//throw new IllegalStateException(
					//						"Iterator exceeds boundaries.");
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
	 * REPORTING
	 ************************************************************************/
	
	public StringBuffer arrayAsText(String name)
	{
		StringBuffer out = new StringBuffer("");
		//double[][][] array = this._array.get(name);
		// TODO
		return out;
	}
	
}
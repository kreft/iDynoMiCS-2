package grid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.DoubleFunction;

import linearAlgebra.*;
import idynomics.Compartment.BoundarySide;

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
public class CartesianGrid extends SpatialGrid
{
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
	public CartesianGrid(int[] nVoxel, double resolution)
	{
		this._nVoxel = Vector.copy(nVoxel);
		this._res = resolution;
	}

	public CartesianGrid()
	{
		this._nVoxel = Vector.vector(3, 1);
		this._res = 1.0;
	}

	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param initialValues
	 */
	public void newArray(ArrayType type, double initialValues)
	{
		/*
		 * First check that the array HashMap has been created.
		 */
		if ( this._array == null )
			this._array = new HashMap<ArrayType, double[][][]>();
		/*
		 * Now try resetting all values of this array. If it doesn't exist
		 * yet, make it.
		 */
		if ( this._array.containsKey(type) )
			Array.setAll(this._array.get(type), 0.0);
		else
		{
			double[][][] array = Array.array(this._nVoxel[0], this._nVoxel[1],
											this._nVoxel[2], initialValues);
			this._array.put(type, array);
		}
	}

	/*************************************************************************
	 * SIMPLE GETTERS
	 ************************************************************************/

	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public double[][][] getArray(ArrayType type)
	{
		return Array.copy(this._array.get(type));
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

	private int[] checkCoords(int[] coord)
	{
		int[] out = Vector.copy(coord);
		if ( this._nVoxel[0] > 1 )
		{
			if ( out[0] < 0 )
				out = _boundaries.get(BoundarySide.XMIN).getCorrectCoord(out);
			if ( out[0] >= _nVoxel[0] )
				out = _boundaries.get(BoundarySide.XMAX).getCorrectCoord(out);
		}
		if ( this._nVoxel[1] > 1 )
		{
			if ( out[1] < 0 )
				out = _boundaries.get(BoundarySide.YMIN).getCorrectCoord(out);
			if ( out[1] >= _nVoxel[0] )
				out = _boundaries.get(BoundarySide.YMAX).getCorrectCoord(out);
		}
		if ( this._nVoxel[2] > 1 )
		{
			if ( out[2] < 0 )
				out = _boundaries.get(BoundarySide.ZMIN).getCorrectCoord(out);
			if ( out[2] >= _nVoxel[2] )
				out = _boundaries.get(BoundarySide.ZMAX).getCorrectCoord(out);
		}
		return out;
	}

	public double getValueAt(ArrayType type, int[] coord)
	{
		double[][][] array = this._array.get(type);
		int[] corrected = this.checkCoords(coord);
		if ( corrected != null )
			return array[corrected[0]][corrected[1]][corrected[2]];
		else
			return Double.NaN;
	}

	/**
	 * 
	 * @param arrayName
	 * @param coord
	 * @param newValue
	 * @return Whether or not the assignment was successful.
	 */
	public boolean setValueAtNew(ArrayType type, int[] coord, double newValue)
	{
		double[][][] array = this._array.get(type);
		int[] corrected = this.checkCoords(coord);
		if ( corrected != null )
		{
			array[corrected[0]][corrected[1]][corrected[2]] = newValue;
			return true;
		}
		else
			return false;
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
	public double[] getVoxelOrigin(int[] coords)
	{
		int[] temp = Vector.copy(coords);
		return Vector.times(Vector.toDbl(temp), this._res);
	}

	/**
	 * \brief TODO
	 * 
	 * @param gridCoords
	 * @return
	 */
	public double[] getVoxelCentre(int[] coords)
	{
		return Vector.add(getVoxelOrigin(coords), 0.5*this._res);
	}

	/*************************************************************************
	 * BOUNDARIES
	 ************************************************************************/

	/**
	 * \brief TODO
	 * 
	 * TODO This doesn't check for the case that multiple boundaries have been
	 * crossed!
	 * 
	 * @param coord
	 * @return
	 */
	protected BoundarySide isOutside(int[] coord)
	{
		if ( coord[0] < 0 )
			return BoundarySide.XMIN;
		if ( coord[0] >= this._nVoxel[0] )
			return BoundarySide.XMAX;
		if ( coord[1] < 0 )
			return BoundarySide.YMIN;
		if ( coord[1] >= this._nVoxel[1] )
			return BoundarySide.YMAX;
		if ( coord[2] < 0 )
			return BoundarySide.ZMIN;
		if ( coord[2] >= this._nVoxel[2] )
			return BoundarySide.ZMAX;
		return null;
	}

	/*************************************************************************
	 * VOXEL GETTERS & SETTERS
	 ************************************************************************/

	/**
	 * \brief TODO
	 * 
	 * 
	 * @param gridCoords
	 * @return
	 */
	public double getValueAtOLD(ArrayType type, int[] gridCoords)
	{
		return this.applyToVoxel(type, gridCoords, (double v)->{return v;});
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

	/*************************************************************************
	 * ARRAY SETTERS
	 ************************************************************************/
	/**
	 * \brief Set all voxels to the <b>value</b> given.
	 * 
	 * @param value double value to use.
	 */
	public void setAllTo(ArrayType type, double value)
	{
		Array.setAll(this._array.get(type), value);
	}

	/**
	 * TODO
	 * 
	 * @param array
	 */
	public void setTo(ArrayType type, double[][][] array)
	{
		Array.setAll(this._array.get(type), array);
	}

	/**
	 * TODO
	 * 
	 * @param value
	 */
	public void addToAll(ArrayType type, double value)
	{
		Array.add(this._array.get(type), value);
	}

	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param value
	 */
	public void timesAll(ArrayType type, double value)
	{
		Array.times(this._array.get(type), value);
	}

	/*************************************************************************
	 * ARRAY GETTERS
	 ************************************************************************/

	/**
	 * \brief Returns the greatest value of the voxels in this grid.
	 * 
	 * @return
	 */
	public double getMax(ArrayType type)
	{
		return Array.max(this._array.get(type));
	}

	/**
	 * \brief Returns the least value of the voxels in this grid.
	 * 
	 * @return
	 */
	public double getMin(ArrayType type)
	{
		return Array.min(this._array.get(type));
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
	protected double differential(ArrayType type, int[] gridCoords, int axis)
	{
		int[] temp = Vector.copy(gridCoords);
		double out = -2.0 * getValueAt(type, temp);
		temp[axis] += 1;
		out += getValueAt(type, temp);
		temp[axis] -= 2;
		out += getValueAt(type, temp);
		out /= 2.0 * this._res;
		return ( Double.isFinite(out) ) ? out : 0.0;
	}

	protected double[] gradient(ArrayType type, int[] gridCoords)
	{
		double[] out = new double[3];
		for ( int axis = 0; axis < 3; axis++ )
			out[axis] = differential(type, gridCoords, axis);
		return out;
	}

	/*************************************************************************
	 * COORDINATE ITERATOR
	 ************************************************************************/

	/**
	 * TODO
	 * 
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
	
	public double getValueAtCurrent(ArrayType type)
	{
		return this.getValueAt(type, this._currentCoord);
	}
	
	public void setValueAtCurrent(ArrayType type, double value)
	{
		this.setValueAt(type, this._currentCoord, value);
	}
	
	/*************************************************************************
	 * NEIGHBOR ITERATOR
	 ************************************************************************/

	/**
	 * TODO
	 * 
	 */
	public int[] resetNbhIterator(boolean inclDiagonalNhbs)
	{
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.copy(this._currentCoord);
		else
			for ( int i = 0; i < 3; i++ )
				this._currentNeighbor[i] = this._currentCoord[i];
		this._inclDiagonalNhbs = inclDiagonalNhbs;
		for ( int axis = 0; axis < 3; axis++ )
			if ( this._nVoxel[axis] > 1 )
				this._currentNeighbor[axis]--;
		if ( (! this._inclDiagonalNhbs) && isDiagNbh() )
			return this.nbhIteratorNext();
		return this._currentNeighbor;
	}

	/**
	 * TODO
	 * 
	 * @param axis
	 * @return
	 */
	private boolean nbhIteratorExceeds(int axis)
	{
		/*
		 * If this is a trivial axis and we're not on it, then we're
		 * definitely in the wrong place.
		 */
		if ( this._nVoxel[axis] == 1 && 
				this._currentNeighbor[axis] != this._currentCoord[axis] )
		{
			return true;
		}
		return _currentNeighbor[axis] >  this._currentCoord[axis] + 1;
	}

	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public boolean isNbhIteratorValid()
	{
		for ( int axis = 0; axis < 3; axis++ )
			if ( nbhIteratorExceeds(axis) )
				return false;
		return true;
	}

	/**
	 * TODO
	 * 
	 * @return int[3] coordinates of next position.
	 * @exception IllegalStateException Iterator exceeds boundaries.
	 */
	public int[] nbhIteratorNext()
	{
		this._currentNeighbor[0]++;
		if ( this.nbhIteratorExceeds(0) )
		{
			this._currentNeighbor[0] = this._currentCoord[0] - 1;
			this._currentNeighbor[1]++;
			if ( this.nbhIteratorExceeds(1) )
			{
				this._currentNeighbor[1] = this._currentCoord[1] - 1;
				this._currentNeighbor[2]++;
			}
		}
		if ( Vector.areSame(this._currentNeighbor, this._currentCoord) )
			return this.nbhIteratorNext();
		if ( (! this._inclDiagonalNhbs) && isDiagNbh() )
			return this.nbhIteratorNext();
		return _currentNeighbor;
	}

	private boolean isDiagNbh()
	{
		int counter = 0;
		int diff;
		for ( int axis = 0; axis < 3; axis++ )
		{
			diff = (int) Math.abs(this._currentNeighbor[axis] - 
					this._currentCoord[axis]);
			if ( diff == 1 )
				counter++;
			if ( counter > 1 )
				return true;
		}
		return false;
	}

	/**
	 * \brief Discard the iterative coordinate.
	 */
	public void closeNbhIterator()
	{
		this._currentNeighbor = null;
	}

	/**
	 * 
	 * @return
	 */
	public GridMethod nbhIteratorIsOutside()
	{
		BoundarySide bSide = this.isOutside(this._currentNeighbor);
		System.out.println(Arrays.toString(this._currentNeighbor));
		if ( bSide == null )
			return null;
		return this._boundaries.get(bSide);
	}

	/*************************************************************************
	 * REPORTING
	 ************************************************************************/

	public String arrayAsText(ArrayType type)
	{
		String out = "";
		double[][][] array = this._array.get(type);
		for ( double[][] matrix : array )
		{
			for ( double[] row : matrix )
			{
				for ( double value : row )
					out += value + ", ";
				out += ";\n";
			}
			out += "|\n";
		}
		return out;
	}

}
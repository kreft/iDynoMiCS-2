package grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.DoubleFunction;

import dataIO.LogFile;
import grid.GridBoundary.GridMethod;
import linearAlgebra.*;
import shape.BoundarySide;

/**
 *\brief 
 * 
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public class CartesianGrid extends SpatialGrid
{
	protected int _nbhDirection;
	
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
	public CartesianGrid(int[] nVoxel, double[][] resolution)
	{
		this._nVoxel = Vector.copy(nVoxel);
		this._res = resolution;
		this.calcMinVoxelVoxelSurfaceArea();
	}
	
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
		this._res = new double[3][];
		for ( int dim = 0; dim < 3; dim++ )
		{
			this._res[dim] = new double[this._nVoxel[dim]];
			for ( int i = 0; i < this._nVoxel[dim]; i++ )
				this._res[dim][i] = resolution;
		}
		this.calcMinVoxelVoxelSurfaceArea();
	}

	public CartesianGrid()
	{
		this._nVoxel = Vector.vector(3, 1);
		this._res = new double[3][1];
		for ( int dim = 0; dim < 3; dim++ )
			this._res[dim][0] = 1.0;
		this.calcMinVoxelVoxelSurfaceArea();
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
			Array.setAll(this._array.get(type), initialValues);
		else
		{
			double[][][] array = Array.array(this._nVoxel[0], this._nVoxel[1],
											this._nVoxel[2], initialValues);
			this._array.put(type, array);
		}
	}
	
	public void calcMinVoxelVoxelSurfaceArea()
	{
		int nSA = this.numSignificantAxes();
		double out = 1.0;
		switch ( nSA )
		{
			case 0:
				out = Double.NaN;
				break;
			case 1:
				for ( int axis = 0; axis < 3; axis++ )
				{
					if ( this._nVoxel[axis] > 1 )
						continue;
					out *= this._res[axis][0];
				}
				break;
			case 2:
				double min = Double.MAX_VALUE;
				for ( int axis = 0; axis < 3; axis++ )
				{
					if ( this._nVoxel[axis] > 1 )
						min = Math.min(min, Vector.min(this._res[axis]));
					else
						out *= this._res[axis][0];
				}
				out *= out;
				break;
			case 3:
				ArrayList<Double> axes = new ArrayList<Double>(3);
				for ( int axis = 0; axis < 3; axis++ )
					axes.set(axis, Vector.min(this._res[axis]));
				Collections.sort(axes);
				out = axes.get(0) * axes.get(1);
				break;
		}
		this._minVoxVoxSurfArea = out;
	}
	
	public void calcMinVoxVoxResSq()
	{
		double m = Double.MAX_VALUE;
		for ( int axis = 0; axis < 3; axis++ )
			for ( int i = 0; i < this._nVoxel[axis] - 1; i++ )
				m = Math.min(m, this._res[axis][i] * this._res[axis][i+1]);
		this._minVoxVoxDist = m;
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
	 * \brief Returns the volume of a voxel in this grid (in cubic 
	 * micrometers).
	 * 
	 * @return double value of a voxel's volume (in um<sup>3</sup>).
	 */
	public double getVoxelVolume(int[] coord)
	{
		double out = 1.0;
		for ( int dim = 0; dim < 3; dim++ )
			out *= this._res[dim][coord[dim]];
		return out;
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
	 * TODO Safety if location is outside bounds.
	 * 
	 * @param location 
	 * @return 
	 */
	public int[] getCoords(double[] location)
	{
		int[] coord = new int[3];
		double counter;
		for ( int dim = 0; dim < 3; dim++ )
		{
			counter = 0.0;
			countLoop: for ( int i = 0; i < this._nVoxel[dim]; i++ )
			{
				counter += this._res[dim][i];
				if ( counter >= location[dim] )
				{
					coord[dim] = i;
					break countLoop;
				}
			}
		}
		return coord;
	}

	/**
	 * \brief TODO
	 * 
	 * @param gridCoords
	 * @return
	 */
	public double[] getVoxelOrigin(int[] coords)
	{
		double[] out = Vector.zerosDbl(3);
		for ( int dim = 0; dim < 3; dim++ )
			for ( int i = 0; i < coords[dim]; i++ )
				out[dim] += this._res[dim][i];
		return out;
	}

	/**
	 * \brief TODO
	 * 
	 * @param gridCoords
	 * @return
	 */
	public double[] getVoxelCentre(int[] coords)
	{
		double[] out = getVoxelOrigin(coords);
		for ( int dim = 0; dim < 3; dim++ )
			out[dim] += 0.5 * this._res[dim][coords[dim]];
		return out;
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
	
	/**
	 * \brief TODO
	 * 
	 * TODO Rob [16Nov2015]: This is far from ideal, but I can't currently see
	 * a better way of doing it.
	 * 
	 * @param bndry
	 * @param coord
	 * @return
	 */
	public int[] cyclicTransform(int[] coord)
	{
		int[] transformed = Vector.copy(coord);
		for ( int i = 0; i < 3; i++ )
		{
			transformed[i] = (transformed[i] % this._nVoxel[i]);
			if ( transformed[i] < 0 )
				transformed[i] += this._nVoxel[i];
		}
		/*System.out.println("transforming "+Arrays.toString(coord)+
				" to "+Arrays.toString(transformed)+" using nVoxel "+
				Arrays.toString(_nVoxel)); //bughunt */
		return transformed;
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
	public int[] resetNbhIterator()
	{
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.copy(this._currentCoord);
		else
			for ( int i = 0; i < 3; i++ )
				this._currentNeighbor[i] = this._currentCoord[i];
		for ( int axis = 0; axis < 3; axis++ )
			if ( this._nVoxel[axis] > 1 )
			{
				this._currentNeighbor[axis]--;
				this._nbhDirection = axis;
				return this._currentNeighbor;
			}
		return null;
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
	 */
	public int[] nbhIteratorNext()
	{
		if ( this._nbhDirection == 2 || 
				this._currentNeighbor[this._nbhDirection] < 
									this._currentCoord[this._nbhDirection] )
		{
			this._currentNeighbor[this._nbhDirection] += 2;
		}
		else
		{
			this._currentNeighbor[this._nbhDirection] = 
									this._currentCoord[this._nbhDirection];
			this._nbhDirection++;
			this._currentNeighbor[this._nbhDirection] =
								this._currentCoord[this._nbhDirection] - 1;
		}
		return this._currentNeighbor;
	}
	
	/**
	 * 
	 */
	public double getNbhSharedSurfaceArea()
	{
		double out = 1.0;
		for ( int axis = 0; axis < 3; axis++ )
		{
			if ( axis == this._nbhDirection )
				continue;
			out *= this._res[axis][this._currentCoord[axis]];
		}
		return out;
	}
	
	public double getCurrentNbhResSq()
	{
		double out = this._res[this._nbhDirection]
								[this._currentCoord[this._nbhDirection]];
		out += this._res[this._nbhDirection]
						[this._currentNeighbor[this._nbhDirection]];
		out *= 0.5;
		return Math.pow(out, 2.0);
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
	
	/*************************************************************************
	 * GRID GETTER
	 ************************************************************************/
	
	/**
	 * TODO: Bas [15.01.2016] these GridGetter methods can probably made a bit
	 * simpler by directly implementing their newGrid(...) methods rather than
	 * returning a new an anonymous GridGetter() that implements this method. 
	 **/
	
	public static final GridGetter standardGetter()
	{
		return new GridGetter()
		{
			@Override
			public SpatialGrid newGrid(int[] nVoxel, double resolution) 
			{
				return new CartesianGrid(nVoxel, resolution);
			}
		};
	}
	
	public static final GridGetter dimensionlessGetter()
	{
		return new GridGetter()
		{
			@Override
			public SpatialGrid newGrid(int[] nVoxel, double resolution) 
			{
				// TODO check this is the best way.
				return new CartesianGrid(Vector.onesInt(3), resolution);
			}
		};
	}
}

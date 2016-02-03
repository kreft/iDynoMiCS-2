package grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.DoubleFunction;

import dataIO.LogFile;
import grid.GridBoundary.GridMethod;
import grid.ResolutionCalculator.ResCalc;
import grid.SpatialGrid.ArrayType;
import linearAlgebra.*;
import shape.ShapeConventions.DimName;

/**
 * \brief Subclass of SpatialGrid that discretises space into rectilinear
 * voxels. Uses the (X, Y, Z) dimension system.
 * 
 * @author Robert Clegg, University of Birmingham (r.j.clegg@bham.ac.uk)
 */
public class CartesianGrid extends SpatialGrid
{
	/**
	 * The number of voxels this grid has in each of the three spatial 
	 * dimensions. Note that some of these may be 1 if the grid is not three-
	 * dimensional.
	 * 
	 * <p>For example, a 3 by 2 rectangle would have _nVoxel = [3, 2, 1].</p>
	 * 
	 * TODO replace with _resCalc
	 */
	protected int[] _nVoxel = new int[3];
	
	/**
	 * Grid resolution, i.e. the side length of each voxel in this grid. This
	 * has three rows, one for each dimension. Each row has length of its
	 * corresponding position in _nVoxel.
	 * 
	 * <p>For example, a 3 by 2 rectangle might have _res = 
	 * [[1.0, 1.0, 1.0], [1.0, 1.0], [1.0]]</p>
	 * 
	 * TODO replace with _resCalc
	 */
	protected double[][] _res  = new double[3][];
	
	/**
	 * 
	 * TODO replace _nVoxel and _res
	 */
	protected ResCalc[] _resCalc = new ResCalc[3];
	
	/**
	 * TODO
	 */
	protected int _nbhDirection;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Construct a CartesianGrid from a 3-vector of total dimension
	 * sizes and corresponding methods for calculating voxel resolutions. 
	 * 
	 * @param totalSize
	 * @param resCalc
	 */
	public CartesianGrid(double[] totalSize, ResCalc[] resCalc)
	{
		/* Dimension names for a CartesianGrid. */
		this._dimName[0] = DimName.X;
		this._dimName[1] = DimName.Y;
		this._dimName[2] = DimName.Z;
		/* Set up the resolutions. */
		ArrayList<Double> resolutions = new ArrayList<Double>();
		double total;
		double temp;
		for ( int dim = 0; dim < 3; dim++ )
		{
			resolutions.clear();
			total = 0.0;
			while ( total < totalSize[dim] )
			{
				temp = resCalc[dim].getResolution(resolutions.size());
				if ( (total + temp) > totalSize[dim] )
					temp = totalSize[dim] - total;
				total += temp;
				resolutions.add(temp);
			}
			this._res[dim] = new double[resolutions.size()];
			this._nVoxel[dim] = resCalc[dim].getNVoxel();
			for ( int i = 0; i < resolutions.size(); i++ )
				this._res[dim][i] = resolutions.get(i);
		}
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
		double[] totalSize = new double[3];
		
		ResolutionCalculator res = new ResolutionCalculator();
		ResCalc[] resCalc = new ResCalc[3];
		for ( int i = 0; i < 3; i++ )
		{
			totalSize[i] = nVoxel[i]*resolution;
			resCalc[i] = res.new UniformResolution();
			resCalc[i].init(resolution, totalSize[i]);
		}
		new CartesianGrid(totalSize, resCalc);
	}

	public CartesianGrid()
	{
		new CartesianGrid(Vector.onesInt(3), 1.0);
	}
	
	@Override
	public void newArray(ArrayType type, double initialValues)
	{
		/*
		 * Try resetting all values of this array. If it doesn't exist yet,
		 * make it.
		 */
		if ( this.hasArray(type) )
			Array.setAll(this._array.get(type), initialValues);
		else
			this._array.put(type, Array.array(this._nVoxel, initialValues));
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
	
	@Override
	public int[] getCoords(double[] location)
	{
		// TODO safety if location is outside?
		int[] coord = new int[3];
		double counter;
		int maxDim = Math.min(3, location.length);
		for ( int dim = 0; dim < maxDim; dim++ )
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

	
	/*************************************************************************
	 * VOXEL GETTERS & SETTERS
	 ************************************************************************/
	
	@Override
	public double[] getVoxelOrigin(int[] coords)
	{
		double[] out = Vector.zerosDbl(3);
		for ( int dim = 0; dim < 3; dim++ )
			for ( int i = 0; i < coords[dim]; i++ )
				out[dim] += this._res[dim][i];
		return out;
	}
	
	@Override
	public double[] getVoxelCentre(int[] coords)
	{
		double[] out = getVoxelOrigin(coords);
		for ( int dim = 0; dim < 3; dim++ )
			out[dim] += 0.5 * this._res[dim][coords[dim]];
		return out;
	}
	
	@Override
	public int[] getNVoxel(int[] coords)
	{
		return this._nVoxel;
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
	
	@Override
	public GridMethod nbhIteratorIsOutside()
	{
		return this.isOutside(this._currentNeighbor);
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
			public SpatialGrid newGrid(double[] totalLength, double resolution) 
			{
				ResolutionCalculator r = new ResolutionCalculator();
				
				ResCalc[] resCalc= new ResCalc[3];
				
				for (int dim=0; dim<3; dim++){
					resCalc[dim] = r.new UniformResolution();
					resCalc[dim].init(resolution, totalLength[dim]);
				};
				return new CartesianGrid(totalLength,resCalc);
			}
		};
	}
	
	public static final GridGetter dimensionlessGetter()
	{
		return new GridGetter()
		{
			@Override
			public SpatialGrid newGrid(double[] totalSize, double resolution) 
			{
				// TODO check this is the best way.
				return new CartesianGrid(Vector.onesInt(3), resolution);
			}
		};
	}
}

package grid;

import java.util.ArrayList;
import java.util.Collections;

import grid.ResolutionCalculator.ResCalc;
import grid.ResolutionCalculator.ResCalcFactory;
import grid.ResolutionCalculator.UniformResolution;
import linearAlgebra.Array;
import linearAlgebra.Vector;
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
	 * TODO
	 */
	protected ResCalc[] _resCalc = new ResCalc[3];
	
	/**
	 * TODO
	 */
	protected DimName _nbhDirection;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Construct a CartesianGrid from a 3-vector of total dimension
	 * sizes and corresponding methods for calculating voxel resolutions. 
	 * 
	 * @param totalSize 3-vector of total side lengths of each dimension.
	 * @param resCalc 
	 */
	public CartesianGrid(ResCalc[] resCalc)
	{
		/* Dimension names for a CartesianGrid. */
		this._dimName[0] = DimName.X;
		this._dimName[1] = DimName.Y;
		this._dimName[2] = DimName.Z;
		
		this._resCalc = resCalc;
		
		/* in the cartesian grid we have to call this method only once here */
		updateCurrentNVoxel();
		
		/* 
		 * Finally, pre-calculate the smallest shared surface area between
		 * neighboring voxels.
		 */
		this.calcMinVoxelVoxelSurfaceArea();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param nVoxel
	 * @param resolution
	 */
	public CartesianGrid(double[] totalLength, double resolution)
	{
		this( ResCalcFactory.createUniformResCalcForCube(totalLength, resolution));
	}

	public CartesianGrid()
	{
		this( Vector.onesDbl(3), 1.0);
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
			this._array.put(type, 
							Array.array(this._currentNVoxel, initialValues));
	}
	
	public void calcMinVoxelVoxelSurfaceArea()
	{
		int nSA = this.numSignificantAxes();
		double out = 1.0;
		ResCalc rC;
		switch ( nSA )
		{
			case 0:
				out = Double.NaN;
				break;
			case 1:
				for ( int axis = 0; axis < 3; axis++ )
				{
					rC = this.getResolutionCalculator(axis);
					if ( this._currentNVoxel[axis] > 1 )
						continue;
					out *= rC.getResolution(0);
				}
				break;
			case 2:
				double min = Double.MAX_VALUE;
				
				for ( int axis = 0; axis < 3; axis++ )
				{
					rC = this.getResolutionCalculator(axis);
					if ( this._currentNVoxel[axis] > 1 )
						min = Math.min(min, rC.getMinResolution());
					else
						out *= rC.getResolution(0);
				}
				out *= out;
				break;
			case 3:
				ArrayList<Double> axes = new ArrayList<Double>(3);
				for ( int axis = 0; axis < 3; axis++ ){
					rC = this.getResolutionCalculator(axis);
					axes.set(axis, rC.getMinResolution());
				}
				Collections.sort(axes);
				out = axes.get(0) * axes.get(1);
				break;
		}
		this._minVoxVoxSurfArea = out;
	}
	
	public void calcMinVoxVoxResSq()
	{
		double m = Double.MAX_VALUE;
		ResCalc rC;
		for ( int axis = 0; axis < 3; axis++ ){
			rC = this.getResolutionCalculator(axis);
			for ( int i = 0; i < this._currentNVoxel[axis] - 1; i++ )
				m = Math.min(m, rC.getResolution(i) * rC.getResolution(i + 1));
		}
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
			out *= this.getResolutionCalculator(dim).getResolution(coord[dim]);
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
		// TODO use ResolutionCalculator getVoxelIndex once we use it here.
		int[] coord = new int[3];
		double counter;
		int maxDim = Math.min(3, location.length);
		ResCalc rC;
		for ( int dim = 0; dim < maxDim; dim++ )
		{
			rC = getResolutionCalculator(dim);
			counter = 0.0;
			countLoop: for ( int i = 0; i < this._currentNVoxel[dim]; i++ )
			{
				counter += rC.getResolution(i);
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
	 * TODO
	 * @param axis
	 * @return
	 */
	protected ResCalc getResolutionCalculator(int axis)
	{
		return this._resCalc[axis];
	}

	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		return getResolutionCalculator(axis);
	}
	
	/*************************************************************************
	 * VOXEL GETTERS & SETTERS
	 ************************************************************************/
	
	@Override
	public int[] getNVoxel(int[] coords, int[] outNVoxel)
	{
		if (outNVoxel == null)
			outNVoxel = new int[3];
		for (int dim=0; dim<3; ++dim)
			outNVoxel[dim] = this._resCalc[dim].getNVoxel();
		return outNVoxel;
	}
	
	/*************************************************************************
	 * NEIGHBOR ITERATOR
	 ************************************************************************/

	@Override
	public int[] resetNbhIterator()
	{
		this._nbhValid = true;
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.copy(this._currentCoord);
		else
			Vector.copyTo(this._currentNeighbor, this._currentCoord);
		for ( int axis = 0; axis < 3; axis++ )
			/* See if we can take one of the neighbors in dimension 'axis'. */
			if ( this.moveNbhToMinus(axis) || this.nbhJumpOverCurrent(axis) ){
				this._nbhDirection = this._dimName[axis];
				return this._currentNeighbor;
			}
		this._nbhValid = false;
		return null;
	}


	@Override
	public int[] nbhIteratorNext()
	{
		int nbhDir = this.indexFor(this._nbhDirection);
		if ( ! this.nbhJumpOverCurrent(nbhDir)){
			nbhDir++;
			if (nbhDir < 3){
				this._nbhDirection = this._dimName[nbhDir];
				if ( ! moveNbhToMinus(nbhDir))
					return nbhIteratorNext();
				
			}
			else this._nbhValid = false;
		}
			
		return this._currentNeighbor;
	}
	
	@Override
	public double getNbhSharedSurfaceArea()
	{
		int absDiff = 0, cumulativeAbsDiff = 0;
		double area = 1.0;
		ResCalc rC;
		for ( int i = 0; i < 3; i++ )
		{
			absDiff = Math.abs(this._currentCoord[i]-this._currentNeighbor[i]);
			if ( absDiff == 0 ){
				rC = this.getResolutionCalculator(i);
				area *= rC.getResolution(this._currentCoord[i]);
			}
			else
				cumulativeAbsDiff += absDiff;
		}
		return ( cumulativeAbsDiff == 1 ) ? area : 0.0;
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
				return new CartesianGrid(totalLength, resolution);
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
				return new CartesianGrid(Vector.onesDbl(3), resolution);
			}
		};
	}
}

package grid;

import grid.resolution.ResolutionCalculator.ResCalc;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;

/**
 * \brief Abstract super class of all polar grids (Cylindrical and Spherical).
 * 
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public abstract class PolarGrid extends SpatialGrid
{
	/**
	 * The starting point for the r-axis. Default value is zero, and may never
	 * be negative.
	 */
	protected double _rMin = 0.0;
	
	/**
	 * A constant factor scaling resolutions of polar grids.
	 * Set to (π/2)<sup>-1</sup> to have quarter circles at radius 0.
	 */
	protected final static double N_ZERO_FACTOR = 2 / Math.PI;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Construct a 'dimensionless' PolarGrid.
	 * 
	 * @param totalSize
	 * @param resCalc
	 */
	public PolarGrid()
	{
		/* Polar grids always start with an R dimension. */
		this._dimName[0] = DimName.R;
	}
	
	/** TODO
	 * 
	 * @param coord
	 * @param dim
	 * @return
	 */
	protected boolean isOnBoundary(int[] coord, int dim)
		ResCalc rC = this.getResolutionCalculator(coord, dim);
		if ( coord[dim] < 0 )
				return true;
		if ( coord[dim] >= rC.getNVoxel() )
				return true;
		return false;
	}
	
	/** TODO
	 * 
	 * @param coord
	 * @param dim
	 * @return
	 */
	protected boolean isOnUndefinedBoundary(int[] coord, int dim){
		if ( coord[dim] < 0 )
            return  (this._dimBoundaries[dim][0] == null);
        if ( coord[dim] >= this.getResolutionCalculator(coord, dim).getNVoxel())
            return (this._dimBoundaries[dim][1] == null);
        return false;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param dim
	 * @param shellIndex 
	 * @return
	 */
	protected boolean setNbhFirstInNewShell(int shellIndex)
	{
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[0] = shellIndex;
		
		/*
		 * First check that the new shell is inside the grid. If we're on a
		 * defined boundary, the angular coordinate is irrelevant.
		 */
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, 0);
		if ( isOnUndefinedBoundary(this._currentNeighbor, 0) )
			return false;
		if (isOnBoundary(this._currentNeighbor, 0))
			return true;
		
		//TODO: possibly return isOnUndefinedBoundary..
		
		rC = this.getResolutionCalculator(this._currentCoord, 1);
		/*
		 * We're on an intermediate shell, so find the voxel which has the
		 * current coordinate's minimum angle inside it.
		 */
		double angle = rC.getCumulativeResolution(this._currentCoord[1] - 1);
		rC = this.getResolutionCalculator(this._currentNeighbor, 1);
		
		this._currentNeighbor[1] = rC.getVoxelIndex(angle);
		return true;
	}
	
	/**
	 * @param dim
	 * @return
	 */
	protected boolean increaseNbhByOnePolar(int dim)
	{		
		if (dim == 0 || (dim == 2 && this._dimName[2] == DimName.Z))
			throw new IllegalArgumentException(
				"dimension: "+dim+" is not a polar dimension");
		
		/* If we are on an invalid shell, we are definitely in the wrong place*/
		if (isOnBoundary(this._currentNeighbor, 0))
			return false;
		/* If we are on an invalid ring in the sphere, we are wrong*/
		if (dim == 2 && isOnBoundary(this._currentNeighbor, 1))
			return false;
		
		ResCalc rC = this.getResolutionCalculator(this._currentNeighbor, dim);
		
		/* If we are already on a boundary, return false */
		if ( this._currentNeighbor[dim] > rC.getNVoxel() - 1)
				return false;
		
		/* If increasing would push us over a null boundary, return false */
		if ( this._currentNeighbor[dim] == rC.getNVoxel() - 1)
			if ( this._dimBoundaries[dim][1] == null )
				return false;

		/*
		 * If increasing would mean we no longer overlap, return false.
		 */
		double nbhMax = rC.getCumulativeResolution(this._currentNeighbor[dim]);
		rC = this.getResolutionCalculator(this._currentCoord, dim);
		double curMax = rC.getCumulativeResolution(this._currentCoord[dim]);
		if ( nbhMax >= curMax )
			return false;
		/*
		 * Otherwise, increase and return true.
		 */
		this._currentNeighbor[dim]++;
		return true;
	}
	
	@Override
	public int[] resetIterator(){
		this._currentCoord = super.resetIterator();
		/* keep the current nVoxel pointer up to date for polar grids */
		this.updateCurrentNVoxel();		
		return this._currentCoord;
	}
	
	@Override
	public int[] iteratorNext()
	{
		this._currentCoord = super.iteratorNext();
		/* keep the current nVoxel pointer up to date for polar grids */
		this.updateCurrentNVoxel();		
		return this._currentCoord;
	}
	
	/**************************************************************************/
	/************************* UTILITY METHODS ********************************/
	/**************************************************************************/
	
	/**
	 * \brief Computes a factor that scales the number of elements for
	 * increasing  radius to keep element volume fairly constant.
	 * 
	 * @param radiusIndex Radial coordinate of all voxels in a given shell.
	 * @return A scaling factor for a given radius, based on the relative arc
	 * length at this radius.
	 */
	protected static int getFactorForShell(int radiusIndex)
	{
		/*
		 * The logic behind this scaling factor is that the length of an arc,
		 * at radius r and of angle θ (in radians), is 2 θ r. Note that the
		 * circumference of a circle is 2 π r. Since Java indices start at zero
		 * and we want the arc length of the radial center of this shell, we
		 * return 2*(radius + 0.5) = (2*radius) + 1
		 * 
		 * TODO Stefan, please check this
		 */
		return 2 * radiusIndex + 1;
	}
	
	/**
	 * \brief Converts the given resolution {@code res} to account for varying radius.
	 * 
	 * @param shell
	 * @param res
	 * @return
	 */
	public static double scaleResolutionForShell(int shell, double res){			
		return res / (N_ZERO_FACTOR * getFactorForShell(shell));
	}
	
	/**	
	 * \brief Converts the given resolution {@code res} to account for varying 
	 * radius and polar angle.
	 * 
	 * @param shell
	 * @param ring
	 * @param res
	 * @return
	 */
	public static double scaleResolutionForRing(int shell, int ring, double res){
		/*
		 * Scale phi to peak at π / 2 instead of s(shell), where it 
		 * would peak for a resolution of one. This way we can use it as
		 * an input argument for a sine (which peaks at sin(π / 2) = 1
		 * naturally). Actually we let it peak at s(shell) - 0.5 to keep
		 * things symmetric around the equator.
		 */
		double ring_scale = 0.5 * Math.PI / (getFactorForShell(shell) - 0.5);
		
		/* Compute the sine of the scaled phi-coordinate */
		double length = Math.sin(ring * ring_scale);
		// TODO: check why length can be < 0 here (possibly resolutions ~ 0)
		length = Math.max(0, length);
		
		/* Scale the result to be:
		 * Nₒ = number of voxels at r = 0 in theta dimension.
		 * sin(0) = N₀
		 * sin(π / 2) = s(shell) * N₀
		 * sin(π) = N₀
		 * This is the number of voxels in theta for resolution one.
		 */
		length = N_ZERO_FACTOR 
					+ N_ZERO_FACTOR * length * (getFactorForShell(shell) - 1);
		// TODO Rob[16Feb2016]: would this be clearer?
		// length = N_ZERO_FACTOR * ( 1 + length*( 2 * shell ) );
		/* Scale the resolution to account for the additional voxels */
		return res / length;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param d
	 * @param len_cur
	 * @param bounds
	 * @param bounds_nbh
	 * @param len_nbh
	 * @return
	 */
	protected static double getSharedArea(int d, double len_cur, 
			double[] bounds, double[] bounds_nbh, double len_nbh)
	{
		boolean isRight, isLeft, isInBetween;
		double sA, len_s;
		/*
		 * theta1 of neighbor <= theta1 of current coordinate
		 * (right, counter-clockwise)
		 */
		if ( d < 0 )
		{
			isRight = bounds_nbh[0] <= bounds[0];
			isLeft = bounds_nbh[1] >= bounds[1];
			isInBetween = isLeft && isRight;
			len_s = len_cur;
		}
		else
		{
			isRight = bounds_nbh[0] < bounds[0];
			isLeft = bounds_nbh[1] > bounds[1];
			isInBetween = ! ( isLeft || isRight );
			len_s = len_nbh;
		}
		/* Find shared surface area based on relative position. */
		if ( isInBetween )
			sA = 1.0;
		else if ( isRight )
			sA = (bounds_nbh[1]-bounds[0])/len_s;
		else
		{
			/* is_left */
			sA = (bounds[1]-bounds_nbh[0])/len_s;
		}
		return sA;
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

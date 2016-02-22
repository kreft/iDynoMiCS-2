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
	
	protected final double MIN_NBH_ANGLE_DIFF = 0.5;
	
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
	
	
	/**
	 * \brief Computes the minimal total angle difference to be accepted as 
	 * 		  neighbor, divided by the central radius of the current coordinate.
	 * 
	 * This is the minimal shared arc length to be accepted as neighbor.
	 * 
	 * @return The minimal shared arc length to be accepted as neighbor of the
	 * 			 current coordinate.
	 */
	protected double getCurrentArcLengthDiffCutoff(){
		if (this._currentCoord[0] == 0) return MIN_NBH_ANGLE_DIFF;
		return MIN_NBH_ANGLE_DIFF / (this._currentCoord[0] - 0.5);
	}
	
	/** TODO
	 * 
	 * @param coord
	 * @param dim
	 * @return
	 */
	protected boolean isOnBoundary(int[] coord, int dim)
	{
		if ( coord[dim] < 0 )
				return true;
		if ( coord[dim] >= this.getResolutionCalculator(coord, dim).getNVoxel())
				return true;
		return false;
	}
	
	/** TODO
	 * 
	 * @param coord
	 * @param dim
	 * @return
	 */
	protected boolean isOnUndefinedBoundary(int[] coord, int dim)
	{
		if ( coord[dim] < 0 )
            return  this.isBoundaryDefined(dim, 0);
        if ( coord[dim] >= this.getResolutionCalculator(coord, dim).getNVoxel())
            return this.isBoundaryDefined(dim, 1);
        return false;
	}
	
	/**
	 * \brief Used to move neighborhood iterator into a new shell.
	 * 
	 * May override first and second dimension, while leaving third unchanged.
	 * 
	 * @param dim
	 * @param shellIndex 
	 * @return
	 */
	protected boolean setNbhFirstInNewShell(int shellIndex)
	{
		//TODO: does sometimes start a bit too early (?) (possibly rounding...)
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
		
		rC = this.getResolutionCalculator(this._currentCoord, 1);
		/*
		 * We're on an intermediate shell, so find the voxel which has the
		 * current coordinate's minimum angle inside it.
		 */
		double cur_min = rC.getCumulativeResolution(this._currentCoord[1] - 1);
		double cur_max = rC.getCumulativeResolution(this._currentCoord[1]);
		rC = this.getResolutionCalculator(this._currentNeighbor, 1);
		
		int idx = rC.getVoxelIndex(cur_min);
		double nbh_max = rC.getCumulativeResolution(idx);
		
		/* if we do not overlap 'enough', try to take next */
		double th = getCurrentArcLengthDiffCutoff();
		if (cur_min >= nbh_max - th)
			/* nbh_max is actually nbh_min now */
			if (cur_max - th >= nbh_max)
				idx++;
			else 
				return false;
			
		this._currentNeighbor[1] = idx;
		return true;
	}
	
	/**
	 * @param dim
	 * @return
	 */
	protected boolean increaseNbhByOnePolar(int dim)
	{		
		//TODO: does sometimes step a bit too long (possibly rounding...)
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
			if ( this.isBoundaryDefined(dim, 1) )
				return false;

		/*
		 * If increasing would mean we no longer overlap, return false.
		 */
		double nbhMin = rC.getCumulativeResolution(this._currentNeighbor[dim]);
		rC = this.getResolutionCalculator(this._currentCoord, dim);
		double curMax = rC.getCumulativeResolution(this._currentCoord[dim]);
		if (nbhMin >= curMax - getCurrentArcLengthDiffCutoff()){
			return false;
		}
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
		 * The area enclosed by two polar curves r₀(θ) and r₁(θ) and two 
		 * polar angles θ₀ and θ₁ is:
		 *  A(r₀, r₁, θ₀, θ₁) := 1/2 (r₁^2-r₀^2) (θ₁-θ₀)			(1)
		 * If we set,  
		 *  θ₀ := 0 and
		 *  r₁ := r₀ + 1 (since java indices start at 0 and we assume res 1), 
		 * this simplifies to:
		 *  A(r) = (r + 1/2) θ₁
		 * and we can determine the angle θ for which we have an area of a 
		 *  circle at r = 1, independent from r:
		 *  A(1) = A(r) = pi 
		 *  -> pi = (r + 1/2) θ
		 *	-> θ = (2 pi)/(2 r + 1).
		 * So the factor to scale the polar angles with varying r is the 
		 *  circumference of the circle at r = 1 divided by θ:
		 * s(r) = (2 pi) / θ = 2 r + 1.
		 *  
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
		/* 
		 * getFactorForShell(shell) will return a scaling factor to have an area 
		 * of A(1) = pi throughout the grid (see getFactorForShell).
		 * 
		 * So we first scale this factor to have an area of pi / 4 (this means
		 * quarter circles at r = 1) throughout the grid and divide the target
		 * resolution by this factor. 
		 *TODO: Stefan[18Feb2016]: we could also set N_ZERO_FACTOR not to have
		 *		quarter circles but a resolution of one throughout the grid.
		 */
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
		 * The scale factor 
		 * (<a href="http://mathworld.wolfram.com/ScaleFactor.html">here</a>)
		 * for the azimuthal angle is r sin phi.
		 * 
		 * Since we assume the rings to be scaled for varying radius already 
		 * and we do not move in between shells, it is sufficient to scale the 
		 * number of voxels in azimuthal dimension according to a sine. 
		 * 
		 * So because shell and ring are given in coordinate space, we have to 
		 * scale the ring to peak at π / 2 instead of getFactorForShell(shell), 
		 * where it would peak for a resolution of one. 
		 * 
		 * (Actually we let it peak at s(shell) - 0.5 to keep
		 * things symmetric around the equator).
		 */
		double ring_scale = 0.5 * Math.PI / (getFactorForShell(shell) - 0.5);
		
		/* Compute the sine of the scaled phi-coordinate */
		double length = Math.sin(ring * ring_scale);
		// TODO: check why length can be < 0 here (possibly resolutions ~ 0)
		length = Math.max(0, length);
		
		/* Scale the result to be in coordinate space again:
		 * Nₒ = number of voxels at r = 0 in θ dimension.
		 * sin(0) = N₀
		 * sin(π / 2) = s(shell) * N₀
		 * sin(π) = N₀
		 * This is the number of voxels in θ for resolution one.
		 */
		 length = N_ZERO_FACTOR * ( 1 + length * ( 2 * shell ) );
		/* Scale the resolution to account for the additional voxels */
		return res / length;
	}
	
	/**
	 * \brief TODO
	 * 
	 * Only to be called for polar dimensions.
	 * 
	 * @param d
	 * @param len_cur
	 * @param bounds
	 * @param bounds_nbh
	 * @param len_nbh
	 * @return
	 */
	protected double getNbhSharedArcLength(int dim)
	{
		int[] cur = this._currentCoord;
		int[] nbh = this._currentNeighbor;
		boolean is_beneath = nbh[dim - 1] < cur[dim - 1];
		/* when dim = 2 (sphere), r is actually the same for current and nbh */
		int r = is_beneath ? cur[0] : nbh[0];
		ResCalc rcCur = getResolutionCalculator(cur, dim);
		double cur_min = rcCur.getCumulativeResolution(cur[dim] - 1);
		double cur_max = rcCur.getCumulativeResolution(cur[dim]);
		
		if (isOnBoundary(nbh, 0))
			return r * (cur_max - cur_min);
		ResCalc rcNbh = getResolutionCalculator(nbh, dim);
		
		double nbh_min = rcNbh.getCumulativeResolution(nbh[dim] - 1); 
		double nbh_max = rcNbh.getCumulativeResolution(nbh[dim]);

		boolean isLeft, isRight, isInBetween;
		double sl, len;
		if ( is_beneath )
		{
			isLeft = nbh_min <= cur_min;
			isRight = nbh_max >= cur_max;
			isInBetween = isRight && isLeft;
			len = cur_max - cur_min;
		}
		else
		{
			isLeft = nbh_min < cur_min;
			isRight = nbh_max > cur_max;
			isInBetween = ! ( isRight || isLeft );
			len = nbh_max - nbh_min;
		}
		/* Find shared length based on relative position. */
//		System.out.println(r+"  "+(nbh_max - cur_min) + " "+ (cur_max - nbh_min));
		if ( isInBetween )
			sl = r * len;
		else if ( isLeft )
			sl = r * (nbh_max - cur_min);
		/* is_left */
		else
			sl = r * (cur_max - nbh_min);
		if (sl==0) throw new RuntimeException();
		return sl;
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

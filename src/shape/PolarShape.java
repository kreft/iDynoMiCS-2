package shape;

import static shape.ShapeConventions.DimName.R;

import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import shape.resolution.ResolutionCalculator.ResCalc;

public abstract class PolarShape extends Shape
{
	
	/**
	 * A constant factor scaling resolutions of polar grids.
	 * Set to (π/2)<sup>-1</sup> to have quarter circles at radius 0.
	 */
	protected final static double N_ZERO_FACTOR = 2 / Math.PI;
	
	@Override
	public double nbhCurrDistance()
	{
		int nDim = this.getNumberOfDimensions();
		double distance = 0.0;
		double temp;
		DimName dim;
		ResCalc rC;
		/*
		 * Find the average radius, as this will be useful in calculating arc
		 * lengths of angular differences.
		 */
		double meanR = this.meanNbhCurrRadius();
		/*
		 * Loop over all dimensions, increasing the distance accordingly.
		 */
		for ( int i = 0; i < nDim; i++ )
		{
			dim = this.getDimensionName(i);
			rC = this.getResolutionCalculator(this._currentCoord, i);
			temp = rC.getPosition(this._currentCoord[i], 0.5);
			rC = this.getResolutionCalculator(this._currentNeighbor, i);
			temp -= rC.getPosition(this._currentNeighbor[i], 0.5);
			/* We need the arc length for angular dimensions. */
			if ( dim.isAngular() )
				temp *= meanR;
			/* Use Pythagoras to update the distance. */
			distance = Math.hypot(distance, temp);
		}
		return distance;
	}
	
	@Override
	public double nbhCurrSharedArea()
	{
		double area = 1.0;
		double meanR = this.meanNbhCurrRadius();
		int nDim = this.getNumberOfDimensions();
		ResCalc rC;
		double temp;
		int index = 0;
		for ( DimName dim : this.getDimensionNames() )
		{
			if ( dim.equals(this._nbhDimName) 
					|| !this.getDimension(dim).isSignificant() )
				continue;
			
			index = this.getDimensionIndex(dim);
			rC = this.getResolutionCalculator(this._currentCoord, index);
			
			temp = ( index >= nDim ) ? rC.getResolution(0) :
										this.getNbhSharedLength(index);
			/* We need the arc length for angular dimensions. */
			if ( dim.isAngular() )
				temp *= meanR;
			area *= temp;
		}
		return area;
	}
	
	/**
	 * @return Average radius of the current iterator voxel and of the neighbor
	 * voxel.
	 */
	private double meanNbhCurrRadius()
	{
		int i = this.getDimensionIndex(R);
		return 0.5 * (this._currentCoord[i] + this._currentNeighbor[i]);
	}
	
	/**
	 * \brief Used to move neighbor iterator into a new shell.
	 * 
	 * <p>May change first and second coordinates, while moving the third to 
	 * the current coordinate.</p>
	 * 
	 * @param shellIndex Index of the shell you want to move the neighbor
	 * iterator into.
	 * @return True is this was successful, false if it was not.
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
		if ( this.isOnUndefinedBoundary(this._currentNeighbor, DimName.R) )
			return false;
		if ( this.isOnBoundary(this._currentNeighbor, 0)){
			this._nbhOnDefBoundary = true; 
			this._nbhDimName = DimName.R;
			this._nbhDirection = this._currentCoord[0] 
									< this._currentNeighbor[0] ? 1 : 0;
			return true;
		}
		/*
		 * We're on an intermediate shell, so find the voxel which has the
		 * current coordinate's minimum angle inside it (which must exist!).
		 */
		rC = this.getResolutionCalculator(this._currentCoord, 1);
		double cur_min = rC.getCumulativeResolution(this._currentCoord[1] - 1);
		rC = this.getResolutionCalculator(this._currentNeighbor, 1);
		int new_index = rC.getVoxelIndex(cur_min);
		this._currentNeighbor[1] = new_index;
		this._nbhOnDefBoundary = false; 
		/* we are always in the same z-slice as the current coordinate when
		 * calling this method, so _nbhDimName can not be Z. 
		 */
		this._nbhDimName = this._currentCoord[0] == this._currentNeighbor[0] ?
										DimName.THETA : DimName.R;
		int dimIdx = getDimensionIndex(this._nbhDimName);
		this._nbhDirection = 
				this._currentCoord[dimIdx]
						< this._currentNeighbor[dimIdx] ? 1 : 0;
		return true;
	}
	
	/**
	 * \brief Try to increase the neighbor iterator by one in the given
	 * dimension.
	 * 
	 * @param dim Name of the dimension required.
	 * @return True is this was successful, false if it was not.
	 */
	protected boolean increaseNbhByOnePolar(DimName dim)
	{
		int index = this.getDimensionIndex(dim);
		Dimension dimension = this.getDimension(dim);
		ResCalc rC = this.getResolutionCalculator(this._currentNeighbor, index);
		/* If we are already on the maximum boundary, we cannot go further. */
		if ( this._currentNeighbor[index] > rC.getNVoxel() - 1 )
				return false;
		/* Do not allow the neighbor to be on an undefined maximum boundary. */
		if ( this._currentNeighbor[index] == rC.getNVoxel() - 1 )
			if ( dimension.isBoundaryDefined(1) )
			{
				this._nbhOnDefBoundary = true;
				this._nbhDirection = 1;
			}	
			else return false;
				
		/*
		 * If increasing would mean we no longer overlap, report failure.
		 */
		double nbhMax = rC.getCumulativeResolution(this._currentNeighbor[index]);
		rC = this.getResolutionCalculator(this._currentCoord, index);
		double curMax = rC.getCumulativeResolution(this._currentCoord[index]);
		if ( nbhMax >= curMax )
			return false;
		/*
		 * All checks have passed, so increase and report success.
		 */
		this._currentNeighbor[index]++;
		return true;
	}
	

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
	protected static double scaleResolutionForShell(int shell, double res){	
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
	protected static double scaleResolutionForRing(int shell, int ring, double res){
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
}

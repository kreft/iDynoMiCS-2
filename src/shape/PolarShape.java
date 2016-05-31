package shape;

import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;
import static shape.Shape.WhereAmI.DEFINED;
import static shape.Shape.WhereAmI.UNDEFINED;
import static shape.Shape.WhereAmI.INSIDE;

import java.util.Arrays;

import dataIO.Log;
import dataIO.Log.Tier;

import static shape.Shape.WhereAmI.CYCLIC;

import linearAlgebra.Vector;
import shape.Dimension.DimName;
import shape.resolution.ResolutionCalculator.ResCalc;

public abstract class PolarShape extends Shape
{
	
	@Override
	public double nbhCurrDistance()
	{
		Tier level = Tier.DEBUG;
		Log.out(level, "  calculating distance between voxels "+
				Vector.toString(this._currentCoord)+" and "+
				Vector.toString(this._currentNeighbor));
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
		Log.out(level, "    distance is "+distance);
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
		Log.out(NHB_ITER_LEVEL, "  trying to set neighbor in new shell "+
				shellIndex);
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[0] = shellIndex;
		this._nbhDimName = R;
		/*
		 * First check that the new shell is inside the grid. If we're on a
		 * defined boundary, the angular coordinate is irrelevant.
		 */
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, 0);
		WhereAmI where = this.whereIsNhb(R);
		if ( where == UNDEFINED )
			return false;
		if ( where == DEFINED || where == CYCLIC)
		{
			this._nbhDimName = R;
			this._nbhDirection = this._currentCoord[0] 
									< this._currentNeighbor[0] ? 1 : 0;
			this._whereIsNbh = where;
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
		/* we are always in the same z-slice as the current coordinate when
		 * calling this method, so _nbhDimName can not be Z. 
		 */
		this._nbhDimName = this._currentCoord[0] == this._currentNeighbor[0] ?
										THETA : R;
		int dimIdx = getDimensionIndex(this._nbhDimName);
		this._nbhDirection = 
				this._currentCoord[dimIdx]
						< this._currentNeighbor[dimIdx] ? 1 : 0;
		this._whereIsNbh = WhereAmI.INSIDE;
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
		Log.out(NHB_ITER_LEVEL, "  trying to increase neighbor "
			  + Arrays.toString(this._currentNeighbor)+" by one polar in "+dim);
		/* avoid increasing on any boundaries */
		int index = this.getDimensionIndex(dim);
		if (whereIsNhb(this._nbhDimName) != INSIDE) 
			return false;
		Dimension dimension = this.getDimension(dim);
		ResCalc rC = this.getResolutionCalculator(this._currentNeighbor, index);
		/* If we are already on the maximum boundary, we cannot go further. */
		if ( this._currentNeighbor[index] > rC.getNVoxel() - 1 )
			return false;
		/* Do not allow the neighbor to be on an undefined maximum boundary. */
		if ( this._currentNeighbor[index] == rC.getNVoxel() - 1 )
		{
			if ( dimension.isBoundaryDefined(1) )
			{
				this._whereIsNbh = DEFINED;
				this._nbhDirection = 1;
				this._nbhDimName = dim;
				this._currentNeighbor[index]++;
				return true;
			}	
			else
				return false;
		}
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
	 * \brief Converts the given resolution {@code res} .
	 * 
	 * @param shell Index of the shell.
	 * @param res Target resolution, in units of "quarter circles"
	 * @return Target resolution, in radians.
	 */
	protected static double scaleResolutionForShell(int shell, double res)
	{
		/*
		 * The number "2 * (index of the shell) + 1" represents the radius of
		 * an imaginary arc running along the centre of the shell.
		 */
		double factor = ( 2 * shell + 1);
		/*
		 * Divide pi/2 by this number, so that the resolution is scaled to a 
		 * quarter circle. If res is one and shell is zero (i.e. the innermost
		 * shell), then this method will return pi/2 radians.
		 */
		factor = Math.PI * 0.5 / factor;
		/*
		 * Multiply res by this factor to convert it from "units of quarter
		 * circle" to radians.
		 */
		return res * factor;
	}

	/**	
	 * \brief Converts the given resolution {@code res} to account for varying 
	 * radius and polar angle.
	 * 
	 * @param shell Index of the shell.
	 * @param ring Index of the ring.
	 * @param res Target resolution, in units of "quarter circles"
	 * @return Target resolution, in radians.
	 */
	protected double scaleResolutionForRing(int shell, int ring,
												double ring_res, double res){
		/* scale theta resolution to have a volume of one for resolution one */
		return res * 3.0 / ((1 + 3 * shell * (1 + shell)) 
			* (Math.cos(ring * ring_res) - Math.cos((1.0 + ring) * ring_res)));
	}
}

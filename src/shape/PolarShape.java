package shape;

import java.util.Arrays;

import dataIO.Log;
import dataIO.Log.Tier;

import static shape.Dimension.DimName;
import static shape.Dimension.DimName.*;
import static shape.Shape.WhereAmI.*;


import linearAlgebra.Vector;
import shape.resolution.ResolutionCalculator.ResCalc;
import utility.ExtraMath;

public abstract class PolarShape extends Shape
{
	/**
	 * tolerance when comparing polar angles for equality
	 */
	public final static double POLAR_ANGLE_EQ_TOL = 1e-6;
	
	@Override
	public double nbhCurrSharedArea()
	{
		Tier level = Tier.BULK;
		double area = 1.0;
		double temp;
		DimName dimName;
		// TODO 
		double meanR = this.meanNbhCurrRadius();
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "calculated meanR "+ meanR +" for current coord"
					+ Arrays.toString(this._currentCoord) + " and nhb "
					+ Arrays.toString(this._currentNeighbor));
		}
		for ( int i = 0; i < this.getNumberOfDimensions(); i++ )
		{
			dimName = this.getDimensionName(i);
			/* 
			 * We are on a defined boundary, so take the length of the
			 * current coordinate and don't care for other dimensions.
			 */
			if ( (this._whereIsNhb == DEFINED || this._whereIsNhb == CYCLIC)
					&& this._nhbDimName == dimName)
			{
				if ( Log.shouldWrite(level) )
					Log.out(level, "  on boundary for dim "+dimName);
				/* 
				 * Take the resolution of the next dimension, i.e. if we move
				 * to an outer shell in the circle (dim R), the THETA length 
				 * should be taken!
				 */
				temp = this.getResolutionCalculator(this._currentCoord, i + 1)
							.getResolution(this._currentCoord[i]);
				/* Move to last dim, so we will break cleanly. */
				i = this.getNumberOfDimensions();
			}
			else
				//TODO: security if on undefined boundary?
				temp = this.getNbhSharedLength(i);
			
			/* We need the arc length for angular dimensions. */
			if ( dimName.isAngular() )
				temp *= meanR;
			/*
			 * If the area is zero for some reason, skip it (e.g. if the nhb is
			 * moving along this dimension, or the precision tolerance for 
			 * comparison was not met).
			 */
			if ( temp == 0 )
			{
				if ( Log.shouldWrite(level) )
					Log.out(level, "  skipping zero area in dim "+dimName);
				continue;
			}
			if ( Log.shouldWrite(level) )
				Log.out(level, "  Shared length for dim "+dimName +" is " + temp);
			area *= temp;
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, " returning area "+area);
		return area;
	}
	
	/**
	 * @return Average radius of the current iterator voxel and of the neighbor
	 * voxel.
	 */
	// TODO To call this a mean wouldn't be quite accurate, were we to use
	// variable resolutions
	private double meanNbhCurrRadius()
	{
		/* 
		 * The average radius is the origin radius of the current coordinate if 
		 * the neighbor's direction is towards negative.
		 * If the direction is positive, the average radius is the upper
		 * radius of the current coordinate.
		 */
		int i = this.getDimensionIndex(R);
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, i);
		if ( this.isNhbIteratorInside() )
		{
			if (this._currentCoord[i] > this._currentNeighbor[i])
				return rC.getCumulativeResolution(this._currentCoord[i] - 1);
			if (this._currentCoord[i] == this._currentNeighbor[i])
				return rC.getPosition(this._currentCoord[i], 0.5);
		}
		if ( this.isNbhIteratorValid() )
		{
			/* If the neighbor is inside with same radius as the current coord 
			 * or on a defined boundary, return the current coordinates radius*/
			return rC.getCumulativeResolution(this._currentCoord[i]);
		}
		/* If the neighbor is on an undefined boundary, return NaN radius
		(this should never happen!) */
		return Double.NaN;
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
		//TODO this will currently not set onto min boundary?
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "trying to set neighbor in new shell "+
				shellIndex);
		}
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[0] = shellIndex;
		this._nhbDimName = R;
		/*
		 * First check that the new shell is inside the grid. If we're on a
		 * defined boundary, the angular coordinate is irrelevant.
		 */
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, 0);
		WhereAmI where = this.whereIsNhb(R);
		if ( where == UNDEFINED )
		{
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				Log.out(NHB_ITER_LEVEL, "  failure, R on undefined boundary");
			this._whereIsNhb = where;
			this._nhbDimName = R;
			return false;
		}
		if ( where == DEFINED || where == CYCLIC)
		{
			this._nhbDimName = R;
			this._nhbDirection = this._currentCoord[0] 
									< this._currentNeighbor[0] ? 1 : 0;
			this._whereIsNhb = where;
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				Log.out(NHB_ITER_LEVEL, "  success on "+ where +" boundary");
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
		/* 
		 * Increase the index if it has approx. the same location as the
		 * current coordinate
		 */
		double cur_max = rC.getCumulativeResolution(new_index);
		if ( ExtraMath.areEqual(cur_max, cur_min, POLAR_ANGLE_EQ_TOL) )
			new_index++;
		/* If we stepped onto the current coord, we went too far. */
		if ( (this._currentNeighbor[0] == this._currentCoord[0]) &&
				(new_index == this._currentCoord[1]) )
		{
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			{
				Log.out(NHB_ITER_LEVEL,
					"  failure, stepped onto current coordinate");
			}
			return false;
		}
		this._currentNeighbor[1] = new_index;
		/* 
		 * We are always in the same z-slice as the current coordinate when
		 * calling this method, so _nbhDimName can not be Z. 
		 */
		if ( this._currentCoord[0] == this._currentNeighbor[0] )
			this._nhbDimName = THETA;
		else
			this._nhbDimName = R;
		int dimIdx = getDimensionIndex(this._nhbDimName);
		this._nhbDirection = 
				this._currentCoord[dimIdx]
						< this._currentNeighbor[dimIdx] ? 1 : 0;
		this._whereIsNhb = WhereAmI.INSIDE;
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			Log.out(NHB_ITER_LEVEL, "  success with index "+new_index);
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
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "  trying to increase neighbor "
					+ Arrays.toString(this._currentNeighbor)+
					" by one polar in "+dim);
		}
		/* Avoid increasing on any boundaries */
		if ( (dim == THETA || this._nhbDimName == R) && 
				this._whereIsNhb != INSIDE)
		{
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			{
				Log.out(NHB_ITER_LEVEL, "  failure: already on "+
						this._nhbDimName+ " boundary, no point increasing");
			}
			return false;
		}
		Dimension dimension = this.getDimension(dim);
		int index = this.getDimensionIndex(dim);
		ResCalc rC = this.getResolutionCalculator(this._currentNeighbor, index);
		/* If we are already on the maximum boundary, we cannot go further. */
		if ( this._currentNeighbor[index] > rC.getNVoxel() - 1 )
		{
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			{
				Log.out(NHB_ITER_LEVEL,
						"  failure: already on maximum boundary");
			}
			return false;
		}
		/* Do not allow the neighbor to be on an undefined maximum boundary. */
		if ( this._currentNeighbor[index] == rC.getNVoxel() - 1 )
		{
			if ( dimension.isBoundaryDefined(1) )
			{
				if (isNoMoreOverlapping(index))
					return false;
				this._currentNeighbor[index]++;
				this._nhbDirection = 1;
				this._nhbDimName = dim;
				this._whereIsNhb = this.whereIsNhb(dim);
				if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				{
					Log.out(NHB_ITER_LEVEL, "  success on "+this._whereIsNhb 
							+ " boundary");
				}
				return true;
			}	
			else
			{
				if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				{
					Log.out(NHB_ITER_LEVEL, "  failure on "+this._whereIsNhb 
							+ " boundary");
				}
				return false;
			}
		}
		if (isNoMoreOverlapping(index))
			return false;
		/*
		 * All checks have passed, so increase and report success.
		 */
		this._currentNeighbor[index]++;
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "  success, new nbh coord is "
					+ this._currentNeighbor[index]);
		}
		return true;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param dimIndex
	 * @return
	 */
	private boolean isNoMoreOverlapping(int dimIndex)
	{
		ResCalc rC;
		double nbhMin, curMax;
		/*
		 * If increasing would mean we no longer overlap, report failure.
		 */
		rC = this.getResolutionCalculator(this._currentNeighbor, dimIndex);
		nbhMin = rC.getCumulativeResolution(this._currentNeighbor[dimIndex]);
		rC = this.getResolutionCalculator(this._currentCoord, dimIndex);
		curMax = rC.getCumulativeResolution(this._currentCoord[dimIndex]);
		if ( nbhMin >= curMax || 
				ExtraMath.areEqual(nbhMin, curMax, POLAR_ANGLE_EQ_TOL) )
		{
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			{
				Log.out(NHB_ITER_LEVEL, "  failure: nhb min greater or "
						+ "approximately equal to current max");
			}
			return true;
		}
		return false;
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
		/* see Docs/polarShapeScalingDerivation */
		/* scale resolution to have a voxel volume of one for resolution one */
		return res * (2.0 / ( 2 * shell + 1));
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
		/* see Docs/polarShapeScalingDerivation */
		/* scale resolution to have a voxel volume of one for resolution one */
		return res * 3.0 / ((1 + 3 * shell * (1 + shell)) 
			* (Math.cos(ring * ring_res) - Math.cos((1.0 + ring) * ring_res)));
	}
}

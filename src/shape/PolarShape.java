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
	 * Tolerance when comparing polar angles for equality (in radians).
	 */
	public final static double POLAR_ANGLE_EQ_TOL = 1e-6;
	
	/**
	 * \brief Used to move neighbor iterator into a new shell.
	 * 
	 * <p>Sets the R coordinate to <b>shellIndex</b>, the PHI - coordinate to  
	 * first valid index and the THETA coordinate to the current coordinate.</p>
	 * 
	 * @param shellIndex Index of the shell you want to move the neighbor
	 * iterator into.
	 * @return True is this was successful, false if it was not.
	 */
	protected boolean setNbhFirstInNewShell(int shellIndex)
	{
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "trying to set neighbor in new shell "+
				shellIndex);
		}
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[0] = shellIndex;
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
			return false;
		}
		if ( where == DEFINED || where == CYCLIC)
		{
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
//		double cur_max = rC.getCumulativeResolution(new_index);
//		if ( ExtraMath.areEqual(cur_max, cur_min, POLAR_ANGLE_EQ_TOL) )
//			new_index++;
		/* If we stepped onto the current coord, we went too far. */
//		if ( (this._currentNeighbor[0] == this._currentCoord[0]) &&
//				(new_index == this._currentCoord[1]) )
//		{
//			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
//			{
//				Log.out(NHB_ITER_LEVEL,
//					"  failure, stepped onto current coordinate");
//			}
//			return false;
//		}
		this._currentNeighbor[1] = new_index;

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
		Log.out(NHB_ITER_LEVEL, "  trying to increase neighbor "
			  + Arrays.toString(this._currentNeighbor)+" by one polar in "+dim);
		/* avoid increasing on any boundaries */
		int index = this.getDimensionIndex(dim);
		if ((dim == THETA || this._nbhDimName == R)  && this._whereIsNhb != INSIDE) {
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			{
				Log.out(NHB_ITER_LEVEL, "  failure: already on "+
						this._nbhDimName+ " boundary, no point increasing");
			}
			return false;
		}
		Dimension dimension = this.getDimension(dim);
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

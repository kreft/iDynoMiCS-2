package shape.iterator;

import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;
import static shape.iterator.ShapeIterator.WhereAmI.CYCLIC;
import static shape.iterator.ShapeIterator.WhereAmI.DEFINED;
import static shape.iterator.ShapeIterator.WhereAmI.INSIDE;
import static shape.iterator.ShapeIterator.WhereAmI.UNDEFINED;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.ResolutionCalculator;
import utility.ExtraMath;

public abstract class PolarShapeIterator extends ShapeIterator
{
	/**
	 * Tolerance when comparing polar angles for equality (in radians).
	 */
	public final static double POLAR_ANGLE_EQ_TOL = 1e-6;
	
	public PolarShapeIterator(Shape shape, int strideLength)
	{
		super(shape, strideLength);
	}
	
	public PolarShapeIterator(Shape shape)
	{
		super(shape);
	}
	
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
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[0] = shellIndex;
		/*
		 * First check that the new shell is inside the grid. If we're on a
		 * defined boundary, the angular coordinate is irrelevant.
		 */
		ResolutionCalculator rC = this._shape.getResolutionCalculator(this._currentCoord, 0);
		WhereAmI where = this.whereIsNhb(R);
		if ( where == UNDEFINED )
		{
			if ( Log.shouldWrite(Tier.DEBUG) )
				Log.out(Tier.DEBUG, "  failure, R on undefined boundary");
			this._whereIsNhb = where;
			return false;
		}
		if ( where == DEFINED || where == CYCLIC)
		{
			this._whereIsNhb = where;
			return true;
		}
		/*
		 * We're on an intermediate shell, so find the voxel which has the
		 * current coordinate's minimum angle inside it (which must exist!).
		 */
		rC = this._shape.getResolutionCalculator(this._currentCoord, 1);
		double cur_min = rC.getCumulativeResolution(this._currentCoord[1] - 1);
		rC = this._shape.getResolutionCalculator(this._currentNeighbor, 1);

		int new_index = rC.getVoxelIndex(cur_min);
		
		this._currentNeighbor[1] = new_index;

		this._whereIsNhb = WhereAmI.INSIDE;
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
		/* avoid increasing on any boundaries */
		int index = this._shape.getDimensionIndex(dim);
		if ((dim == THETA || this._nbhDimName == R)  && this._whereIsNhb != INSIDE) {
			if ( Log.shouldWrite(Tier.DEBUG) )
				Log.out(Tier.DEBUG, "  failure: already on "+
						this._nbhDimName+ " boundary, no point increasing");
			return false;
		}
		Dimension dimension = this._shape.getDimension(dim);
		ResolutionCalculator rC = this._shape.getResolutionCalculator(this._currentNeighbor, index);
		/* If we are already on the maximum boundary, we cannot go further. */
		if ( this._currentNeighbor[index] > rC.getNVoxel() - 1 )
		{
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
				return true;
			}	
			else
			{
				return false;
			}
		}
		if (isNoMoreOverlapping(index))
			return false;
		/*
		 * All checks have passed, so increase and report success.
		 */
		this._currentNeighbor[index]++;
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
		ResolutionCalculator rC;
		double nbhMin, curMax;
		/*
		 * If increasing would mean we no longer overlap, report failure.
		 */
		rC = this._shape.getResolutionCalculator(this._currentNeighbor, dimIndex);
		nbhMin = rC.getCumulativeResolution(this._currentNeighbor[dimIndex]);
		rC = this._shape.getResolutionCalculator(this._currentCoord, dimIndex);
		curMax = rC.getCumulativeResolution(this._currentCoord[dimIndex]);
		if ( nbhMin >= curMax || 
				ExtraMath.areEqual(nbhMin, curMax, POLAR_ANGLE_EQ_TOL) )
		{
			if ( Log.shouldWrite(Tier.DEBUG) )
			{
				Log.out(Tier.DEBUG, "  failure: nhb min greater or "
						+ "approximately equal to current max");
			}
			return true;
		}
		return false;
	}
}

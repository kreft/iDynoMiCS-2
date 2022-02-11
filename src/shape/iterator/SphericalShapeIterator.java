package shape.iterator;

import static shape.Dimension.DimName.PHI;
import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;
import static shape.iterator.ShapeIterator.NhbDirection.AHEAD;
import static shape.iterator.ShapeIterator.NhbDirection.BEHIND;
import static shape.iterator.ShapeIterator.WhereAmI.INSIDE;
import static shape.iterator.ShapeIterator.WhereAmI.UNDEFINED;

import shape.SphericalShape;
import shape.resolution.ResolutionCalculator;

public class SphericalShapeIterator extends PolarShapeIterator
{
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
	public SphericalShapeIterator(SphericalShape shape, int strideLength)
	{
		super(shape, strideLength);
	}
	
	public SphericalShapeIterator(SphericalShape shape)
	{
		super(shape);
	}
	
	/* ***********************************************************************
	 * NEIGHBOR ITERATOR
	 * **********************************************************************/
	
	@Override
	protected void resetNbhIter()
	{
		/* See if we can use the inside r-shell. */
		if ( this.setNbhFirstInNewShell( this._currentCoord[0] - 1 ) 
			&& this.setNbhFirstInNewRing( this._currentNeighbor[1] ) )
		{
			this._nbhDimName = R;
			this._nbhDirection = BEHIND;
		}
		/* 
		 * See if we can take one of the phi-minus-neighbors of the current 
		 * r-shell. 
		 */
		else if ( this.setNbhFirstInNewShell( this._currentCoord[0]) 
					&& this.setNbhFirstInNewRing( this._currentCoord[1] - 1) )
		{
			this._nbhDimName = PHI;
			this._nbhDirection = BEHIND;
		}
		/* 
		 * See if we can take one of the theta-neighbors in the current r-shell.
		 */
		else if ( this.moveNhbToMinus(THETA))
		{
			this._nbhDimName = THETA;
			this._nbhDirection = BEHIND;
		}
		else if (this.nhbJumpOverCurrent(THETA))
		{
			this._nbhDimName = THETA;
			this._nbhDirection = AHEAD;
		}
		/* See if we can take one of the phi-plus-neighbors. */
		else if ( this.setNbhFirstInNewRing( this._currentCoord[1] + 1) )
		{
			this._nbhDimName = PHI;
			this._nbhDirection = AHEAD;
		}
		
		/* See if we can use the outside r-shell. */
		else if ( this.setNbhFirstInNewShell( this._currentCoord[0] + 1 ) 
					&& this.setNbhFirstInNewRing( this._currentNeighbor[1] ) )
		{
			this._nbhDimName = R;
			this._nbhDirection = AHEAD;
		}
		/* There are no valid neighbors. */
		else
			this._whereIsNhb = UNDEFINED;
		if ( this.isNbhIteratorValid() )
		{
			transformNhbCyclic();
			return;
		}
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		this.untransformNhbCyclic();
		/*
		 * In the spherical shape, we start the TODO
		 */
		if ( this._currentNeighbor[0] == this._currentCoord[0] - 1 )
		{
			/*
			 * We're in the r-shell just inside that of the current coordinate.
			 */
			this._nbhDimName = R;
			this._nbhDirection = BEHIND;
			/* Try increasing theta by one voxel. */
			if ( ! this.increaseNbhByOnePolar(THETA) )
			{
				/* Try moving out to the next ring and set the first phi nhb */
				if ( ! this.increaseNbhByOnePolar(PHI) ||
										! this.setNbhFirstInNewRing(
												this._currentNeighbor[1]) )
				{
					/* 
					 * Try moving to the next shell. This sometimes misses the
					 * first valid phi coord (limited double accuracy), so lets
					 * additionally try the phi-minus ring.
					 */
					this._nbhDimName = PHI;
					this._nbhDirection = BEHIND;
					if ( ! this.moveNhbToMinus(PHI)
						|| ! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
					{
						/*
						 * If this fails, the phi-ring must be invalid, so try
						 * to move to the theta-minus neighbor in the current
						 * phi-ring.
						 * If this fails call this method again.
						 */
						this._nbhDimName = THETA;
						this._nbhDirection = BEHIND;
						if ( ! this.moveNhbToMinus(THETA) )
							return this.nbhIteratorNext();
					}
				}
			}
		}
		else if ( this._currentNeighbor[0] == this._currentCoord[0] )
		{
			/* 
			 * We're in the same r-shell as the current coordinate.
			 */
			if ( this._currentNeighbor[1] == this._currentCoord[1] - 1 )
			{
				/*
				 * We're in the phi-ring just inside that of the 
				 * current coordinate.
				 * Try increasing theta by one voxel. If this fails, move out
				 * to the next ring. If this fails, call this method again.
				 */
				this._nbhDimName = PHI;
				this._nbhDirection = BEHIND;
				if ( ! this.increaseNbhByOnePolar(THETA) ){
					this._nbhDimName = THETA;
					this._nbhDirection = BEHIND;
					if ( ! this.moveNhbToMinus(THETA) )
						return this.nbhIteratorNext();
				}
			}
			else if ( this._currentNeighbor[1] == this._currentCoord[1] )
			{
				/*
				 * We're in the same phi-ring as the current coordinate.
				 * Try to jump to the theta-plus side of the current
				 * coordinate. If you can't, try switching to the phi-plus
				 * ring.
				 */
				this._nbhDimName = THETA;
				this._nbhDirection = AHEAD;
				if ( ! this.nhbJumpOverCurrent(THETA) )
				{
					this._nbhDimName = PHI;
					this._nbhDirection = AHEAD;
					if ( ! this.setNbhFirstInNewRing(this._currentCoord[1]+1) )
						return this.nbhIteratorNext();
				}
			}
			else 
			{
				/* 
				 * We're in the phi-ring just above that of the current
				 * coordinate. 
				 */
				int rPlus = this._currentCoord[0] + 1;
				this._nbhDimName = PHI;
				this._nbhDirection = AHEAD;
				/* Try increasing theta by one voxel. */
				if ( ! this.increaseNbhByOnePolar(THETA) )
				{
					/* Move out to the next shell or the next rings. */
					this._nbhDimName = R;
					this._nbhDirection = AHEAD;
					if (! this.setNbhFirstInNewShell(rPlus) ||
						! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
					{
						this.nbhIteratorNext();
					}
				}
			}
		}
		else 
		{
			/* 
			 * We're in the r-shell just outside that of the current coordinate.
			 * If we can't increase phi and theta any more, then we've finished.
			 */
			this._nbhDimName = R;
			this._nbhDirection = AHEAD;
			if ( ! this.increaseNbhByOnePolar(THETA) )
				if (!this.increaseNbhByOnePolar(PHI) ||
						! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
				{
					this._whereIsNhb = UNDEFINED;
				}
		}
		this.transformNhbCyclic();
		return this._currentNeighbor;
	}
	
	/**
	 * this will set the current neighbor's phi coordinate to ringIndex and 
	 * attempt to set the theta coordinate.
	 * 
	 * @param shellIndex
	 * @return
	 */
	protected boolean setNbhFirstInNewRing(int ringIndex)
	{
		//TODO this will currently not set onto min boundary?
		this._currentNeighbor[1] = ringIndex;
		/*
		 * We must be on a shell inside the array or on a defined R boundary.
		 */
		WhereAmI whereIsR = this.whereIsNhb(R);
		if ( whereIsR != INSIDE )
		{
			if (whereIsR != UNDEFINED)
				return true;
			return false;
		}
		/*
		 * First check that the new ring is inside the grid. If we're on a
		 * defined boundary, the theta coordinate is irrelevant.
		 */
		if ( (this._whereIsNhb = this.whereIsNhb(PHI)) != INSIDE ){
			if (this._whereIsNhb != UNDEFINED)
				return true;
			return false;
		}

		ResolutionCalculator rC = this._shape.getResolutionCalculator(this._currentCoord, 2);
		/*
		 * We're on an intermediate ring, so find the voxel which has the
		 * current coordinate's minimum theta angle inside it.
		 */
		double theta = rC.getCumulativeResolution(this._currentCoord[2] - 1);
		rC = this._shape.getResolutionCalculator(this._currentNeighbor, 2);
		int new_index = rC.getElementIndex(theta);
		this._currentNeighbor[2] = new_index;
		
		return true;
	}
}

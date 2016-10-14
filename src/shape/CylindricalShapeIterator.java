package shape;

import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;
import static shape.Dimension.DimName.Z;
import static shape.ShapeIterator.WhereAmI.UNDEFINED;

public class CylindricalShapeIterator extends PolarShapeIterator{
	
	public CylindricalShapeIterator(CylindricalShape shape) {
		super(shape);
	}
	
	@Override
	protected void resetNbhIter()
	{
		/* See if we can use the inside r-shell. */
		if ( this.setNbhFirstInNewShell(this._currentCoord[0] - 1) )
		{ 
			this._nbhDimName = R;
			this._nbhDirection = 0;	
		}
		/* See if we can take the theta-minus-neighbor. */
		else if (this.moveNhbToMinus(THETA)) 
		{
			this._nbhDimName = THETA;
			this._nbhDirection = 0;	
		}
		/* See if we can take the theta-plus-neighbor. */
		else if(this.nhbJumpOverCurrent(THETA))
		{
			this._nbhDimName = THETA;
			this._nbhDirection = 1;	
		}
		/* See if we can take the z-minus-neighbor. */
		else if (this.moveNhbToMinus(Z))
		{
			this._nbhDimName = Z;
			this._nbhDirection = 0;	
		}
		/* See if we can take the z-plus-neighbor. */
		else if (this.nhbJumpOverCurrent(Z))
		{
			this._nbhDimName = Z;
			this._nbhDirection = 1;	
		}
		/* See if we can use the outside r-shell. */
		else if ( this.setNbhFirstInNewShell(this._currentCoord[0] + 1) )
		{
			this._nbhDimName = R;
			this._nbhDirection = 1;	
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
		 * In the cylindrical grid, we start the TODO
		 */
		if ( this._currentNeighbor[0] == this._currentCoord[0] - 1 )
		{
			/* 
			 * We're in the r-shell just inside that of the current coordinate.
			 * Try increasing theta by one voxel. If this fails, move out to
			 * the next shell. If this fails, call this method again.
			 */
			this._nbhDimName = R;
			this._nbhDirection = 0;
			if ( ! this.increaseNbhByOnePolar(THETA) )
			{
				this._nbhDimName = THETA;
				this._nbhDirection = 0;
				if ( ! this.moveNhbToMinus(THETA) )
					return this.nbhIteratorNext();
			}
					
		}
		else if ( this._currentNeighbor[0] == this._currentCoord[0] )
		{
			/* 
			 * We're in the same r-shell as the current coordinate.
			 */
			if ( this._currentNeighbor[2] == this._currentCoord[2] )
			{
				/*
				 * We're in the same z-slice as the current coordinate.
				 * Try to move to the theta-plus side of the current
				 * coordinate. If you can't, try switching to the z-minus
				 * voxel.
				 */
				this._nbhDimName = THETA;
				this._nbhDirection = 1;
				if ( ! this.nhbJumpOverCurrent(THETA) )
				{
					this._nbhDimName = Z;
					this._nbhDirection = 0;
					if ( ! this.moveNhbToMinus(Z) )
						return this.nbhIteratorNext();
				}
			}
			else if (this.nhbJumpOverCurrent(Z) )
			{
				this._nbhDimName = Z;
				this._nbhDirection = 1;
			}
			else{
				/*
				 * We tried to move to the z-plus side of the current
				 * coordinate, but since we failed we must be finished.
				 */
				this._nbhDimName = R;
				this._nbhDirection = 1;
				if ( ! this.setNbhFirstInNewShell(this._currentCoord[0] + 1) )
					this._whereIsNhb = UNDEFINED;
			}
		}
		else 
		{
			/* 
			 * We're in the r-shell just outside that of the current coordinate.
			 * If we can't increase theta any more, then we've finished.
			 */
			if ( ! this.increaseNbhByOnePolar(THETA) )
				this._whereIsNhb = UNDEFINED;
		}
		this.transformNhbCyclic();
		return this._currentNeighbor;
	}
}

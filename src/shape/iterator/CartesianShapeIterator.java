package shape.iterator;

import static shape.iterator.ShapeIterator.WhereAmI.UNDEFINED;

import shape.CartesianShape;
import shape.Dimension.DimName;

public class CartesianShapeIterator extends ShapeIterator
{
	public CartesianShapeIterator(CartesianShape shape, int strideLength)
	{
		super(shape, strideLength);
	}
	
	public CartesianShapeIterator(CartesianShape shape)
	{
		super(shape);
	}

	@Override
	protected void resetNbhIter()
	{
		this._whereIsNhb = UNDEFINED;
		for ( DimName dim : this._shape.getDimensionNames() )
		{
			/* Skip insignificant dimensions. */
			if ( ! this._shape.getDimension(dim).isSignificant() )
				continue;
			/* See if we can take one of the neighbors. */
			if ( this.moveNhbToMinus(dim) )
			{
				this._nbhDirection = NhbDirection.BEHIND;
				this._nbhDimName = dim;
				this.transformNhbCyclic();
				return;
			}
			else if ( this.nhbJumpOverCurrent(dim) )
			{
				this._nbhDirection = NhbDirection.AHEAD;
				this._nbhDimName = dim;
				this.transformNhbCyclic();
				return;
			}
		}
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		this.untransformNhbCyclic();
		int nhbIndex = this._shape.getDimensionIndex(this._nbhDimName);
		this._nbhDirection = NhbDirection.AHEAD;
		if ( ! this.nhbJumpOverCurrent(this._nbhDimName))
		{
			/*
			 * If we're in X or Y, try to move up one.
			 * If we're already in Z, then stop.
			 */
			nhbIndex++;
			if ( nhbIndex < 3 )
			{
				this._nbhDimName = this._shape.getDimensionName(nhbIndex);
				this._nbhDirection = NhbDirection.BEHIND;
				if ( ! moveNhbToMinus(this._nbhDimName) )
					return nbhIteratorNext();
			}
			else
			{
				this._whereIsNhb = UNDEFINED;
			}
		}
		
		this.transformNhbCyclic();
		return this._currentNeighbor;
	}
}

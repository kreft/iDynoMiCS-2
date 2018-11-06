package shape.iterator;

import static shape.iterator.ShapeIterator.WhereAmI.UNDEFINED;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
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
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, " Resetting nhb iter: current coord is "+
				Vector.toString(this._currentNeighbor));
		}
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
				if( Log.shouldWrite(NHB_ITER_LEVEL) )
					Log.out(NHB_ITER_LEVEL, "   returning transformed neighbor "
							+ "at "	+ Vector.toString(this._currentNeighbor) +
						": status "+this._whereIsNhb);
				return;
			}
			else if ( this.nhbJumpOverCurrent(dim) )
			{
				this._nbhDirection = NhbDirection.AHEAD;
				this._nbhDimName = dim;
				this.transformNhbCyclic();
				if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				{
					Log.out(NHB_ITER_LEVEL, "   returning transformed "+
						"neighbor at "+Vector.toString(this._currentNeighbor)+
						": status "+this._whereIsNhb);
				}
				return;
			}
		}
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, " Looking for next nhb of "+
				Vector.toString(this._currentCoord));
		}
		this.untransformNhbCyclic();
		int nhbIndex = this._shape.getDimensionIndex(this._nbhDimName);
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "   untransformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				", trying along "+this._nbhDimName);
		}
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
				if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				{
					Log.out(NHB_ITER_LEVEL, "   jumped into dimension "
						+this._nbhDimName);
				}
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

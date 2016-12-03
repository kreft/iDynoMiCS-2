package shape.iterator;

import linearAlgebra.Vector;
import shape.CartesianShape;

public class CartesianShapeRedBlackIterator extends CartesianShapeIterator
{
	/**
	 * Private switch for whether we are iterating over red squares (even
	 * index) or black squares (odd index).
	 */
	private boolean _isRed = true;
	
	public CartesianShapeRedBlackIterator(CartesianShape shape)
	{
		super(shape);
	}

	@Override
	public int[] resetIterator()
	{
		int[] coord = super.resetIterator();
		this._isRed = isRed(coord);
		return coord;
	}
	
	@Override
	public void loadSavedIteratorState()
	{
		super.loadSavedIteratorState();
		this._isRed = isRed(this._currentCoord);
	}
	
	@Override
	public int[] iteratorNext()
	{
		/* Move forward two steps instead of one. */
		super.iteratorNext();
		super.iteratorNext();
		/*
		 * If we have reached the end of the reds, reset and move forward one
		 * to start on the blacks.
		 */
		if ( this._isRed && ! this.isIteratorValid() )
		{
			this.resetIterator();
			this._isRed = false;
			this.iteratorNext();
		}
		
		return this._currentCoord;
	}
	
	private static boolean isRed(int[] coord)
	{
		return Vector.sum(coord) % 2 == 0;
	}
}

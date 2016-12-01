package shape;

import static dataIO.Log.Tier.BULK;
import static shape.ShapeIterator.WhereAmI.CYCLIC;
import static shape.ShapeIterator.WhereAmI.DEFINED;
import static shape.ShapeIterator.WhereAmI.INSIDE;
import static shape.ShapeIterator.WhereAmI.UNDEFINED;

import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import shape.Dimension.DimName;
import shape.resolution.ResolutionCalculator.ResCalc;

public abstract class ShapeIterator
{
	protected enum WhereAmI
	{
		/**
		 * Inside the array.
		 */
		INSIDE,
		/**
		 * On a cyclic voxel.
		 */
		CYCLIC,
		/**
		 * On a defined boundary.
		 */
		DEFINED,
		/**
		 * On an undefined boundary.
		 */
		UNDEFINED;
	}
	
	protected Shape _shape;
	
	/**
	 * An array to store the current iterator state.
	 */
	protected int[] storeIter;
	/**
	 * An array to store the current neighborhood iterator state.
	 */
	protected int[] storeNbh;
	/**
	 * Current coordinate considered by the internal iterator.
	 */
	protected int[] _currentCoord;
	/**
	 * The number of voxels, in each dimension, for the current coordinate of
	 * the internal iterator.
	 */
	protected int[] _currentNVoxel;
	/**
	 * Current neighbour coordinate considered by the neighbor iterator.
	 */
	protected int[] _currentNeighbor;
	/**
	 * The dimension name the current neighbor is moving in.
	 */
	protected DimName _nbhDimName;
	/**
	 * Integer indicating positive (1) or negative (0) relative position
	 * to the current coordinate.
	 */
	protected int _nbhDirection;
	/**
	 * What kind of voxel the current neighbor iterator is in.
	 */
	protected WhereAmI _whereIsNhb;
	
	/**
	 * \brief Log file verbosity level used for debugging the neighbor iterator.
	 * 
	 * <ul><li>Set to {@code BULK} for normal simulations</li>
	 * <li>Set to {@code DEBUG} when trying to debug an issue</li></ul>
	 */
	protected static final Tier NHB_ITER_LEVEL = BULK;
	
	public ShapeIterator(Shape shape) {
		this._shape = shape;
	}
	
	/**
	 * \brief Find out what kind of voxel is represented by the given
	 * coordinates, within the reference frame of the given dimension.
	 * 
	 * @param coord Integer vector representing the coordinates of a voxel.
	 * @param dimName Name of a dimension in this shape.
	 * @return What kind of voxel this is.
	 */
	protected WhereAmI whereIs(int[] coord, DimName dimName)
	{
		Dimension dim = this._shape.getDimension(dimName);
		if ( ! dim.isSignificant() )
			return UNDEFINED;
		int index = this._shape.getDimensionIndex(dimName);
		if ( coord[index] < 0 )
		{
			if ( dim.isCyclic() )
				return CYCLIC;
			return ( dim.isBoundaryDefined(0) ) ? DEFINED : UNDEFINED;
		}
		int nVox = this._shape.getResolutionCalculator(coord, index).getNVoxel();
		if ( coord[index] >= nVox )
		{
			if ( dim.isCyclic() )
				return CYCLIC;
			return ( dim.isBoundaryDefined(1) ) ? DEFINED : UNDEFINED;
		}
		return INSIDE;
	}
	
	/**
	 * \brief Find out what kind of voxel the neighbor iterator is at, within
	 * the reference frame of the given dimension.
	 * 
	 * @param dimName Name of a dimension in this shape.
	 * @return What kind of voxel the neighbor iterator is.
	 */
	protected WhereAmI whereIsNhb(DimName dimName)
	{
		return this.whereIs(this._currentNeighbor, dimName);
	}
	
	/**
	 * \brief Find out what kind of voxel the neighbor iterator is at.
	 * 
	 * @return What kind of voxel the neighbor iterator is.
	 */
	protected WhereAmI whereIsNhb()
	{
		this._whereIsNhb = INSIDE;
		WhereAmI where;
		for ( DimName dim : this._shape._dimensions.keySet() )
		{
			where = this.whereIsNhb(dim);
			if ( where == UNDEFINED )
				return (this._whereIsNhb = UNDEFINED);
			if ( this.isNbhIteratorInside() && where == DEFINED )
				this._whereIsNhb = DEFINED;
		}
		return this._whereIsNhb;
	}
	
	
	/* ***********************************************************************
	 * COORDINATE ITERATOR
	 * **********************************************************************/
	
	/**
	 * \brief Get the number of voxels in each dimension for the current
	 * coordinates.
	 * 
	 * <p>For Cartesian shapes the value of <b>coords</b> will be
	 * irrelevant, but it will make a difference in Polar shapes.</p>
	 * 
	 * @param coords Discrete coordinates of a voxel on this shape.
	 * @return A 3-vector of the number of voxels in each dimension.
	 */
	protected void updateCurrentNVoxel()
	{
		if ( this._currentNVoxel == null )
			this._currentNVoxel = Vector.zerosInt(3);
		if ( this._currentCoord == null )
			this.resetIterator();
		this._shape.nVoxelTo(this._currentNVoxel, this._currentCoord);
	}
	
	/**
	 * \brief Return the coordinate iterator to its initial state.
	 * 
	 * @return The value of the coordinate iterator.
	 */
	public int[] resetIterator()
	{
		if ( this._currentCoord == null )
		{
			this._currentCoord = Vector.zerosInt(this._shape._dimensions.size());
			this._currentNVoxel = Vector.zerosInt(this._shape._dimensions.size());
		}
		else
			Vector.reset(this._currentCoord);
		this.updateCurrentNVoxel();	
		return this._currentCoord;
	}
	
	public void saveCurrentIteratorState(){
		/*
		 * Store the two iterators, in case we're in the middle of an
		 * iteration and want to start a new iteration.
		 */
		if ( this._currentCoord != null )
			storeIter = Vector.copy(this._currentCoord);
		if ( this._currentNeighbor != null )
			storeNbh = Vector.copy(this._currentNeighbor);
	}
	
	public void loadSavedIteratorState(){
		/*
		 * Put the iterators back to their stored values.
		 */
		this._currentCoord = (storeIter == null) ? null:Vector.copy(storeIter);
		this._currentNeighbor = (storeNbh==null) ? null:Vector.copy(storeNbh);
	}

	/**
	 * \brief Current coordinates of the voxel iterator.
	 * 
	 * @return 3-vector of integers.
	 */
	public int[] iteratorCurrent()
	{
		return this._currentCoord;
	}

	/**
	 * \brief Step the coordinate iterator forward once.
	 * 
	 * @return The new value of the coordinate iterator.
	 */
	public int[] iteratorNext()
	{
		/*
		 * We have to step through last dimension first, because we use jagged 
		 * arrays in the PolarGrids.
		 */
		this._currentCoord[2]++;
		if ( this.iteratorExceeds(2) )
		{
			this._currentCoord[2] = 0;
			this._currentCoord[1]++;
			if ( this.iteratorExceeds(1) )
			{
				this._currentCoord[1] = 0;
				this._currentCoord[0]++;
			}
		}
		if ( this.isIteratorValid() )
			this.updateCurrentNVoxel();
		return this._currentCoord;
	}
	
	/**
	 * \brief Determine whether the current coordinate of the iterator is
	 * outside the grid in the dimension specified.
	 * 
	 * @param dim Index of the dimension to look at.
	 * @return Whether the coordinate iterator is inside (false) or outside
	 * (true) the grid along this dimension.
	 */
	protected boolean iteratorExceeds(int dim)
	{
		return this._currentCoord[dim] >= this._currentNVoxel[dim];
	}
	
	/**
	 * \brief Check if the current coordinate of the internal iterator is
	 * valid.
	 * 
	 * @return True if is valid, false if it is invalid.
	 */
	public boolean isIteratorValid()
	{
		int nDim = this._shape.getNumberOfDimensions();
		for ( int dim = 0; dim < nDim; dim++ )
			if ( this.iteratorExceeds(dim) )
				return false;
		return true;
	}	

	/**
	 * \brief Calculates the starting point for integration between the current 
	 * iterator voxel and the neighbor voxel, in the dimension given by 
	 * <b>index</b>.
	 * This is the maximum of the minima of both locations.
	 * 
	 * @param index Index of the required dimension.
	 * @return The starting point for integration.
	 */
	protected double getIntegrationMin(int index)
	{
		ResCalc rC;
		double curMin, nhbMin;
		/* Current voxel of the main iterator. */
		rC = this._shape.getResolutionCalculator(this._currentCoord, index);
		curMin = rC.getCumulativeResolution(this._currentCoord[index] - 1);
		
		/* on defined boundary */
		if (this._whereIsNhb == DEFINED || this._whereIsNhb == CYCLIC)
			return curMin;
		
		/* Current voxel of the neighbor iterator. */
		rC = this._shape.getResolutionCalculator(this._currentNeighbor, index);
		nhbMin = rC.getCumulativeResolution(this._currentNeighbor[index] - 1);
		/* Find integration minimum. */		
		return Math.max(curMin, nhbMin);
	}
	
	/**
	 * \brief Calculates the end point for integration between the current 
	 * iterator voxel and the neighbor voxel, in the dimension given by 
	 * <b>index</b>.
	 * This is the minimum of the maxima of both locations.
	 * 
	 * @param index Index of the required dimension.
	 * @return The end point for integration.
	 */
	protected double getIntegrationMax(int index)
	{
		ResCalc rC;
		double curMax, nhbMax;
		/* Current voxel of the main iterator. */
		rC = this._shape.getResolutionCalculator(this._currentCoord, index);
		curMax = rC.getCumulativeResolution(this._currentCoord[index]);
		
		/* on defined boundary */
		if (this._whereIsNhb == DEFINED || this._whereIsNhb == CYCLIC)
			return curMax;
		
		/* Current voxel of the neighbor iterator. */
		rC = this._shape.getResolutionCalculator(this._currentNeighbor, index);
		nhbMax = rC.getCumulativeResolution(this._currentNeighbor[index]);
		/* Find integration minimum. */		
		return Math.min(curMax, nhbMax);
	}
	
	/* ***********************************************************************
	 * NEIGHBOR ITERATOR
	 * **********************************************************************/
	
	/**
	 * \brief Reset the neighbor iterator.
	 * 
	 * <p>Typically used just after the coordinate iterator has moved.</p>
	 * 
	 * @return The current neighbor coordinate.
	 */
	public int[] resetNbhIterator()
	{
		/* Set the neighbor to the current coordinate. */
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.copy(this._currentCoord);
		else
			Vector.copyTo(this._currentNeighbor, this._currentCoord);
		/* Find the first neighbor by shape type. */
		this.resetNbhIter();
		/* Return the current neighbour coordinate. */
		return this._currentNeighbor;
	}
	
	/**
	 * \brief Current coordinates of the neighbor iterator.
	 * 
	 * @return 3-vector of integers.
	 */
	public int[] nbhIteratorCurrent()
	{
		return this._currentNeighbor;
	}
	
	/**
	 * \brief Check if the neighbor iterator takes a valid coordinate.
	 * 
	 * <p>Valid coordinates are either inside the array, or on a defined
	 * boundary.</p>
	 * 
	 * @return {@code boolean true} if it is valid, {@code false} if it is not.
	 */
	public boolean isNbhIteratorValid()
	{
		return this.isNbhIteratorInside() || (this._whereIsNhb == DEFINED);
	}
	
	/**
	 * \brief Check if the neighbor iterator takes a coordinate inside the
	 * array.
	 * 
	 * @return {@code boolean true} if it is inside, {@code false} if it is
	 * on a boundary (defined or undefined).
	 */
	public boolean isNbhIteratorInside()
	{
		return (this._whereIsNhb == INSIDE) || (this._whereIsNhb == CYCLIC);
	}
	
	/**
	 * \brief Check if the neighbor iterator is on a defined boundary.
	 * 
	 * @return The respective boundary or null if the nbh iterator is inside.
	 */
	public SpatialBoundary nbhIteratorOutside()
	{
		if ( this._whereIsNhb == DEFINED )
		{
			Dimension dim = this._shape.getDimension(this._nbhDimName);
			return dim.getBoundary(this._nbhDirection);
		}
		return null;
	}
	
	
	/**
	 * \brief Transform the coordinates of the neighbor iterator, in the
	 * current neighbor direction, so that that they lie within the array.
	 * 
	 * <p>This should be reversed using {@link #untransformNhbCyclic()}.</p>
	 */
	protected void transformNhbCyclic()
	{
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "   pre-transformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				": status "+this._whereIsNhb);
		}
		Dimension dim = this._shape.getDimension(this._nbhDimName);
		if ( (this._whereIsNhb == CYCLIC) && dim.isCyclic() )
		{
			int dimIdx = this._shape.getDimensionIndex(this._nbhDimName);
			int nVoxel = this._shape.getResolutionCalculator(
					this._currentNeighbor, dimIdx).getNVoxel();
			if ( this._nbhDirection == 0 )
			{
				/* Direction 0: the neighbor wraps below, to the highest. */
				this._currentNeighbor[dimIdx] = nVoxel - 1;
			}
			else
			{
				/* Direction 1: the neighbor wraps above, to zero. */
				this._currentNeighbor[dimIdx] = 0;
			}
		}
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "   returning transformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				": status "+this._whereIsNhb);
		}
	}
	
	/**
	 * \brief Reverses the transformation of {@link #transformNhbCyclic()},
	 * putting the coordinates of the neighbor iterator that wrap around a
	 * cyclic dimension back where they were.
	 */
	protected void untransformNhbCyclic()
	{
		Dimension dim = this._shape.getDimension(this._nbhDimName);
		if ( (this._whereIsNhb == CYCLIC) && dim.isCyclic() )
		{
			int dimIdx = this._shape.getDimensionIndex(this._nbhDimName);
			if ( this._nbhDirection == 0 )
			{
				/* Direction 0: the neighbor should reset to minus one. */
				this._currentNeighbor[dimIdx] = -1;
			}
			else
			{
				int nVoxel = this._shape.getResolutionCalculator(
						this._currentNeighbor, dimIdx).getNVoxel();
				/* Direction 1: the neighbor should reset above the highest.*/
				this._currentNeighbor[dimIdx] = nVoxel;
			}
		}
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "   un-transformed neighbor at "+
					Vector.toString(this._currentNeighbor)+
					": status "+this._whereIsNhb);
		}
	}
	
	/**
	 * \brief Move the neighbor iterator to the current coordinate, 
	 * and make the index at <b>dim</b> one less.
	 * If successful, sets <b>_nbhDirection</b> to 1 (lower than current coord)
	 * If Outside, but on defined boundary, sets <b>_nbhOnBoundary</b> to true.
	 * 
	 * @return {@code boolean} reporting whether this is valid.
	 */
	protected boolean moveNhbToMinus(DimName dim)
	{
		int index = this._shape.getDimensionIndex(dim);
		/* Move to the coordinate just belong the current one. */
		Vector.copyTo(this._currentNeighbor, this._currentCoord);
		this._currentNeighbor[index]--;
		/* Check that this coordinate is acceptable. */
		WhereAmI where = this.whereIsNhb(dim);
		this._whereIsNhb = where;
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL,
				"   tried moving to minus in "+dim+": result "+
						Vector.toString(this._currentNeighbor)+" is "+where);
		}
		return (where != UNDEFINED);
	}
	
	/**
	 * \brief Try to increase the neighbor iterator from the minus-side of the
	 * current coordinate to the plus-side.
	 * 
	 * <p>For use on linear dimensions (X, Y, Z, R) and not on angular ones
	 * (THETA, PHI).</p>
	 * 
	 * @param dim Index of the dimension to move in.
	 * @return Whether the increase was successful (true) or a failure (false).
	 */
	protected boolean nhbJumpOverCurrent(DimName dim)
	{
		int index = this._shape.getDimensionIndex(dim);
		/* Check we are behind the current coordinate. */
		if ( this._currentNeighbor[index] < this._currentCoord[index] )
		{
			/* Try to jump. */
			this._currentNeighbor[index] = this._currentCoord[index] + 1;
			boolean bMaxDef = this._shape.getDimension(dim).isBoundaryDefined(1);
			this._whereIsNhb = this.whereIsNhb(dim);
			/* Check there is space on the other side. */
			if ( this._whereIsNhb == INSIDE || bMaxDef )
			{
				/* report success. */
				if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				{
					Log.out(NHB_ITER_LEVEL, "   success jumping over in "+dim+
							": result "+Vector.toString(this._currentNeighbor)+
							" is "+this._whereIsNhb);
				}
				return true;
			}
		}
		/* Undo jump and report failure. */
		this._currentNeighbor[index] = this._currentCoord[index] - 1;
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "   failure jumping over in "+dim+
				": result "+Vector.toString(this._currentNeighbor)+
				" is "+this._whereIsNhb);
		}
		return false;
	}
	
	/**
	 * \brief Helper method for resetting the neighbor iterator, to be
	 * implemented by subclasses.
	 */
	protected abstract void resetNbhIter();
	
	/**
	 * @return The next neighbor iterator coordinate.
	 */
	public abstract int[] nbhIteratorNext();
}

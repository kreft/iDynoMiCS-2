package shape;

import static shape.Shape.WhereAmI.*;

import dataIO.Log;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import shape.ShapeConventions.SingleVoxel;
import shape.resolution.ResolutionCalculator.ResCalc;

/**
 * \brief Abstract subclass of {@code Shape} that handles the important methods
 * for {@code Line}, {@code Rectangle},and {@code Cuboid}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public abstract class CartesianShape extends Shape
{
	/**
	 * Array of resolution calculators used by all linear {@code Shape}s.
	 */
	protected ResCalc[] _resCalc = new ResCalc[3];
	
	
	/*************************************************************************
	 * CONSTRUCTION
	 ************************************************************************/
	
	public CartesianShape()
	{
		/*
		 * Fill the resolution calculators with dummies for now: they should
		 * be overwritten later.
		 */
		for ( int i = 0; i < 3; i++ )
		{
			SingleVoxel sV = new SingleVoxel();
			sV.init(1.0, 1.0);
			this._resCalc[i] = sV;
		}
		/*
		 * These are the dimension names for any Cartesian shape. Assume they
		 * are all insignificant to begin with.
		 */
		for ( DimName d : new DimName[]{DimName.X, DimName.Y, DimName.Z} )
			this._dimensions.add(new Dimension(false, d));
		
	}
	
	@Override
	public double[][][] getNewArray(double initialValue)
	{
		return Array.array(this.updateCurrentNVoxel(), initialValue);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	@Override
	public double[] getLocalPosition(double[] location)
	{
		return location;
	}
	
	@Override
	public double[] getGlobalLocation(double[] local)
	{
		return local;
	}
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/
	
	@Override
	public void setDimensionResolution(DimName dName, ResCalc resC)
	{
		int index = this.getDimensionIndex(dName);
		this._resCalc[index] = resC;
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		/* Coordinate is irrelevant here. */
		return this._resCalc[axis];
	}
	
	/*************************************************************************
	 * LOCATIONS
	 ************************************************************************/
	
	/*************************************************************************
	 * SURFACES
	 ************************************************************************/
	
	@Override
	public void setSurfaces()
	{
		for ( Dimension dim : this._dimensions )
			if ( dim.isSignificant() )
				this.setPlanarSurfaces(dim._dimName);
	}
	
	/*************************************************************************
	 * BOUNDARIES
	 ************************************************************************/
	
	/*************************************************************************
	 * VOXELS
	 ************************************************************************/
	
	@Override
	public double getVoxelVolume(int[] coord)
	{
		double out = 1.0;
		ResCalc rC;
		for ( int dim = 0; dim < getNumberOfDimensions(); dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			out *= rC.getResolution(coord[dim]);
		}
		return out;
	}
	
	/*************************************************************************
	 * SUBVOXEL POINTS
	 ************************************************************************/
	
	/*************************************************************************
	 * COORDINATE ITERATOR
	 ************************************************************************/
	
	/*************************************************************************
	 * NEIGHBOR ITERATOR
	 ************************************************************************/
	
	@Override
	protected void resetNbhIter()
	{
		Log.out(NHB_ITER_LEVEL, " Resetting nhb iter: current coord is "+
				Vector.toString(this._currentNeighbor));
		this._whereIsNbh = UNDEFINED;
		for ( Dimension dim : this._dimensions )
		{
			/* Skip insignificant dimensions. */
			if ( ! dim.isSignificant() )
				continue;
			/* See if we can take one of the neighbors. */
			if ( this.moveNbhToMinus(dim._dimName) || this.nbhJumpOverCurrent(dim._dimName) )
			{
				this._nbhDimName = dim._dimName;
				this.transformNbhCyclic();
				Log.out(NHB_ITER_LEVEL, "   returning transformed neighbor at "
						+Vector.toString(this._currentNeighbor)+
						": status "+this._whereIsNbh);
				return;
			}
		}
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		Log.out(NHB_ITER_LEVEL, " Looking for next nhb of "+
				Vector.toString(this._currentCoord));
		this.untransformNbhCyclic();
		int nbhIndex = this.getDimensionIndex(this._nbhDimName);
		Log.out(NHB_ITER_LEVEL, "   untransformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				", trying along "+this._nbhDimName);
		if ( ! this.nbhJumpOverCurrent(this._nbhDimName))
		{
			/*
			 * If we're in X or Y, try to move up one.
			 * If we're already in Z, then stop.
			 */
			nbhIndex++;
			if ( nbhIndex < 3 )
			{
				this._nbhDimName = this.getDimensionName(nbhIndex);
				if ( ! moveNbhToMinus(this._nbhDimName) )
					return nbhIteratorNext();
			}
		}
		Log.out(NHB_ITER_LEVEL, "   pre-transformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				": status "+this._whereIsNbh);
		this.transformNbhCyclic();
		Log.out(NHB_ITER_LEVEL, "   returning transformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				": status "+this._whereIsNbh);
		return this._currentNeighbor;
	}
	
	@Override
	public double nbhCurrDistance()
	{
		int i = this.getDimensionIndex(this._nbhDimName);
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, i);
		double out = rC.getResolution(this._currentCoord[i]);
		if ( this.isNhbIteratorInside() )
		{
			/* If the neighbor is inside the array, use the mean resolution. */
			out += rC.getResolution(this._currentNeighbor[i]);
			return 0.5 * out;
		}
		if ( this.isNbhIteratorValid() )
		{
			/* If the neighbor is on a defined boundary, use the current 
				coord's resolution. */
			return out;
		}
		/* If the neighbor is on an undefined boundary, return infinite
			distance (this should never happen!) */
		return Double.POSITIVE_INFINITY;
	}
	
	@Override
	public double nbhCurrSharedArea()
	{
		double area = 1.0;
		int nDim = this.getNumberOfDimensions();
		ResCalc rC;
		int index;
		for ( Dimension dim : this._dimensions )
		{
			if ( dim.equals(this._nbhDimName) 
					|| !dim.isSignificant() )
				continue;
			index = this.getDimensionIndex(dim._dimName);
			rC = this.getResolutionCalculator(this._currentCoord, index);
			/* Need to be careful about insignificant axes. */
			area *= ( index >= nDim ) ? rC.getResolution(0) :
								rC.getResolution(this._currentCoord[index]);
		}
		return area;
	}
}

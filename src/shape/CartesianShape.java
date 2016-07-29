package shape;

import static shape.Shape.WhereAmI.*;

import dataIO.Log;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import static shape.Dimension.DimName;
import static shape.Dimension.DimName.*;
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
		//TODO Stefan: Why do we need this?
		// 			   Shouldn't ResCalc be null if no dimension is specified?
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
		for ( DimName d : new DimName[]{X, Y, Z} )
			this._dimensions.put(d, new Dimension(false, d));
		
	}
	
	@Override
	public double[][][] getNewArray(double initialValue) {
		int[] nVoxel = this.updateCurrentNVoxel();
		/* we need at least length 1 in each dimension for the array */
		return Array.array(nVoxel[0] == 0 ? 1 : nVoxel[0], 
							nVoxel[1] == 0 ? 1 : nVoxel[1], 
							nVoxel[2] == 0 ? 1 : nVoxel[2], initialValue);
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
		for ( DimName dim : this._dimensions.keySet() )
			if ( this._dimensions.get(dim).isSignificant() )
				this.setPlanarSurfaces(dim);
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
		this._whereIsNhb = UNDEFINED;
		for ( DimName dim : this._dimensions.keySet() )
		{
			/* Skip insignificant dimensions. */
			if ( ! this.getDimension(dim).isSignificant() )
				continue;
			/* See if we can take one of the neighbors. */
			if ( this.moveNhbToMinus(dim) )
			{
				this._nbhDirection = 0;
				this._nbhDimName = dim;
				this.transformNhbCyclic();
				Log.out(NHB_ITER_LEVEL, "   returning transformed neighbor at "
						+Vector.toString(this._currentNeighbor)+
						": status "+this._whereIsNhb);
				return;
			}
			else if ( this.nhbJumpOverCurrent(dim) )
			{
				this._nbhDirection = 1;
				this._nbhDimName = dim;
				this.transformNhbCyclic();
				Log.out(NHB_ITER_LEVEL, "   returning transformed neighbor at "
						+Vector.toString(this._currentNeighbor)+
						": status "+this._whereIsNhb);
				return;
			}
		}
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		Log.out(NHB_ITER_LEVEL, " Looking for next nhb of "+
				Vector.toString(this._currentCoord));
		this.untransformNhbCyclic();
		int nhbIndex = this.getDimensionIndex(this._nbhDimName);
		Log.out(NHB_ITER_LEVEL, "   untransformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				", trying along "+this._nbhDimName);
		this._nbhDirection = 1;
		if ( ! this.nhbJumpOverCurrent(this._nbhDimName))
		{
			/*
			 * If we're in X or Y, try to move up one.
			 * If we're already in Z, then stop.
			 */
			nhbIndex++;
			if ( nhbIndex < 3 )
			{
				this._nbhDimName = this.getDimensionName(nhbIndex);
				this._nbhDirection = 0;
				Log.out(NHB_ITER_LEVEL, "   jumped into dimension "
						+this._nbhDimName);
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
	
	@Override
	public double nhbCurrSharedArea()
	{
		double area = 1.0;
		ResCalc rC;
		int index;
		for ( DimName dim : this.getDimensionNames() )
		{
			if ( dim.equals(this._nbhDimName) 
					|| ! this.getDimension(dim).isSignificant() )
				continue;
			index = this.getDimensionIndex(dim);
			rC = this.getResolutionCalculator(this._currentCoord, index);
			area *= rC.getResolution(this._currentCoord[index]);
		}
		return area;
	}
}

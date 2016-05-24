package shape;

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
			this._dimensions.put(d, new Dimension(false));
		
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
		if ( this._currentNeighbor == null )
			this._currentNeighbor = Vector.zerosInt(3);
		this._nbhValid = true;
		for ( DimName dim : this._dimensions.keySet() )
		{
			/* Skip insignificant dimensions. */
			if ( ! this.getDimension(dim).isSignificant() )
				continue;
			/* See if we can take one of the neighbors. */
			if ( this.moveNbhToMinus(dim) || this.nbhJumpOverCurrent(dim) )
			{
				this._nbhDimName = dim;
				this.transformNbhCyclic();
				return;
			}
		}
		this._nbhValid = false;
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		this.untransformNbhCyclic();
		int nbhIndex = this.getDimensionIndex(this._nbhDimName);
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
			else
				this._nbhValid = false;
		}
		this.transformNbhCyclic();
		this._whereIsNbh = this.whereIsNhb();
		return this._currentNeighbor;
	}
	
	@Override
	public double nbhCurrDistance()
	{
		int i = this.getDimensionIndex(this._nbhDimName);
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, i);
		// FIXME using the resolution is the neighbor voxel is out of bounds is
		// only a quick fix
		if ( this._currentNeighbor[i] >= 0 )
		{
			double out = rC.getPosition(this._currentCoord[i], 0.5);
			out -= rC.getPosition(this._currentNeighbor[i], 0.5);
			return Math.abs(out);
		}
		else
		{
			return rC.getResolution(this._currentCoord[i]);
		}
	}
	
	@Override
	public double nbhCurrSharedArea()
	{
		double area = 1.0;
		int nDim = this.getNumberOfDimensions();
		ResCalc rC;
		int index;
		for ( DimName dim : this.getDimensionNames() )
		{
			if ( dim.equals(this._nbhDimName) 
					|| !this.getDimension(dim).isSignificant() )
				continue;
			index = this.getDimensionIndex(dim);
			rC = this.getResolutionCalculator(this._currentCoord, index);
			/* Need to be careful about insignificant axes. */
			area *= ( index >= nDim ) ? rC.getResolution(0) :
								rC.getResolution(this._currentCoord[index]);
		}
		return area;
	}
}

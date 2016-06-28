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
	
	
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
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
	public double[][][] getNewArray(double initialValue)
	{
		this.updateCurrentNVoxel();
		/* We need at least length 1 in each dimension for the array. */
		int nI = (this._currentNVoxel[0] == 0) ? 1 : this._currentNVoxel[0];
		int nJ = (this._currentNVoxel[1] == 0) ? 1 : this._currentNVoxel[1];
		int nK = (this._currentNVoxel[2] == 0) ? 1 : this._currentNVoxel[2];
		return Array.array(nI, nJ, nK, initialValue);
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	@Override
	public double getTotalVolume()
	{
		double out = 1.0;
		for ( Dimension dim : this._dimensions.values() )
			out *= dim.getLength();
		return out;
	}
	
	@Override
	public void getLocalPositionTo(double[] destination, double[] location)
	{
		Vector.copyTo(destination, location);
	}
	
	@Override
	protected void getGlobalLocationTo(double[] destination, double[] local)
	{
		Vector.copyTo(destination, local);
	}
	
	/* ***********************************************************************
	 * DIMENSIONS
	 * **********************************************************************/
	
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
	
	/* ***********************************************************************
	 * LOCATIONS
	 * **********************************************************************/
	
	/* ***********************************************************************
	 * SURFACES
	 * **********************************************************************/
	
	@Override
	public void setSurfaces()
	{
		for ( DimName dim : this._dimensions.keySet() )
			if ( this._dimensions.get(dim).isSignificant() )
				this.setPlanarSurfaces(dim);
	}
	
	/* ***********************************************************************
	 * BOUNDARIES
	 * **********************************************************************/
	
	@Override
	public double getBoundarySurfaceArea(DimName dimN, int extreme)
	{
		double area = 1.0;
		for ( DimName iDimN : this._dimensions.keySet() )
		{
			if ( iDimN.equals(dimN) )
				continue;
			area *= this.getDimension(iDimN).getLength();
		}
		return area;
	}
	
	/* ***********************************************************************
	 * VOXELS
	 * **********************************************************************/
	
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
	
	/* ***********************************************************************
	 * SUBVOXEL POINTS
	 * **********************************************************************/
	
	/* ***********************************************************************
	 * COORDINATE ITERATOR
	 * **********************************************************************/
	
	/* ***********************************************************************
	 * NEIGHBOR ITERATOR
	 * **********************************************************************/
	
	@Override
	protected void resetNbhIter()
	{
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, " Resetting nhb iter: current coord is "+
				Vector.toString(this._currentNeighbor));
		}
		this._whereIsNhb = UNDEFINED;
		for ( DimName dim : this._dimensions.keySet() )
		{
			/* Skip insignificant dimensions. */
			if ( ! this.getDimension(dim).isSignificant() )
				continue;
			/* See if we can take one of the neighbors. */
			if ( this.moveNbhToMinus(dim) || this.nbhJumpOverCurrent(dim) )
			{
				this._nhbDimName = dim;
				this.transformNbhCyclic();
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
		this.untransformNbhCyclic();
		int nbhIndex = this.getDimensionIndex(this._nhbDimName);
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "   untransformed neighbor at "+
				Vector.toString(this._currentNeighbor)+
				", trying along "+this._nhbDimName);
		}
		if ( ! this.nbhJumpOverCurrent(this._nhbDimName))
		{
			/*
			 * If we're in X or Y, try to move up one.
			 * If we're already in Z, then stop.
			 */
			nbhIndex++;
			if ( nbhIndex < 3 )
			{
				this._nhbDimName = this.getDimensionName(nbhIndex);
				if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				{
					Log.out(NHB_ITER_LEVEL, "   jumped into dimension "
						+this._nhbDimName);
				}
				if ( ! moveNbhToMinus(this._nhbDimName) )
					return nbhIteratorNext();
			}
			else
			{
				this._whereIsNhb = UNDEFINED;
			}
		}
		
		this.transformNbhCyclic();
		return this._currentNeighbor;
	}
	
	@Override
	public double nbhCurrSharedArea()
	{
		double area = 1.0;
		ResCalc rC;
		int index;
		for ( DimName dim : this.getDimensionNames() )
		{
			// FIXME here we implicitly assume that insignificant dimensions
			// have dummy length of one
			if ( dim.equals(this._nhbDimName)
					|| ! this.getDimension(dim).isSignificant() )
				continue;
			index = this.getDimensionIndex(dim);
			rC = this.getResolutionCalculator(this._currentCoord, index);
			area *= rC.getResolution(this._currentCoord[index]);
		}
		return area;
	}
}

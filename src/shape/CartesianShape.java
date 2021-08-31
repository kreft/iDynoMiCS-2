package shape;

import static shape.Dimension.DimName.X;
import static shape.Dimension.DimName.Y;
import static shape.Dimension.DimName.Z;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import shape.Dimension.DimName;
import shape.ShapeConventions.SingleVoxel;
import shape.iterator.CartesianShapeIterator;
import shape.iterator.ShapeIterator;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;

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
	protected ResolutionCalculator[] _resCalc = new ResolutionCalculator[3];
	
	
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
	public CartesianShape()
	{
		int i = 0;
		for ( DimName d : new DimName[]{X, Y, Z} )
		{
			/*
			 * These are the dimension names for any Cartesian shape. Assume
			 * they are all insignificant to begin with.
			 */
			Dimension dimension = new Dimension(false, d);
			this._dimensions.put(d, dimension);
			/*
			 * Fill the resolution calculators with dummies for now: they
			 * should be overwritten later.
			 */
			this._resCalc[i] = new SingleVoxel(dimension);
			i++;
		}
		/*
		 * By default assume that we should use an iterator with step length 1.
		 */
		this.setNewIterator(1);
	}
	
	@Override
	public double[][][] getNewArray(double initialValue)
	{

		int[] nElement = new int[3];
		/* We need at least length 1 in each dimension for the array. */
		for ( int dim = 0; dim < 3; dim ++ )
			nElement[dim] = Math.max(this._resCalc[dim].getNElement(), 1);
		return Array.array(nElement, initialValue);
	}
	
	@Override
	public ShapeIterator getNewIterator(int strideLength)
	{
		return new CartesianShapeIterator(this, strideLength);
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
	public double getTotalRealVolume()
	{
		double out = 1.0;
		for ( Dimension dim : this._dimensions.values() )
			out *= dim.getRealLength();
		return out;
	}
	
	public void setTotalVolume( double volume)
	{
		Log.out(Tier.CRITICAL, "Cannot adjust Cartesian shape volume" );
	}
	
	@Override
	public void getLocalPositionTo(double[] destination, double[] location)
	{
		Vector.copyTo(destination, location);
	}
	
	@Override
	public void getGlobalLocationTo(double[] destination, double[] local)
	{
		Vector.copyTo(destination, local);
	}
	
	/* ***********************************************************************
	 * DIMENSIONS
	 * **********************************************************************/
	
	@Override
	public void setDimensionResolution(DimName dName, ResolutionCalculator resC)
	{
		int index = this.getDimensionIndex(dName);
		this._resCalc[index] = resC;
	}
	
	@Override
	public ResolutionCalculator getResolutionCalculator(int[] coord, int axis)
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
				this.setPlanarSurface(dim);
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
	
	@Override
	public double getRealSurfaceArea(DimName dimN, int extreme)
	{
		double area = 1.0;
		for ( DimName iDimN : this._dimensions.keySet() )
		{
			if ( iDimN.equals(dimN) )
				continue;
			area *= this.getDimension(iDimN).getRealLength();
		}
		return area;
	}
	
	/* ***********************************************************************
	 * VOXELS
	 * **********************************************************************/
	
	@Override
	public int getTotalNumberOfVoxels()
	{
		int n = 1;
		for ( int i = 0; i < 3; i++ )
			n *= this._resCalc[i].getNVoxel();
		return n;
	}
	
	@Override
	public double getVoxelVolume(double[] origin, double[] upper)
	{
		double out = 1.0;
		for ( int dim = 0; dim < getNumberOfDimensions(); dim++ )
			out *= upper[dim] - origin[dim];
		return out;
	}
	
	@Override
	public double nhbCurrSharedArea()
	{
		double area = 1.0;
		ResolutionCalculator rC;
		int index;
		int[] currentCoord = this._it.iteratorCurrent();
		for ( DimName dim : this.getDimensionNames() )
		{
			// FIXME here we implicitly assume that insignificant dimensions
			// have dummy length of one
			if ( dim.equals(this._it.currentNhbDimName())
					|| ! this.getDimension(dim).isSignificant() )
				continue;
			index = this.getDimensionIndex(dim);
			rC = this.getResolutionCalculator(currentCoord, index);
			area *= rC.getResolution();
		}
		return area;
	}
	
	/* ***********************************************************************
	 * MULTIGRID CONSTRUCTION
	 * **********************************************************************/
	
	@Override
	public boolean canGenerateCoarserMultigridLayer()
	{
		/*
		 * Acceptable resolution calculators are MultigridResolution and
		 * SingleVoxel. There must be at least one MultigridResolution with
		 * more than 2 voxels to be worth generating a multigrid.
		 */
		int multigridCount = 0;
		for (ResolutionCalculator resCalc : this._resCalc)
		{
			if ( resCalc instanceof MultigridResolution)
			{
				if ( resCalc.getNVoxel() > 2 )
					multigridCount++;
				continue;
			}
			if ( resCalc instanceof SingleVoxel )
				continue;
			return false;
		}
		return multigridCount > 0;
	}
	
	@Override
	public Shape generateCoarserMultigridLayer()
	{
		Shape out;
		try
		{
			out = this.getClass().newInstance();
		}
		catch (Exception e)
		{
			return null;
		}
		
		ResolutionCalculator newResCalc;
		DimName dimName;
		Dimension dimension;
		for ( int i = 0; i < 3; i++ )
		{
			dimName = this.getDimensionName(i);
			dimension = this.getDimension(dimName);
			if ( this._resCalc[i] instanceof SingleVoxel )
				out.setDimensionResolution(dimName, this._resCalc[i]);
			else if ( this._resCalc[i] instanceof MultigridResolution )
			{
				newResCalc = ((MultigridResolution)this._resCalc[i])
						.getCoarserResolution();
				out.setDimensionResolution(dimName, newResCalc);
				if ( dimension.isCyclic() )
					out.makeCyclic(dimName);
			}
			else
				return null;
			
			if ( ! dimension.isCyclic() )
			{
				out.setBoundary(dimName, 0, dimension.getBoundary(0));
				out.setBoundary(dimName, 1, dimension.getBoundary(1));
			}
		}
		
		return out;
	}
}

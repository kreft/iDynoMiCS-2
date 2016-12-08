package shape;

import static shape.Dimension.DimName.X;
import static shape.Dimension.DimName.Y;
import static shape.Dimension.DimName.Z;

import linearAlgebra.Array;
import linearAlgebra.Vector;
import shape.Dimension.DimName;
import shape.ShapeConventions.SingleVoxel;
import shape.iterator.CartesianShapeIterator;
import shape.iterator.ShapeIterator;
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
		for ( int i = 0; i < 3; i++ )
		{
			SingleVoxel sV = new SingleVoxel();
			sV.init(1.0, 0.0, 1.0);
			this._resCalc[i] = sV;
		}
		/*
		 * These are the dimension names for any Cartesian shape. Assume they
		 * are all insignificant to begin with.
		 */
		for ( DimName d : new DimName[]{X, Y, Z} )
			this._dimensions.put(d, new Dimension(false, d));
		/*
		 * By default assume that we should use an iterator with step length 1.
		 */
		this.setNewIterator(1);
	}
	
	@Override
	public double[][][] getNewArray(double initialValue)
	{
		int[] nVoxel = new int[3];
		/* We need at least length 1 in each dimension for the array. */
		for ( int dim = 0; dim < 3; dim ++ )
			nVoxel[dim] = Math.max(this._resCalc[dim].getNVoxel(), 1);
		return Array.array(nVoxel, initialValue);
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
	public void setDimensionResolution(DimName dName, ResCalc resC)
	{
		int index = this.getDimensionIndex(dName);
		this._resCalc[index] = resC;
	}
	
	@Override
	public ResCalc getResolutionCalculator(int[] coord, int axis)
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
	
	/* ***********************************************************************
	 * VOXELS
	 * **********************************************************************/
	
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
		ResCalc rC;
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
			area *= rC.getResolution(currentCoord[index]);
		}
		return area;
	}
}

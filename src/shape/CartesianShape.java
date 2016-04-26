package shape;

import grid.CartesianGrid;
import grid.SpatialGrid.GridGetter;
import shape.ShapeConventions.DimName;
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
	protected ResCalc[] _resCalc;
	
	protected DimName _nbhDirection;
	
	public CartesianShape()
	{
		this._resCalc = new ResCalc[3];
		for ( DimName d : new DimName[]{DimName.X, DimName.Y, DimName.Z} )
			this._dimensions.put(d, new Dimension(false));
	}
	
	@Override
	public GridGetter gridGetter()
	{
		// TODO Make 1D, 2D, and 3D getters?
		return CartesianGrid.standardGetter();
	}
	
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
	
	@Override
	public void setSurfaces()
	{
		for ( DimName dim : this._dimensions.keySet() )
			if ( this._dimensions.get(dim).isSignificant() )
				this.setPlanarSurfaces(dim);
	}
	
	@Override
	protected void resetNbhIter()
	{
		this._nbhValid = true;
		for ( DimName dim : this._dimensions.keySet() )
		{
			/* Skip insignificant dimensions. */
			if ( ! this.getDimension(dim).isSignificant() )
				continue;
			/* See if we can take one of the neighbors. */
			if ( this.moveNbhToMinus(dim) || this.nbhJumpOverCurrent(dim) )
			{
				this._nbhDirection = dim;
				return;
			}
		}
		this._nbhValid = false;
	}
	
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
	
	protected void getNVoxel(int[] coords, int[] outNVoxel)
	{
		for ( int dim = 0; dim < this.getNumberOfDimensions(); dim++ )
			outNVoxel[dim] = this._resCalc[dim].getNVoxel();
	}
	
	@Override
	public double getVoxelVolume(int[] coord)
	{
		double out = 1.0;
		ResCalc rC;
		// TODO handle y, z dimensions
		for ( int dim = 0; dim < 3; dim++ )
		{
			rC = this.getResolutionCalculator(coord, dim);
			out *= rC.getResolution(coord[dim]);
		}
		return out;
	}
}

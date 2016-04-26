package shape;

import grid.CylindricalGrid;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import shape.resolution.ResolutionCalculator.ResCalc;
import surface.Rod;
import utility.ExtraMath;

/**
 * 
 * 
 */
public abstract class CylindricalShape extends Shape
{
	/**
	 * Collection of resolution calculators for each dimension.
	 */
	protected ResCalc[][] _resCalc;
	
	/*************************************************************************
	 * CONSTRUCTION
	 ************************************************************************/
	
	public CylindricalShape()
	{
		super();
		this._resCalc = new ResCalc[3][];
		Dimension dim;
		/* There is no need for an r-min boundary. */
		dim = new Dimension();
		dim.setBoundaryOptional(0);
		this._dimensions.put(DimName.R, dim);
		/*
		 * Set to a full circle by default, let it be overwritten later.
		 */
		dim = new Dimension();
		dim.setCyclic();
		dim.setLength(2 * Math.PI);
		dim.setInsignificant();
		this._dimensions.put(DimName.THETA, dim);
		/*
		 * The z-dimension is insignificant, unless told otherwise later.
		 */
		dim = new Dimension();
		dim.setInsignificant();
		this._dimensions.put(DimName.Z, dim);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	@Override
	public GridGetter gridGetter()
	{
		return CylindricalGrid.standardGetter();
	}
	

	@Override
	public double[] getLocalPosition(double[] location)
	{
		return Vector.toCylindrical(location);
	}
	
	@Override
	public double[] getGlobalLocation(double[] local)
	{
		return Vector.cylindricalToCartesian(local);
	}
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/
	
	@Override
	public void setDimensionResolution(DimName dName, ResCalc resC)
	{
		int index = this.getDimensionIndex(dName);
		switch ( dName )
		{
		case R:
		{
			this._resCalc[index][0] = resC;
			this.trySetDimRes(DimName.THETA);
			return;
		}
		case THETA:
		{
			ResCalc radiusC = this._resCalc[0][0];
			if ( radiusC == null )
				this._rcStorage.put(dName, resC);
			else
			{
				int nShell = radiusC.getNVoxel();
				this._resCalc[index] = new ResCalc[nShell];
				double shellRadius;
				ResCalc shellResCalc;
				for ( int i = 0; i < nShell; i++ )
				{
					/* Find the mid-point of this shell. */
					shellRadius = radiusC.getPosition(i, 0.5);
					shellResCalc = (ResCalc) resC.copy();
					shellResCalc.setLength(shellRadius);
					this._resCalc[index][i] = shellResCalc;
				}
			}
		}
		case Z:
		{
			this._resCalc[index][0] = resC;
			return;
		}
		default:
		{
			// TODO safety
			return;
		}
		}
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int dim)
	{
		int index = 0;
		if ( dim == 1 )
		{
			index = coord[0];
			// TODO check if valid?
		}
		return this._resCalc[dim][index];
	}
	
	/*************************************************************************
	 * SURFACES
	 ************************************************************************/
	
	public void setSurfaces()
	{
		/*
		 * The ends of the Rod axis.
		 */
		int nDim = this.getNumberOfDimensions();
		double[] pointA = Vector.zerosDbl(nDim);
		double[] pointB = Vector.zerosDbl(nDim);
		/*
		 * Check if we need to use the Z dimension.
		 */
		// TODO move this into Cylinder somehow?
		Dimension zDim = this.getDimension(DimName.Z);
		if ( zDim.isSignificant() )
		{
			pointA[2] = zDim.getExtreme(0);
			pointB[2] = zDim.getExtreme(1);
		}
		/*
		 * Find the radii and add the rod(s).
		 */
		Dimension radiusDim = this.getDimension(DimName.R);
		/* If there is an inner radius, use it. */
		double radius = radiusDim.getExtreme(0);
		if ( radius > 0.0 )
			this._surfaces.add(new Rod(pointA, pointB, radius));
		/* We always use the outer radius. */
		radius = radiusDim.getExtreme(1);
		this._surfaces.add(new Rod(pointA, pointB, radius));
		/*
		 * If theta is not cyclic, we need to add two planes.
		 */
		Dimension thetaDim = this.getDimension(DimName.THETA);
		if ( ! thetaDim.isCyclic() )
		{
			// TODO can we use Shape.setPlanarSurfaces() here?
			// Probably depends on which coordinate system we use.
		}
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
		double[] origin = this.getVoxelOrigin(coord);
		double[] upper = this.getVoxelUpperCorner(coord);
		/* 
		 * r: pi times this number would be the area of a ring. 
		 */
		double volume = ExtraMath.sq(upper[0]) - ExtraMath.sq(origin[0]);
		/* 
		 * theta: this number divided by pi would be the arc length.
		 */
		volume *= (upper[1] - origin[1]) * 0.5;
		/* 
		 * z: height. 
		 */
		volume *= (upper[2] - origin[2]);
		return volume;
	}
	
	@Override
	protected void getNVoxel(int[] coord, int[] outNVoxel)
	{
		int nDim = this.getNumberOfDimensions();
		/* Initialise the out vector if necessary. */
		if ( outNVoxel == null )
			outNVoxel = Vector.zerosInt(nDim);
		
		ResCalc rC;
		for ( int dim = 0; dim < nDim; dim++ )
		{
			// TODO check if coord is valid?
			rC = this.getResolutionCalculator(coord, dim);
			outNVoxel[dim] = rC.getNVoxel();
		}
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
		//TODO
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		// TODO
		return null;
	}
}

package shape;

import grid.CylindricalGrid;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import static shape.ShapeConventions.DimName.R;
import static shape.ShapeConventions.DimName.THETA;
import static shape.ShapeConventions.DimName.Z;
import shape.resolution.ResolutionCalculator.ResCalc;
import surface.Rod;
import surface.Surface;
import utility.ExtraMath;

/**
 * 
 * 
 */
public abstract class CylindricalShape extends PolarShape
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
		this._dimensions.put(R, dim);
		/*
		 * Set to a full circle by default, let it be overwritten later.
		 */
		dim = new Dimension(false);
		dim.setCyclic();
		dim.setLength(2 * Math.PI);
		this._dimensions.put(THETA, dim);
		/*
		 * The z-dimension is insignificant, unless told otherwise later.
		 */
		dim = new Dimension(false);
		this._dimensions.put(Z, dim);
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
		return Vector.cylindrify(location);
	}
	
	@Override
	public double[] getGlobalLocation(double[] local)
	{
		return Vector.uncylindrify(local);
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
			this.trySetDimRes(THETA);
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
	 * LOCATIONS
	 ************************************************************************/
	
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
		Dimension zDim = this.getDimension(Z);
		if ( zDim.isSignificant() )
		{
			pointA[2] = zDim.getExtreme(0);
			pointB[2] = zDim.getExtreme(1);
		}
		/*
		 * Find the radii and add the rod(s).
		 */
		Dimension radiusDim = this.getDimension(R);
		/* If there is an inner radius, use it. */
		double radius = radiusDim.getExtreme(0);
		if ( radius > 0.0 )
		{
			Surface rod = new Rod(pointA, pointB, radius);
			this._surfaces.put(rod, radiusDim.getBoundary(0));
		}
		/* We always use the outer radius. */
		radius = radiusDim.getExtreme(1);
		Surface rod = new Rod(pointA, pointB, radius);
		this._surfaces.put(rod, radiusDim.getBoundary(1));
		/*
		 * If theta is not cyclic, we need to add two planes.
		 */
		Dimension thetaDim = this.getDimension(THETA);
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
	protected void nVoxelTo(int[] destination, int[] coord)
	{
		int nDim = this.getNumberOfDimensions();
		/* Initialise the out vector if necessary. */
		if ( destination == null )
			destination = Vector.zerosInt(nDim);
		
		ResCalc rC;
		for ( int dim = 0; dim < nDim; dim++ )
		{
			// TODO check if coord is valid?
			rC = this.getResolutionCalculator(coord, dim);
			destination[dim] = rC.getNVoxel();
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
		this._nbhValid = true;
		/* See if we can use the inside r-shell. */
		if ( this.setNbhFirstInNewShell(this._currentCoord[0] - 1) )
			return;
		/* See if we can take one of the theta-neighbors. */
		if (this.moveNbhToMinus(THETA)||this.nbhJumpOverCurrent(THETA))
			return;
		/* See if we can take one of the z-neighbors. */
		if (this.moveNbhToMinus(Z)||this.nbhJumpOverCurrent(Z))
			return;
		/* See if we can use the outside r-shell. */
		if ( this.setNbhFirstInNewShell(this._currentCoord[0] + 1) )
			return;
		/* There are no valid neighbors. */
		this._nbhValid = false;
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		/*
		 * In the cylindrical grid, we start the TODO
		 */
		if ( this._currentNeighbor[0] == this._currentCoord[0] - 1 )
		{
			/* 
			 * We're in the r-shell just inside that of the current coordinate.
			 * Try increasing theta by one voxel. If this fails, move out to
			 * the next shell. If this fails, call this method again.
			 */
			if ( ! this.increaseNbhByOnePolar(THETA) )
				if ( ! this.moveNbhToMinus(THETA) )
					return this.nbhIteratorNext();
					
		}
		else if ( this._currentNeighbor[0] == this._currentCoord[0] )
		{
			/* 
			 * We're in the same r-shell as the current coordinate.
			 */
			if ( this._currentNeighbor[2] == this._currentCoord[2] )
			{
				/*
				 * We're in the same z-slice as the current coordinate.
				 * Try to move to the theta-plus side of the current
				 * coordinate. If you can't, try switching to the z-minus
				 * voxel.
				 */
				if ( ! this.nbhJumpOverCurrent(THETA) )
					if ( ! this.moveNbhToMinus(Z) )
						return this.nbhIteratorNext();
			}
			else if ( ! this.nbhJumpOverCurrent(Z) )
			{
				/*
				 * We tried to move to the z-plus side of the current
				 * coordinate, but since we failed we must be finished.
				 */
				this.moveNbhToOuterShell();
			}
		}
		else 
		{
			/* 
			 * We're in the r-shell just outside that of the current coordinate.
			 * If we can't increase theta any more, then we've finished.
			 */
			if ( ! this.increaseNbhByOnePolar(THETA) )
				this._nbhValid = false;
		}
		return this._currentNeighbor;
	}
	
	/**
	 * \brief Try moving the neighbor iterator to the r-shell just outside that
	 * of the current coordinate.
	 * 
	 * <p>Set the neighbor iterator valid flag to false if this fails.</p>
	 */
	protected void moveNbhToOuterShell()
	{
		if ( ! this.setNbhFirstInNewShell(this._currentCoord[0] + 1) )
			this._nbhValid = false;
	}
}

package shape;

import static shape.Shape.WhereAmI.UNDEFINED;

import shape.Dimension.Dim;
import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
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
		dim = new Dimension(Dim.R);
		dim.setBoundaryOptional(0);
		this._dimensions.add(dim);
		this._resCalc[getDimensionIndex(Dim.R)] = new ResCalc[1];
		/*
		 * Set to a full circle by default, let it be overwritten later.
		 */
		dim = new Dimension(Dim.THETA);
		dim.setCyclic();
		dim.setLength(2 * Math.PI);
		this._dimensions.add(dim);
		/*
		 * The z-dimension is insignificant, unless told otherwise later.
		 */
		dim = new Dimension(false, Dim.Z);
		this._dimensions.add(dim);
		this._resCalc[getDimensionIndex(Dim.Z)] = new ResCalc[1];
	}
	
	@Override
	public double[][][] getNewArray(double initialValue) {
		return PolarArray.createCylinder(this._resCalc, initialValue);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	

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
	public void setDimensionResolution(Dim dName, ResCalc resC)
	{
		int index = this.getDimensionIndex(dName);
		switch ( dName )
		{
		case R:
		{
			this._resCalc[index][0] = resC;
			this.trySetDimRes(Dim.THETA);
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
				ResCalc shellResCalc;
				for ( int i = 0; i < nShell; i++ )
				{
					shellResCalc = (ResCalc) resC.copy();
					/* since we so not allow initialization with varying 
					 * resolutions, they should all be the same here 
					 */
					shellResCalc.setResolution(scaleResolutionForShell(i, resC.getResolution(0)));
					this._resCalc[index][i] = shellResCalc;
				}
			}
			return;
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
		Dimension zDim = this.getDimension(Dim.Z);
		if ( zDim.isSignificant() )
		{
			pointA[2] = zDim.getExtreme(0);
			pointB[2] = zDim.getExtreme(1);
		}
		/*
		 * Find the radii and add the rod(s).
		 */
		Dimension radiusDim = this.getDimension(Dim.R);
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
		Dimension thetaDim = this.getDimension(Dim.THETA);
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
		/* See if we can use the inside r-shell. */
		if ( this.setNbhFirstInNewShell(this._currentCoord[0] - 1) ) ;
		/* See if we can take one of the theta-neighbors. */
		else if (this.moveNbhToMinus(Dim.THETA)||this.nbhJumpOverCurrent(Dim.THETA)) ;
		/* See if we can take one of the z-neighbors. */
		else if (this.moveNbhToMinus(Dim.Z)||this.nbhJumpOverCurrent(Dim.Z)) ;
		/* See if we can use the outside r-shell. */
		else if ( this.setNbhFirstInNewShell(this._currentCoord[0] + 1) ) ;
		/* There are no valid neighbors. */
		else
			this._whereIsNbh = UNDEFINED;
		if ( this.isNbhIteratorValid() )
		{
			transformNbhCyclic();
			return;
		}
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		this.untransformNbhCyclic();
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
			if ( ! this.increaseNbhByOnePolar(Dim.THETA) )
				if ( ! this.moveNbhToMinus(Dim.THETA) )
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
				if ( ! this.nbhJumpOverCurrent(Dim.THETA) )
					if ( ! this.moveNbhToMinus(Dim.Z) )
						return this.nbhIteratorNext();
			}
			else if ( ! this.nbhJumpOverCurrent(Dim.Z) )
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
			if ( ! this.increaseNbhByOnePolar(Dim.THETA) )
				this._whereIsNbh = UNDEFINED;
		}
		this.transformNbhCyclic();
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
			this._whereIsNbh = UNDEFINED;
	}
}

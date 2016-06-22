package shape;

import static shape.Dimension.DimName;
import static shape.Dimension.DimName.*;
import static shape.Shape.WhereAmI.UNDEFINED;

import dataIO.Log.Tier;
import linearAlgebra.Matrix;
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
	
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
	public CylindricalShape()
	{
		super();
		this._resCalc = new ResCalc[3][];
		Dimension dim;
		/* There is no need for an r-min boundary. */
		dim = new Dimension(true, R);
		dim.setBoundaryOptional(0);
		this._dimensions.put(R, dim);
		this._resCalc[getDimensionIndex(R)] = new ResCalc[1];
		/*
		 * The theta-dimension is insignificant, unless told otherwise later.
		 */
		dim = new Dimension(false, THETA);
		this._dimensions.put(THETA, dim);
		/*
		 * The z-dimension is insignificant, unless told otherwise later.
		 */
		dim = new Dimension(false, Z);
		this._dimensions.put(Z, dim);
		this._resCalc[getDimensionIndex(Z)] = new ResCalc[1];
	}
	
	@Override
	public double[][][] getNewArray(double initialValue) {
		int nr, nz;
		if (getNumberOfDimensions() < 2)
			throw new IllegalArgumentException(
					"A cylindrical array needs at least 2 dimensions");
		nr = _resCalc[0][0].getNVoxel();
		/* we need at least one voxel in each dimension */ 
		nz = _resCalc[2][0] == null ? 1 : _resCalc[2][0].getNVoxel();
		double[][][] a = new double[nr][][];
		for ( int i = 0; i < nr; i++ )
			a[i] = Matrix.matrix(_resCalc[1][i].getNVoxel(), nz, initialValue);
		return a;
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	

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
	
	/* ***********************************************************************
	 * DIMENSIONS
	 * **********************************************************************/
	
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
				ResCalc shellResCalc;
				for ( int i = 0; i < nShell; i++ )
				{
					shellResCalc = (ResCalc) resC.copy();
					/* since we do not allow initialization with varying 
					 * resolutions, resC.getResolution(x) should all be the 
					 * same at this point. 
					 */
					shellResCalc.setResolution(scaleResolutionForShell(i,
							resC.getResolution(0)));
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
		/* 
		 * If this is the radial dimension (0) or the z dimension (2),
		 * always use the first.
		 * 
		 * If it is the theta dimension (1), use the r-index.
		 */
		int index = 0;
		if ( dim == 1 )
		{
			index = coord[0];
			// TODO check if valid?
		}
		return this._resCalc[dim][index];
	}
	
	/* ***********************************************************************
	 * LOCATIONS
	 * **********************************************************************/
	
	/* ***********************************************************************
	 * SURFACES
	 * **********************************************************************/
	
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
	
	/* ***********************************************************************
	 * BOUNDARIES
	 * **********************************************************************/
	
	@Override
	public double getBoundarySurfaceArea(DimName dimN, int extreme)
	{
		Tier level = Tier.BULK;
		switch( dimN )
		{
		case R:
		{
			/* 
			 * Area is a curved rectangle (cylinder if theta length is 2 pi).
			 */
			double rExt = this.getDimension(R).getExtreme(extreme);
			double thetaLength = this.getDimension(THETA).getLength();
			double arcLength = rExt * thetaLength;
			double zLength = this.getDimension(Z).getLength();
			double area = arcLength * zLength;
			return area;
		}
		case THETA:
		{
			/* 
			 * For theta boundaries, it makes no difference which extreme.
			 * Area is simply a rectangle of area (r * z).
			 */
			double rLength = this.getDimension(R).getLength();
			double zLength = this.getDimension(Z).getLength();
			double area = rLength * zLength;
			return area;
		}
		case Z:
		{
			/* 
			 * For z boundaries, it makes no difference which extreme.
			 * Area is simply the area of the rMax circle, minus the area of
			 * the rMin circle (this may be zero). Assumes rMax > rMin > 0
			 */
			double thetaLength = this.getDimension(THETA).getLength();
			Dimension r = this.getDimension(R);
			double rMin = r.getExtreme(0);
			double rMax = r.getExtreme(1);
			double area = thetaLength*(ExtraMath.sq(rMax)-ExtraMath.sq(rMin));
			return area;
		}
		default:
		{
			// TODO safety
			return Double.NaN;
		}
		}
	}
	
	/* ***********************************************************************
	 * VOXELS
	 * **********************************************************************/
	
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
		/* See if we can use the inside r-shell. */
		if ( this.setNbhFirstInNewShell(this._currentCoord[0] - 1) ) ;
		/* See if we can take one of the theta-neighbors. */
		else if (this.moveNbhToMinus(THETA)||this.nbhJumpOverCurrent(THETA)) ;
		/* See if we can take one of the z-neighbors. */
		else if (this.moveNbhToMinus(Z)||this.nbhJumpOverCurrent(Z)) ;
		/* See if we can use the outside r-shell. */
		else if ( this.setNbhFirstInNewShell(this._currentCoord[0] + 1) ) ;
		/* There are no valid neighbors. */
		else
			this._whereIsNhb = UNDEFINED;
		if ( this.isNbhIteratorValid() )
		{
			transformNbhCyclic();
			return;
		}
	}
	
	//TODO: does it even make sense to cross the r boundary ? 
	// Or should we just forbid cyclic r (and phi in the sphere) dimensions? 
//	@Override
//	protected void transformNbhCyclic() {
//		super.transformNbhCyclic();
//		/* lets (additionally) choose a random location on the other side of 
//		 * the shape when crossing the radial boundary.
//		 * This will be always zero when moving from max to min */
//		if (this._nbhDimName == R)
//			this._currentNeighbor[1] = ExtraMath.random.nextInt(
//				this._resCalc[1][this._currentNeighbor[0]].getNVoxel());
//	}
	
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
				this._whereIsNhb = UNDEFINED;
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
			this._whereIsNhb = UNDEFINED;
	}
}

package shape;

import static shape.ShapeConventions.DimName.PHI;
import static shape.ShapeConventions.DimName.R;
import static shape.ShapeConventions.DimName.THETA;
import static shape.Shape.WhereAmI.INSIDE;
import static shape.Shape.WhereAmI.UNDEFINED;

import linearAlgebra.PolarArray;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import shape.resolution.ResolutionCalculator.ResCalc;
import surface.Ball;
import surface.Point;
import utility.ExtraMath;

public abstract class SphericalShape extends PolarShape
{
	/**
	 * Collection of resolution calculators for each dimension.
	 */
	protected ResCalc[][][] _resCalc;
	
	/*************************************************************************
	 * CONSTRUCTION
	 ************************************************************************/
	
	public SphericalShape()
	{
		super();
		/*
		 * Set up the array of resolution calculators.
		 */
		this._resCalc = new ResCalc[3][][];
		/* radius */
		this._resCalc[0] = new ResCalc[1][];
		this._resCalc[0][0] = new ResCalc[1];
		/* phi */
		this._resCalc[1] = new ResCalc[1][];
		/* theta will depend on phi, so leave for now. */
		/*
		 * Set up the dimensions.
		 */
		Dimension dim;
		/* There is no need for an r-min boundary. */
		dim = new Dimension();
		dim.setBoundaryOptional(0);
		this._dimensions.put(R, dim);
		/*
		 * Set full angular dimensions by default, can be overwritten later.
		 */
		dim = new Dimension();
		dim.setCyclic();
		dim.setLength(Math.PI);
		this._dimensions.put(PHI, dim);
		dim = new Dimension();
		dim.setCyclic();
		dim.setLength(2 * Math.PI);
		this._dimensions.put(THETA, dim);
	}
	
	@Override
	public double[][][] getNewArray(double initialValue) {
		return PolarArray.createSphere(this._resCalc, initialValue);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	@Override
	public double[] getLocalPosition(double[] location)
	{
		return Vector.unspherify(location);
	}
	
	@Override
	public double[] getGlobalLocation(double[] local)
	{
		return Vector.spherify(local);
	}
	
	/*************************************************************************
	 * DIMENSIONS
	 ************************************************************************/
	
	@Override
	public void setDimensionResolution(DimName dName, ResCalc resC)
	{
		int index = this.getDimensionIndex(dName);
		ResCalc radiusC = this._resCalc[0][0][0];
		int nShell;
		ResCalc focalResCalc;
		/*
		 * How we initialise this resolution calculator depends on the
		 * dimension it is used for.
		 */
		switch ( dName )
		{
		case R:
		{
			this._resCalc[index][0][0] = resC;
			/* Check if phi is waiting for the radius before returning. */
			trySetDimRes(PHI);
			return;
		}
		case PHI:
		{
			if ( radiusC == null )
			{
				this._rcStorage.put(dName, resC);
				return;
			}
			nShell = radiusC.getNVoxel();
			this._resCalc[1][0] = new ResCalc[nShell];
			for ( int i = 0; i < nShell; i++ )
			{
				focalResCalc = (ResCalc) resC.copy();
				focalResCalc.setResolution(scaleResolutionForShell(i, resC.getResolution(0)));
				this._resCalc[1][0][i] = focalResCalc;
			}
			/* Check if theta is waiting for phi before returning. */
			trySetDimRes(THETA);
			return;
		}
		case THETA:
		{
			ResCalc[] phiC = this._resCalc[1][0];
			if ( radiusC == null || phiC == null )
			{
				this._rcStorage.put(dName, resC);
				return;
			}
			/*
			 * We have what we need for theta, so set up the array of
			 * resolution calculators.
			 */
			nShell = radiusC.getNVoxel();
			this._resCalc[2] = new ResCalc[nShell][];
			/* Iterate over the shells. */
			int nRing;
			for ( int shell = 0; shell < nShell; shell++ )
			{
				/* Prepare the array of ResCalcs for this shell. */
				nRing = phiC[shell].getNVoxel();
				this._resCalc[2][shell] = new ResCalc[nRing];
				/* Iterate over the rings in this shell. */
				for ( int ring = 0; ring < nRing; ring++ )
				{
					focalResCalc = (ResCalc) resC.copy();
					focalResCalc.setResolution(scaleResolutionForRing(shell, ring, resC.getResolution(0)));
					this._resCalc[2][shell][ring] = focalResCalc;
				}
			}
		}
		default:
		{
			// TODO safety
		}
		}
	}
	
	@Override
	protected ResCalc getResolutionCalculator(int[] coord, int axis)
	{
		switch ( axis )
		{
			/* r */
			case 0: return this._resCalc[0][0][0];
			/* phi */
			case 1: return this._resCalc[1][0][coord[0]];
			/* theta */
			case 2: return this._resCalc[2][coord[0]][coord[1]];
			// TODO throw an exception?
			default: return null;
		}
	}
	
	/*************************************************************************
	 * LOCATIONS
	 ************************************************************************/
	
	/*************************************************************************
	 * SURFACES
	 ************************************************************************/
	
	public void setSurfaces()
	{
		Dimension dim = this.getDimension(R);
		double[] centre = Vector.zerosDbl(this.getNumberOfDimensions());
		Ball outbound;
		double radius;
		/* Inner radius, if it exists. */
		radius = dim.getExtreme(0);
		if ( radius > 0.0 )
		{
			outbound = new Ball( new Point(centre) , radius);
			outbound.bounding = false;
			this._surfaces.put(outbound, dim.getBoundary(0));
		}
		/* Outer radius always exists. */
		radius = dim.getExtreme(1);
		outbound = new Ball( new Point(centre) , radius);
		outbound.bounding = true;
		this._surfaces.put(outbound, dim.getBoundary(1));
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
		//TODO: wrong
		// mathematica: Integrate[r^2 sin p,{p,p1,p2},{t,t1,t2},{r,r1,r2}] 
		double[] loc1 = getVoxelOrigin(coord);
		double[] loc2 = getVoxelUpperCorner(coord);
		/* r */
		double out = ExtraMath.cube(loc1[0]) - ExtraMath.cube(loc2[0]);
		/* phi */
		out *= loc1[1] - loc2[1];
		/* theta */
		out *= Math.cos(loc1[2]) - Math.cos(loc2[2]);
		return out / 3.0;
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
		if ( this.setNbhFirstInNewShell( this._currentCoord[0] - 1 ) 
			&& this.setNbhFirstInNewRing( this._currentNeighbor[1] ) ) ;
		/* 
		 * See if we can take one of the phi-minus-neighbors of the current 
		 * r-shell. 
		 */
		else if ( this.setNbhFirstInNewShell( this._currentCoord[0]) 
					&& this.setNbhFirstInNewRing( this._currentCoord[1] - 1) ) ;
		/* 
		 * See if we can take one of the theta-neighbors in the current r-shell.
		 */
		else if ( this.moveNbhToMinus(THETA) || this.nbhJumpOverCurrent(THETA) ) ;
		/* See if we can take one of the phi-plus-neighbors. */
		else if ( this.setNbhFirstInNewRing( this._currentCoord[1] + 1) ) ;
		
		/* See if we can use the outside r-shell. */
		else if ( this.setNbhFirstInNewShell( this._currentCoord[0] + 1 ) 
					&& this.setNbhFirstInNewRing( this._currentNeighbor[1] ) ) ;
		/* There are no valid neighbors. */
		else this._nbhValid = false;
		
		if ( this._nbhValid )
		{
			transformNbhCyclic();
			return;
		}
		
		this._whereIsNbh = UNDEFINED;
		this._nbhValid = false;
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		this.untransformNbhCyclic();
		/*
		 * In the spherical shape, we start the TODO
		 */
		if ( this._currentNeighbor[0] == this._currentCoord[0] - 1 )
		{
			/*
			 * We're in the r-shell just inside that of the current coordinate.
			 */
			int curR = this._currentCoord[0];
			int nbhPhi = this._currentNeighbor[1];
			/* Try increasing theta by one voxel. */
			if ( ! this.increaseNbhByOnePolar(THETA) )
			{
				/* Try moving out to the next ring. This must exist! */
				if ( ! this.increaseNbhByOnePolar(PHI) ||
										! this.setNbhFirstInNewRing(nbhPhi) )
				{
					/* 
					 * The current coordinate must be the only voxel in that 
					 * ring. If that fails, try the phi-minus ring.
					 */
					if ( ! this.setNbhFirstInNewShell(curR) || 
									! this.setNbhFirstInNewRing(nbhPhi - 1) )
					{
						/*
						 * If this fails, the phi-ring must be invalid, so try
						 * to move to the theta-minus neighbor in the current
						 * phi-ring.
						 * If this fails call this method again.
						 */
						if ( ! this.moveNbhToMinus(THETA) )
							return this.nbhIteratorNext();
					}
				}
			}
		}
		else if ( this._currentNeighbor[0] == this._currentCoord[0] )
		{
			/* 
			 * We're in the same r-shell as the current coordinate.
			 */
			if ( this._currentNeighbor[1] == this._currentCoord[1] - 1 )
			{
				/*
				 * We're in the phi-ring just inside that of the 
				 * current coordinate.
				 * Try increasing theta by one voxel. If this fails, move out
				 * to the next ring. If this fails, call this method again.
				 */
				if ( ! this.increaseNbhByOnePolar(THETA) )
					if ( ! this.moveNbhToMinus(THETA) )
						return this.nbhIteratorNext();
			}
			else if ( this._currentNeighbor[1] == this._currentCoord[1] )
			{
				/*
				 * We're in the same phi-ring as the current coordinate.
				 * Try to jump to the theta-plus side of the current
				 * coordinate. If you can't, try switching to the phi-plus
				 * ring.
				 */
				if ( ! this.nbhJumpOverCurrent(THETA) )
					if ( ! this.setNbhFirstInNewRing(this._currentCoord[1]+1) )
						return this.nbhIteratorNext();
			}
			else 
			{
				/* 
				 * We're in the phi-ring just above that of the current
				 * coordinate. 
				 */
				int rPlus = this._currentCoord[0] + 1;
				int nbhPhi = this._currentNeighbor[1];
				/* Try increasing theta by one voxel. */
				if ( ! this.increaseNbhByOnePolar(THETA) )
				{
					/* Move out to the next shell or the next rings. */
					if (! this.setNbhFirstInNewShell(rPlus) ||
									! this.setNbhFirstInNewRing(nbhPhi) )
					{
						if ( ! this.increaseNbhByOnePolar(PHI) ||
								! this.setNbhFirstInNewRing(nbhPhi) )
						{
							if (!this.increaseNbhByOnePolar(PHI) ||
									! this.setNbhFirstInNewRing(nbhPhi) )
							{
								this._nbhValid = false;
							}
						}
					}
				}
			}
		}
		else 
		{
			/* 
			 * We're in the r-shell just outside that of the current coordinate.
			 * If we can't increase phi and theta any more, then we've finished.
			 */
			if ( ! this.increaseNbhByOnePolar(THETA) )
				if ( ! this.increaseNbhByOnePolar(PHI) ||
						! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
				{
					this._nbhValid = false;
				}
		}
		this.transformNbhCyclic();
		return this._currentNeighbor;
	}
	
	/**
	 * TODO
	 * 
	 * @param shellIndex
	 * @return
	 */
	protected boolean setNbhFirstInNewRing(int ringIndex)
	{
		this._currentNeighbor[1] = ringIndex;
		/*
		 * We must be on a ring inside the array: not even a defined boundary
		 * will do here.
		 */
		if ( this.whereIsNhb(R) != INSIDE )
			return false;
		/*
		 * First check that the new ring is inside the grid. If we're on a
		 * defined boundary, the theta coordinate is irrelevant.
		 */
		if ( this.whereIsNhb(THETA) != INSIDE )
			return false;
		
		ResCalc rC = this.getResolutionCalculator(this._currentCoord, 2);
		/*
		 * We're on an intermediate ring, so find the voxel which has the
		 * current coordinate's minimum theta angle inside it.
		 */
		double theta = rC.getCumulativeResolution(this._currentCoord[2] - 1);
		
		rC = this.getResolutionCalculator(this._currentNeighbor, 2);
		
		this._currentNeighbor[2] = rC.getVoxelIndex(theta);
		
		this._nbhDimName = this._currentCoord[1] == this._currentNeighbor[1] ?
				DimName.THETA : DimName.PHI;
		int dimIdx = getDimensionIndex(this._nbhDimName);
		this._nbhDirection = 
				this._currentCoord[dimIdx]
						< this._currentNeighbor[dimIdx] ? 1 : 0;
		return true;
	}
}

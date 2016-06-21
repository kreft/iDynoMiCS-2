package shape;

import static shape.Dimension.DimName.PHI;
import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;
import static shape.Shape.WhereAmI.INSIDE;
import static shape.Shape.WhereAmI.UNDEFINED;

import dataIO.Log;
import linearAlgebra.Vector;
import shape.Dimension.DimName;
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
		/* There is no need for an r-min boundary. 
		 * R must always be significant and non-cyclic */
		dim = new Dimension(true, R); 
		dim.setBoundaryOptional(0);
		this._dimensions.put(R, dim);
		/*
		 * Phi must always be significant and non-cyclic.
		 */
		dim = new Dimension(true, PHI);
		this._dimensions.put(PHI, dim);
		
		dim = new Dimension(false, THETA);
		this._dimensions.put(THETA, dim);
	}
	
	@Override
	public double[][][] getNewArray(double initialValue) {
		if (getNumberOfDimensions() != 3)
			throw new IllegalArgumentException(
					"A spherical array needs 3 dimensions");

		int nR = _resCalc[0][0][0].getNVoxel();
		int nPhi;
		double[][][] a = new double[nR][][];
		for ( int r = 0; r < nR; r++ )
		{
			nPhi = _resCalc[1][0][r].getNVoxel();
			
			a[r] = new double[nPhi][];
			for ( int p = 0; p < nPhi; p++ )
				a[r][p] = Vector.vector(_resCalc[2][r][p].getNVoxel(),
																initialValue);
		}
		return a;
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	@Override
	public double[] getLocalPosition(double[] location)
	{
		return Vector.spherify(location);
	}
	
	@Override
	public double[] getGlobalLocation(double[] local)
	{
		return Vector.unspherify(local);
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
					/* For varying resolution this will not work anymore */
					focalResCalc.setResolution(scaleResolutionForRing(shell,
							ring, phiC[shell].getResolution(0),
							resC.getResolution(0)));
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
		// mathematica: Integrate[r^2 sin p,{p,p1,p1+dp},{t,t1,t1+dt},{r,r1,r1+dr}] 
		double[] loc1 = getVoxelOrigin(coord);
		double[] loc2 = getVoxelUpperCorner(coord);
		double[] dloc = new double[3];
		Vector.minusTo(dloc, loc2, loc1);
		/* theta */
		double out = dloc[0] * dloc[2];
		/* r */
		out *= 3 * ExtraMath.sq(loc1[0]) + 3 * loc1[0] * dloc[0] 
														+ ExtraMath.sq(dloc[0]);
		/* phi */
		out *= Math.sin(dloc[1] / 2) * Math.sin(loc1[1] + dloc[1] / 2);
		return out * 2 / 3;
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
		else
			this._whereIsNhb = UNDEFINED;
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
		 * In the spherical shape, we start the TODO
		 */
		if ( this._currentNeighbor[0] == this._currentCoord[0] - 1 )
		{
			/*
			 * We're in the r-shell just inside that of the current coordinate.
			 */
			int curR = this._currentCoord[0];
			/* Try increasing theta by one voxel. */
			if ( ! this.increaseNbhByOnePolar(THETA) )
			{
				/* Try moving out to the next ring and set the first phi nhb */
				if ( ! this.increaseNbhByOnePolar(PHI) ||
										! this.setNbhFirstInNewRing(
												this._currentNeighbor[1]) )
				{
					/* 
					 * Try moving to the next shell. This sometimes misses the
					 * first valid phi coord (limited double accuracy), so lets
					 * additionally try the phi-minus ring.
					 */
					if ( ( this.setNbhFirstInNewShell(curR)
						&& ! this.setNbhFirstInNewRing(this._currentCoord[1]) )
							|| ! this.setNbhFirstInNewRing(
												this._currentCoord[1] - 1))
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
					if ( ! this.setNbhFirstInNewRing(this._currentCoord[1] + 1))
						return this.nbhIteratorNext();
			}
			else 
			{
				/* 
				 * We're in the phi-ring just above that of the current
				 * coordinate. 
				 */
				int rPlus = this._currentCoord[0] + 1;
				int nbhPhi = this._currentCoord[1];
				/* Try increasing theta by one voxel. */
				if ( ! this.increaseNbhByOnePolar(THETA) )
				{
					/* Move out to the next shell or the next rings. */
					if (! this.setNbhFirstInNewShell(rPlus) ||
									! this.setNbhFirstInNewRing(nbhPhi) )
					{
						this.nbhIteratorNext();
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
				if (!this.increaseNbhByOnePolar(PHI) ||
						! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
				{
					this._whereIsNhb = UNDEFINED;
				}
		}
		this.transformNbhCyclic();
		return this._currentNeighbor;
	}
	
	/**
	 * this will set the current neighbor's phi coordinate to ringIndex and 
	 * attempt to set the theta coordinate.
	 * 
	 * @param shellIndex
	 * @return
	 */
	protected boolean setNbhFirstInNewRing(int ringIndex)
	{
		//TODO this will currently not set onto min boundary?
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
		{
			Log.out(NHB_ITER_LEVEL, "  trying to set neighbor in new ring "+
				ringIndex);
		}
		this._currentNeighbor[1] = ringIndex;
		/*
		 * We must be on a ring inside the array: not even a defined boundary
		 * will do here.
		 */
		if ( this.whereIsNhb(R) != INSIDE )
		{
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				Log.out(NHB_ITER_LEVEL, "  failure, R on any boundary");
			return false;
		}
		/*
		 * First check that the new ring is inside the grid. If we're on a
		 * defined boundary, the theta coordinate is irrelevant.
		 */
		if ( (this._whereIsNhb = this.whereIsNhb(PHI)) != INSIDE )
		{
			this._nhbDimName = PHI;
			if ( this._whereIsNhb != UNDEFINED )
			{
				if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				{
					Log.out(NHB_ITER_LEVEL, "  success on "+ this._whereIsNhb 
						+" boundary");
				}
				return true;
			}
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
				Log.out(NHB_ITER_LEVEL, "  failure, PHI on undefined boundary");
			return false;
		}

		ResCalc rC = this.getResolutionCalculator(this._currentCoord, 2);
		/*
		 * We're on an intermediate ring, so find the voxel which has the
		 * current coordinate's minimum theta angle inside it.
		 */
		double theta = rC.getCumulativeResolution(this._currentCoord[2] - 1);
		
		rC = this.getResolutionCalculator(this._currentNeighbor, 2);
		
		int new_index = rC.getVoxelIndex(theta);

		/* increase the index if it has approx. the same theta location as the
		 * current coordinate */
		if ( ExtraMath.areEqual(
				rC.getCumulativeResolution(new_index), theta, 
				POLAR_ANGLE_EQ_TOL) )
		{
			new_index++;
		}
		/* if we stepped onto the current coord, we went too far*/
		if ( this._currentNeighbor[0] == this._currentCoord[0] 
				&& this._currentNeighbor[1] == this._currentCoord[1]
				&& new_index == this._currentCoord[2] )
		{
			if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			{
				Log.out(NHB_ITER_LEVEL, 
						"  failure, stepped onto current coordinate");
			}
			return false;
		}
		
		this._currentNeighbor[2] = new_index;
		
		this._nhbDimName = this._currentCoord[1] == this._currentNeighbor[1] ?
					THETA : PHI;
		
		int dimIdx = getDimensionIndex(this._nhbDimName);
		this._nhbDirection = 
				this._currentCoord[dimIdx]
						< this._currentNeighbor[dimIdx] ? 1 : 0;
		if ( Log.shouldWrite(NHB_ITER_LEVEL) )
			Log.out(NHB_ITER_LEVEL, "  success with theta idx "+new_index);
		return true;
	}
}

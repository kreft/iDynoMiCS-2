package shape;


import static shape.Dimension.DimName.PHI;
import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;
import static shape.Shape.WhereAmI.INSIDE;
import static shape.Shape.WhereAmI.UNDEFINED;

import java.util.Arrays;

import dataIO.Log;
import dataIO.Log.Tier;
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
					/* NOTE For varying resolution this will not work anymore */
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
		// mathematica: Integrate[r^2 sin p,{p,p1,p2},{t,t1,t2},{r,r1,r2}] 
		double[] loc1 = getVoxelOrigin(coord), loc2 = getVoxelUpperCorner(coord);
		/* R */
		double vol = ExtraMath.cube(loc1[0]) - ExtraMath.cube(loc2[0]);	
		/* PHI */
		vol *= Math.cos(loc1[1]) - Math.cos(loc2[1]);
		/* THETA */
		vol *= loc1[2] - loc2[2];
		return vol / 3;
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
			&& this.setNbhFirstInNewRing( this._currentNeighbor[1] ) )
		{
			this._nbhDimName = R;
			this._nbhDirection = 0;	
		}
		/* 
		 * See if we can take one of the phi-minus-neighbors of the current 
		 * r-shell. 
		 */
		else if ( this.setNbhFirstInNewShell( this._currentCoord[0]) 
					&& this.setNbhFirstInNewRing( this._currentCoord[1] - 1) )
		{
			this._nbhDimName = PHI;
			this._nbhDirection = 0;	
		}
		/* 
		 * See if we can take one of the theta-neighbors in the current r-shell.
		 */
		else if ( this.moveNhbToMinus(THETA))
		{
			this._nbhDimName = THETA;
			this._nbhDirection = 0;	
		}
		else if (this.nhbJumpOverCurrent(THETA))
		{
			this._nbhDimName = THETA;
			this._nbhDirection = 1;	
		}
		/* See if we can take one of the phi-plus-neighbors. */
		else if ( this.setNbhFirstInNewRing( this._currentCoord[1] + 1) )
		{
			this._nbhDimName = PHI;
			this._nbhDirection = 1;	
		}
		
		/* See if we can use the outside r-shell. */
		else if ( this.setNbhFirstInNewShell( this._currentCoord[0] + 1 ) 
					&& this.setNbhFirstInNewRing( this._currentNeighbor[1] ) )
		{
			this._nbhDimName = R;
			this._nbhDirection = 1;		
		}
		/* There are no valid neighbors. */
		else
			this._whereIsNhb = UNDEFINED;
		if ( this.isNbhIteratorValid() )
		{
			transformNhbCyclic();
			return;
		}
	}
	
	@Override
	public int[] nbhIteratorNext()
	{
		this.untransformNhbCyclic();
		/*
		 * In the spherical shape, we start the TODO
		 */
		if ( this._currentNeighbor[0] == this._currentCoord[0] - 1 )
		{
			/*
			 * We're in the r-shell just inside that of the current coordinate.
			 */
			this._nbhDimName = R;
			this._nbhDirection = 0;
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
					this._nbhDimName = PHI;
					this._nbhDirection = 0;
					if ( ! this.moveNhbToMinus(PHI)
						|| ! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
					{
						/*
						 * If this fails, the phi-ring must be invalid, so try
						 * to move to the theta-minus neighbor in the current
						 * phi-ring.
						 * If this fails call this method again.
						 */
						this._nbhDimName = THETA;
						this._nbhDirection = 0;
						if ( ! this.moveNhbToMinus(THETA) )
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
				this._nbhDimName = PHI;
				this._nbhDirection = 0;
				if ( ! this.increaseNbhByOnePolar(THETA) ){
					this._nbhDimName = THETA;
					this._nbhDirection = 0;
					if ( ! this.moveNhbToMinus(THETA) )
						return this.nbhIteratorNext();
				}
			}
			else if ( this._currentNeighbor[1] == this._currentCoord[1] )
			{
				/*
				 * We're in the same phi-ring as the current coordinate.
				 * Try to jump to the theta-plus side of the current
				 * coordinate. If you can't, try switching to the phi-plus
				 * ring.
				 */
				this._nbhDimName = THETA;
				this._nbhDirection = 1;
				if (! this.nhbJumpOverCurrent(THETA) ) {
					this._nbhDimName = PHI;
					this._nbhDirection = 1;
					if ( ! this.setNbhFirstInNewRing(this._currentCoord[1] + 1))
						return this.nbhIteratorNext();
				}
			}
			else 
			{
				/* 
				 * We're in the phi-ring just above that of the current
				 * coordinate. 
				 */
				int rPlus = this._currentCoord[0] + 1;
				this._nbhDimName = PHI;
				this._nbhDirection = 1;
				/* Try increasing theta by one voxel. */
				if ( ! this.increaseNbhByOnePolar(THETA) )
				{
					/* Move out to the next shell or the next rings. */
					this._nbhDimName = R;
					this._nbhDirection = 1;
					if (! this.setNbhFirstInNewShell(rPlus) ||
									! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
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
			this._nbhDimName = R;
			this._nbhDirection = 1;
			if ( ! this.increaseNbhByOnePolar(THETA) )
				if (!this.increaseNbhByOnePolar(PHI) ||
						! this.setNbhFirstInNewRing(this._currentNeighbor[1]) )
				{
					this._whereIsNhb = UNDEFINED;
				}
		}
		this.transformNhbCyclic();
		Log.out(NHB_ITER_LEVEL, "Current coord is " 
				+ Arrays.toString(this._currentCoord) 
				+ " ,dimension name is "+this._nbhDimName
				+", direction is "+this._nbhDirection);
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
		Log.out(NHB_ITER_LEVEL, "  trying to set neighbor in new ring "+
				ringIndex);
		this._currentNeighbor[1] = ringIndex;
		/*
		 * We must be on a shell inside the array or on a defined R boundary.
		 */
		WhereAmI whereIsR = this.whereIsNhb(R);
		if ( whereIsR != INSIDE ){
			if (whereIsR != UNDEFINED){
				Log.out(NHB_ITER_LEVEL, "  success on "+ whereIsR +" boundary");
				return true;
			}
			Log.out(NHB_ITER_LEVEL, "  failure, R on undefined boundary");
			return false;
		}
		/*
		 * First check that the new ring is inside the grid. If we're on a
		 * defined boundary, the theta coordinate is irrelevant.
		 */
		if ( (this._whereIsNhb = this.whereIsNhb(PHI)) != INSIDE ){
			if (this._whereIsNhb != UNDEFINED){
				Log.out(NHB_ITER_LEVEL, "  success on "+ this._whereIsNhb 
						+" boundary");
				return true;
			}
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
//		if (ExtraMath.areEqual(
//				rC.getCumulativeResolution(new_index), theta, 
//				POLAR_ANGLE_EQ_TOL))
//			new_index++;
		
		/* if we stepped onto the current coord, we went too far*/
//		if (this._currentNeighbor[0] == this._currentCoord[0] 
//				&& this._currentNeighbor[1] == this._currentCoord[1]
//				&& new_index == this._currentCoord[2]){
//			Log.out(NHB_ITER_LEVEL, "  failure, stepped onto current coordinate");
//			return false;
//		}
		
		this._currentNeighbor[2] = new_index;
		
		Log.out(NHB_ITER_LEVEL, "  success with theta idx "+new_index);
		return true;
	}
	
	@Override
	public double nhbCurrSharedArea()
	{
		Tier level = Tier.BULK;
		int[] cc = this._currentCoord, nhb = this._currentNeighbor;
		Log.out(level, "  current coord is "+Arrays.toString(cc)
			+", current nhb is "+Arrays.toString(nhb));
		
		boolean pos_direc = this._nbhDirection == 1;
		
		double r1 = getIntegrationMin(0), r2 = getIntegrationMax(0),
				phi1 = getIntegrationMin(1), phi2 = getIntegrationMax(1),
				theta1 = getIntegrationMin(2), theta2 = getIntegrationMax(2);
	
		double area = 1;
		Log.out(level, "    Dimension name is "+this._nbhDimName+", direction is "
														+this._nbhDirection);
		switch (this._nbhDimName){
		case R: /* phi-theta plane */
			/* minimum must be greater than maximum for R in this case */
			assert (r1 > r2); 
			area *= ExtraMath.sq(pos_direc ? r1 : r2);
			area *= theta2 - theta1;
			area *= Math.cos(phi1) - Math.cos(phi2);
			break;
		case PHI: /* r-theta plane */
			/* minimum must be greater than maximum for PHI in this case */
			assert (phi1 > phi2);
			area *= Math.sin(pos_direc ? phi1 : phi2);
			area *= ExtraMath.cube(r1) - ExtraMath.cube(r2);
			area *= theta1 - theta2;
			area /= 3;
			area *= (phi2 / phi1);
			break;
		case THETA: /* phi-r plane */
			area *= ExtraMath.cube(r2) - ExtraMath.cube(r1);
			area *= Math.cos(phi1) - Math.cos(phi2);
			area /= 3;
			break;
		default: throw new IllegalArgumentException("unknown dimension " 
											+ this._nbhDimName + " for sphere");
		}
		Log.out(level, "    r1 is "+r1+", phi1 is "+phi1+ ", theta1 is "+theta1
				+ ", r2 is "+r2+", phi2 is "+phi2+ ", theta2 is "+theta2);
		return area;
	}
}

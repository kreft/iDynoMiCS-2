package shape;

import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import shape.resolution.ResolutionCalculator.ResCalc;
import surface.Ball;
import surface.Point;
import utility.ExtraMath;

public abstract class SphericalShape extends Shape
{
	/**
	 * Collection of resolution calculators for each dimension.
	 */
	protected ResCalc[][][] _resCalc;
	
	public SphericalShape()
	{
		super();
		/*
		 * Set full angular dimensions by default, can be overwritten later.
		 */
		Dimension dim = new Dimension();
		dim.setCyclic();
		dim.setLength(Math.PI);
		this._dimensions.put(DimName.PHI, dim);
		dim = new Dimension();
		dim.setCyclic();
		dim.setLength(2 * Math.PI);
		this._dimensions.put(DimName.THETA, dim);
		/*
		 * Set up the array of resolution calculators.
		 */
		this._resCalc = new ResCalc[3][][];
		/* radius */
		this._resCalc[0] = new ResCalc[1][];
		this._resCalc[0][0] = new ResCalc[1];
		/* phi */
		this._resCalc[1] = new ResCalc[1][];
	}
	
	@Override
	public GridGetter gridGetter()
	{
		// TODO Make getter for SphericalGrid
		return null;
	}
	
	@Override
	public double[] getLocalPosition(double[] location)
	{
		return Vector.toPolar(location);
	}
	
	@Override
	public double[] getGlobalLocation(double[] local)
	{
		return Vector.toCartesian(local);
	}
	
	public void setSurfaces()
	{
		Dimension dim = this.getDimension(DimName.R);
		double[] centre = Vector.zerosDbl(this.getNumberOfDimensions());
		Ball outbound;
		double radius;
		/* Inner radius, if it exists. */
		radius = dim.getExtreme(0);
		if ( radius > 0.0 )
		{
			outbound = new Ball( new Point(centre) , radius);
			outbound.bounding = false;
			this._surfaces.add(outbound);
		}
		/* Outer radius always exists. */
		radius = dim.getExtreme(1);
		outbound = new Ball( new Point(centre) , radius);
		outbound.bounding = true;
		this._surfaces.add(outbound);
	}

	@Override
	public void setDimensionResolution(DimName dName, ResCalc resC)
	{
		int index = this.getDimensionIndex(dName);
		ResCalc radiusC = this._resCalc[0][0][0];
		int nShell;
		double arcLength;
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
			trySetDimRes(DimName.PHI);
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
			double phiLen = this.getDimension(DimName.PHI).getLength();
			for ( int i = 0; i < nShell; i++ )
			{
				/* Find the arc length along the mid-point of this shell. */
				arcLength = radiusC.getPosition(i, 0.5) * phiLen;
				/*
				 * Now use this arc length as the total length of
				 * the resolution calculator in this ring.
				 */
				focalResCalc = (ResCalc) resC.copy();
				focalResCalc.setLength(arcLength);
				this._resCalc[1][0][i] = focalResCalc;
			}
			/* Check if theta is waiting for phi before returning. */
			trySetDimRes(DimName.THETA);
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
			/* Find some useful angle values in advance. */
			double thetaLen = this.getDimension(DimName.THETA).getLength();
			double phiMin = this.getDimension(DimName.PHI).getExtreme(0);
			double phiLen = this.getDimension(DimName.PHI).getLength();
			/* Iterate over the shells. */
			int nRing;
			double radius, phiAngle;
			for ( int shell = 0; shell < nShell; shell++ )
			{
				/* Find the mid-point of this shell. */
				radius = radiusC.getPosition(shell, 0.5);
				/* Prepare the array of ResCalcs for this shell. */
				nRing = phiC[shell].getNVoxel();
				this._resCalc[2][shell] = new ResCalc[nRing];
				/* Iterate over the rings in this shell. */
				for ( int ring = 0; ring < nRing; ring++ )
				{
					/*
					 * Calculate the angle and then the arc length
					 * along the centre-line of this ring.
					 */
					phiAngle = phiMin + (phiLen * (ring+0.5)/nRing);
					arcLength = radius * thetaLen * Math.sin(phiAngle);
					/*
					 * Now use this arc length as the total length of
					 * the resolution calculator in this ring.
					 */
					focalResCalc = (ResCalc) resC.copy();
					focalResCalc.setLength(arcLength);
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

	@Override
	protected void getNVoxel(int[] coords, int[] outNVoxel)
	{
		// TODO
	}
	
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
	
	protected void resetNbhIter()
	{
		
	}
}

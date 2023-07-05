package shape;


import static shape.Dimension.DimName.PHI;
import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import shape.Dimension.DimName;
import shape.ShapeConventions.SingleVoxel;
import shape.iterator.ShapeIterator;
import shape.iterator.SphericalShapeIterator;
import shape.resolution.ResolutionCalculator;
import surface.Ball;
import surface.Point;
import utility.ExtraMath;

public abstract class SphericalShape extends Shape
{
	/**
	 * The value of 1/3. Division is computationally costly, so calculate this
	 * once.
	 */
	private final static double ONE_THIRD = (1.0/3.0);
	
	/**
	 * Collection of resolution calculators for each dimension.
	 */
	protected ResolutionCalculator[][][] _resCalc;
	
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
	public SphericalShape()
	{
		super();
		/*
		 * Set up the array of resolution calculators.
		 */
		this._resCalc = new ResolutionCalculator[3][][];
		/*
		 * Set up the dimensions.
		 */
		Dimension dim;
		/* There is no need for an r-min boundary. 
		 * R must always be significant and non-cyclic */
		dim = new Dimension(true, R); 
		dim.setBoundaryOptional(0);
		this._dimensions.put(R, dim);
		this._resCalc[getDimensionIndex(R)] = new ResolutionCalculator[1][1];
		this._resCalc[getDimensionIndex(R)][0][0] = new SingleVoxel(dim);
		/*
		 * Phi must always be significant and non-cyclic.
		 */
		dim = new Dimension(true, PHI);
		this._dimensions.put(PHI, dim);
		this._resCalc[getDimensionIndex(PHI)] = new ResolutionCalculator[1][1];
		this._resCalc[getDimensionIndex(PHI)][0][0] = new SingleVoxel(dim);
		/*
		 * The phi-dimension is insignificant, unless told otherwise later.
		 */
		dim = new Dimension(false, THETA);
		this._dimensions.put(THETA, dim);
		this._resCalc[getDimensionIndex(THETA)] = new ResolutionCalculator[1][1];
		this._resCalc[getDimensionIndex(THETA)][0][0] = new SingleVoxel(dim);
		
		this._it = this.getNewIterator();
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
	
	@Override
	public ShapeIterator getNewIterator(int strideLength)
	{
		return new SphericalShapeIterator(this, strideLength);
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	@Override
	public double getTotalVolume()
	{
		/*
		 * Volume of a complete sphere: (4/3) pi * r^3
		 * (2/3) * theta gives the angle (pi in complete cylinder).
		 * Need to subtract the inner cylinder from the outer one, hence
		 * rFactor = rMax^3 - rMin^3
		 */
		Dimension r = this.getDimension(R);
		double rMin = r.getExtreme(0);
		double rMax = r.getExtreme(1);
		double thetaLength = this.getDimension(THETA).getLength();
		/*
		 * 
		 */
		double sphereFactor = 2.0 * ONE_THIRD * thetaLength;
		double volMax = sphereFactor * ExtraMath.cube(rMax);
		double volMin = sphereFactor * ExtraMath.cube(rMin);
		/*
		 * The tricky bit if for non-standard values of phi:
		 */
		Dimension phi = this.getDimension(PHI);
		double phiMin = phi.getExtreme(0);
		
		if ( phiMin <= 0.0 )
		{
			volMax -= volConeCap(rMax, thetaLength, phiMin);
			volMin -= volConeCap(rMin, thetaLength, phiMin);
		}
		else
		{
			volMax = volConeCap(rMax, thetaLength, phiMin);
			volMin = volConeCap(rMin, thetaLength, phiMin);
		}
		double phiMax = phi.getExtreme(1);
		if ( phiMax >= 0.0 )
		{
			volMax -= volConeCap(rMax, thetaLength, phiMax);
			volMin -= volConeCap(rMin, thetaLength, phiMax);
		}
		else
		{
			volMax = volConeCap(rMax, thetaLength, phiMax) - volMax;
			volMin = volConeCap(rMin, thetaLength, phiMax) - volMin;
		}
		return volMax - volMin;
	}
	
	@Override
	public double getTotalRealVolume()
	{
		/*
		 * Volume of a complete sphere: (4/3) pi * r^3
		 * (2/3) * theta gives the angle (pi in complete cylinder).
		 * Need to subtract the inner cylinder from the outer one, hence
		 * rFactor = rMax^3 - rMin^3
		 */
		Dimension r = this.getDimension(R);
		double rMin = r.getRealExtreme(0);
		double rMax = r.getRealExtreme(1);
		double thetaLength = this.getDimension(THETA).getRealLength();
		/*
		 * 
		 */
		double sphereFactor = 2.0 * ONE_THIRD * thetaLength;
		double volMax = sphereFactor * ExtraMath.cube(rMax);
		double volMin = sphereFactor * ExtraMath.cube(rMin);
		/*
		 * The tricky bit if for non-standard values of phi:
		 */
		Dimension phi = this.getDimension(PHI);
		double phiMin = phi.getRealExtreme(0);
		
		if ( phiMin <= 0.0 )
		{
			volMax -= volConeCap(rMax, thetaLength, phiMin);
			volMin -= volConeCap(rMin, thetaLength, phiMin);
		}
		else
		{
			volMax = volConeCap(rMax, thetaLength, phiMin);
			volMin = volConeCap(rMin, thetaLength, phiMin);
		}
		double phiMax = phi.getRealExtreme(1);
		if ( phiMax >= 0.0 )
		{
			volMax -= volConeCap(rMax, thetaLength, phiMax);
			volMin -= volConeCap(rMin, thetaLength, phiMax);
		}
		else
		{
			volMax = volConeCap(rMax, thetaLength, phiMax) - volMax;
			volMin = volConeCap(rMin, thetaLength, phiMax) - volMin;
		}
		return volMax - volMin;
	}
	
	public void setTotalVolume( double volume)
	{
		Log.out(Tier.CRITICAL, "Cannot adjust Spherical shape volume" );
	}
	
	private static final double volConeCap(double r, double theta, double phi)
	{
		/* No point doing the calculation if we know it will be zero. */
		if ( r == 0.0 )
			return 0.0;
		/* If this is a hemisphere, the result is easy to calculate. */
		if ( phi == 0.0 )
			return theta * ONE_THIRD * ExtraMath.cube(r);
		/*
		 * h is the distance from the center of the sphere, along the axis, to
		 * where the cone and cap meet.
		 * 
		 * The volume of a cone is (pi/3) * r^2 * h
		 * http://mathworld.wolfram.com/Cone.html
		 * 
		 * The volume of a spherical cap is pi * h^2 * ( r - h/3)
		 * http://mathworld.wolfram.com/SphericalCap.html
		 */
		double sinPhi = Math.sin(phi);
		double h = r * ( 1.0 - sinPhi );
		double cone = ExtraMath.sq(r) * h * ONE_THIRD;
		double cap = ExtraMath.sq(h) * (r - (ONE_THIRD * h) );
		/* If theta is 2 pi (i.e. a full circle), then theta/2 will be pi */
		return 0.5 * theta * ( cap + cone );
	}
	
	@Override
	public void getLocalPositionTo(double[] destination, double[] location)
	{
		Vector.spherifyTo(destination, location);
	}
	
	@Override
	public void getGlobalLocationTo(double[] destination, double[] local)
	{
		Vector.unspherifyTo(destination, local);
	}
	
	/* ***********************************************************************
	 * DIMENSIONS
	 * **********************************************************************/
	
	@Override
	public void setDimensionResolution(DimName dName, ResolutionCalculator resC)
	{
		int index = this.getDimensionIndex(dName);
		ResolutionCalculator radiusC = this._resCalc[0][0][0];
		int nShell;
		ResolutionCalculator focalResCalc;
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
			/* If R only stores a single voxel, it is most 
			 * probably not set already -> check again later */
			if ( radiusC.getNVoxel() == 1 )
			{
				this._rcStorage.put(dName, resC);
				return;
			}
			nShell = radiusC.getNVoxel();
			int rMin = (int)this.getDimension(R).getExtreme(0);
			this._resCalc[1][0] = new ResolutionCalculator[nShell];
			for ( int i = 0; i < nShell; i++ )
			{
				focalResCalc = (ResolutionCalculator) resC.copy();
				double res = ShapeHelper.scaleResolutionForShell(
						rMin+i, resC.getResolution());
				focalResCalc.setResolution(res);
				this._resCalc[1][0][i] = focalResCalc;
			}
			/* Check if theta is waiting for phi before returning. */
			trySetDimRes(THETA);
			return;
		}
		case THETA:
		{
			ResolutionCalculator[] phiC = this._resCalc[1][0];
			/* If R or PHI only store a single voxel, they are most 
			 * probably not set already -> check again later */
			if ( radiusC.getNVoxel() == 1 || phiC[phiC.length - 1].getNVoxel() == 1 )
			{
				this._rcStorage.put(dName, resC);
				return;
			}
			/*
			 * We have what we need for theta, so set up the array of
			 * resolution calculators.
			 */
			nShell = radiusC.getNVoxel();
			
			/* NOTE For varying resolution this has to be adjusted */
			int rMin = (int) (this.getDimension(R).getExtreme(0) 
					/ radiusC.getResolution());
			this._resCalc[2] = new ResolutionCalculator[nShell][];
			/* Iterate over the shells. */
			int nRing;
			for ( int shell = 0; shell < nShell; shell++ )
			{
				/* Prepare the array of ResCalcs for this shell. */
				nRing = phiC[shell].getNVoxel();
				/* NOTE For varying resolution this has to be adjusted */
				int phiMin = (int)(this.getDimension(PHI).getExtreme(0) 
						/ phiC[shell].getResolution());
				this._resCalc[2][shell] = new ResolutionCalculator[nRing];
				/* Iterate over the rings in this shell. */
				for ( int ring = 0; ring < nRing; ring++ )
				{
					focalResCalc = (ResolutionCalculator) resC.copy();
					/* NOTE For varying resolution this will not work anymore */
					double res = ShapeHelper.scaleResolutionForRing(rMin+shell,
							phiMin+ring, phiC[shell].getResolution(),
							resC.getResolution());
					focalResCalc.setResolution(res);
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
	public ResolutionCalculator getResolutionCalculator(int[] coord, int axis)
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
	
	/* ***********************************************************************
	 * LOCATIONS
	 * **********************************************************************/
	
	/* ***********************************************************************
	 * SURFACES
	 * **********************************************************************/
	
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
			outbound.init(_defaultCollision);
			dim.setSurface(outbound, 0);
		}
		/* Outer radius always exists. */
		radius = dim.getExtreme(1);
		outbound = new Ball( new Point(centre) , radius);
		outbound.init(_defaultCollision);
		outbound.bounding = true;
		//this._surfaces.put(outbound, dim.getBoundary(1));
		dim.setSurface(outbound, 1);
	}
	
	/* ***********************************************************************
	 * BOUNDARIES
	 * **********************************************************************/
	
	@Override
	public double getBoundarySurfaceArea(DimName dimN, int extreme)
	{
		switch( dimN )
		{
		case R:
		{
			/* 
			 * Area is a cross-over between an spherical segment and a
			 * spherical lune. See
			 * http://mathworld.wolfram.com/SphericalSegment.html
			 * equation (15) and 
			 * http://mathworld.wolfram.com/SphericalWedge.html
			 * equation (2) for inspiration.
			 */
			/* In a full sphere: thetaLength = 2 * pi */
			double rExt = this.getDimension(R).getExtreme(extreme);
			double thetaLength = this.getDimension(THETA).getLength();
			Dimension phi = this.getDimension(PHI);
			double phiMin = phi.getExtreme(0);
			double phiMax = phi.getExtreme(1);
			/* In a full sphere: h = 2 * rExt */
			double h = rExt * ( Math.sin(phiMax) - Math.sin(phiMin) );
			/* In a full sphere: area = 2 * pi * rExt * 2 * rExt */
			double area = thetaLength * rExt * h;
			return area;
		}
		case THETA:
		{
			/* 
			 * For theta boundaries, it makes no difference which extreme.
			 * The area is essentially half that of a circle with circular
			 * segments above phiMax and below phiMin removed (if appropriate).
			 * See http://mathworld.wolfram.com/CircularSegment.html for
			 * inspiration.
			 */
			// TODO this section could do with some streamlining!
			Dimension r = this.getDimension(R);
			double rMin = r.getExtreme(0);
			double rMax = r.getExtreme(1);
			Dimension phi = this.getDimension(PHI);
			double phiMin = phi.getExtreme(0);
			double phiMax = phi.getExtreme(1);
			double maxSegment, minSegment;
			/* */
			double areaMax = ExtraMath.areaOfACircle(rMax);
			maxSegment = ExtraMath.areaOfACircleSegment(rMax, phiMax);
			minSegment = ExtraMath.areaOfACircleSegment(rMax, phiMin);
			if ( phiMax > 0.0 )
				areaMax -= maxSegment;
			else
				areaMax = maxSegment;
			if ( phiMin < 0.0 )
				areaMax -= minSegment;
			else
				areaMax = minSegment - maxSegment;
			/* */
			double areaMin = ExtraMath.areaOfACircle(rMin);
			minSegment = ExtraMath.areaOfACircleSegment(rMin, phiMax);
			minSegment = ExtraMath.areaOfACircleSegment(rMin, phiMin);
			if ( phiMax > 0.0 )
				areaMin -= maxSegment;
			else
				areaMin = maxSegment;
			if ( phiMin < 0.0 )
				areaMin -= minSegment;
			else
				areaMin = minSegment - maxSegment;
			return areaMax - areaMin;
		}
		case PHI:
		{
			/*
			 * This is the area of a cone
			 * 
			 */
			double phiExt =  this.getDimension(PHI).getExtreme(extreme);
			Dimension r = this.getDimension(R);
			double rMin = r.getExtreme(0);
			double rMax = r.getExtreme(1);
			double thetaLength = this.getDimension(THETA).getLength();
			double thetaScalar = thetaLength / (2.0 * Math.PI);
			
			double maxCone = ExtraMath.lateralAreaOfACone(rMax, phiExt);
			double minCone = ExtraMath.lateralAreaOfACone(rMin, phiExt);
			double area = thetaScalar * (maxCone - minCone);
			return area;
		}
		default:
		{
			// TODO safety
			return Double.NaN;
		}
		}
	}
	
	@Override
	public double getRealSurfaceArea(DimName dimN, int extreme)
	{
		switch( dimN )
		{
		case R:
		{
			/* 
			 * Area is a cross-over between an spherical segment and a
			 * spherical lune. See
			 * http://mathworld.wolfram.com/SphericalSegment.html
			 * equation (15) and 
			 * http://mathworld.wolfram.com/SphericalWedge.html
			 * equation (2) for inspiration.
			 */
			/* In a full sphere: thetaLength = 2 * pi */
			double rExt = this.getDimension(R).getRealExtreme(extreme);
			double thetaLength = this.getDimension(THETA).getRealLength();
			Dimension phi = this.getDimension(PHI);
			double phiMin = phi.getRealExtreme(0);
			double phiMax = phi.getRealExtreme(1);
			/* In a full sphere: h = 2 * rExt */
			double h = rExt * ( Math.sin(phiMax) - Math.sin(phiMin) );
			/* In a full sphere: area = 2 * pi * rExt * 2 * rExt */
			double area = thetaLength * rExt * h;
			return area;
		}
		case THETA:
		{
			/* 
			 * For theta boundaries, it makes no difference which extreme.
			 * The area is essentially half that of a circle with circular
			 * segments above phiMax and below phiMin removed (if appropriate).
			 * See http://mathworld.wolfram.com/CircularSegment.html for
			 * inspiration.
			 */
			// TODO this section could do with some streamlining!
			Dimension r = this.getDimension(R);
			double rMin = r.getRealExtreme(0);
			double rMax = r.getRealExtreme(1);
			Dimension phi = this.getDimension(PHI);
			double phiMin = phi.getRealExtreme(0);
			double phiMax = phi.getRealExtreme(1);
			double maxSegment, minSegment;
			/* */
			double areaMax = ExtraMath.areaOfACircle(rMax);
			maxSegment = ExtraMath.areaOfACircleSegment(rMax, phiMax);
			minSegment = ExtraMath.areaOfACircleSegment(rMax, phiMin);
			if ( phiMax > 0.0 )
				areaMax -= maxSegment;
			else
				areaMax = maxSegment;
			if ( phiMin < 0.0 )
				areaMax -= minSegment;
			else
				areaMax = minSegment - maxSegment;
			/* */
			double areaMin = ExtraMath.areaOfACircle(rMin);
			minSegment = ExtraMath.areaOfACircleSegment(rMin, phiMax);
			minSegment = ExtraMath.areaOfACircleSegment(rMin, phiMin);
			if ( phiMax > 0.0 )
				areaMin -= maxSegment;
			else
				areaMin = maxSegment;
			if ( phiMin < 0.0 )
				areaMin -= minSegment;
			else
				areaMin = minSegment - maxSegment;
			return areaMax - areaMin;
		}
		case PHI:
		{
			/*
			 * This is the area of a cone
			 * 
			 */
			double phiExt =  this.getDimension(PHI).getRealExtreme(extreme);
			Dimension r = this.getDimension(R);
			double rMin = r.getRealExtreme(0);
			double rMax = r.getRealExtreme(1);
			double thetaLength = this.getDimension(THETA).getRealLength();
			double thetaScalar = thetaLength / (2.0 * Math.PI);
			
			double maxCone = ExtraMath.lateralAreaOfACone(rMax, phiExt);
			double minCone = ExtraMath.lateralAreaOfACone(rMin, phiExt);
			double area = thetaScalar * (maxCone - minCone);
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
	public int getTotalNumberOfVoxels()
	{
		int n = 1;
		int dimTheta = this.getDimensionIndex(THETA);
		int dimPhi = this.getDimensionIndex(PHI);
		int nR = this._resCalc[this.getDimensionIndex(R)][0][0].getNVoxel();
		for ( int i = 0; i < nR; i++ )
		{
			int nPhi = this._resCalc[dimPhi][i][0].getNVoxel();
			for ( int j = 0; j < nPhi; j++ )
				n += this._resCalc[dimTheta][i][j].getNVoxel();
		}
		return n;
	}
	
	@Override
	public double getVoxelVolume(double[] origin, double[] upper)
	{
		/* R */
		double vol = ExtraMath.cube(origin[0]) - ExtraMath.cube(upper[0]);	
		/* PHI */
		vol *= Math.cos(origin[1]) - Math.cos(upper[1]);
		/* THETA */
		vol *= origin[2] - upper[2];
		return vol / 3.0;
	}
	
	@Override
	public double nhbCurrSharedArea()
	{
		DimName nhbDimName = this._it.currentNhbDimName();
		/* moving towards positive in the current dim? */
		boolean isNhbAhead = this._it.isCurrentNhbAhead();

		/* Integration minima and maxima, these are the lower and upper 
		 * locations of the intersections between the current voxel and the 
		 * neighbor voxel for each dimension. */
		double r1 = this._it.getIntegrationMin(0),
				r2 = this._it.getIntegrationMax(0),
				phi1 = this._it.getIntegrationMin(1),
				phi2 = this._it.getIntegrationMax(1),
				theta1 = this._it.getIntegrationMin(2),
				theta2 = this._it.getIntegrationMax(2);
		/* 
		 * Compute the area element, depending along which dimension we are 
		 * currently moving. This is 
		 * Integrate[r^2 sin p,{phi,phi1,phi2},{theta,theta1,theta2},{r,r1,r2}] 
		 * with integration length zero for the current dimension.
		 */
		double area = 1.0;
		switch (nhbDimName)
		{
		case R: /* phi-theta plane */
			area *= ExtraMath.sq(isNhbAhead ? r1 : r2);
			area *= theta2 - theta1;
			area *= Math.cos(phi1) - Math.cos(phi2);
			break;
		case PHI: /* r-theta plane */
			area *= Math.sin(isNhbAhead ? phi1 : phi2);
			area *= ExtraMath.cube(r1) - ExtraMath.cube(r2);
			area *= theta1 - theta2;
			area /= 3;
			break;
		case THETA: /* phi-r plane */
			area *= ExtraMath.cube(r2) - ExtraMath.cube(r1);
			area *= Math.cos(phi1) - Math.cos(phi2);
			area /= 3;
			break;
		default: throw new IllegalArgumentException("unknown dimension " 
											+ nhbDimName + " for sphere");
		}
		return area;
	}
	
	/* ***********************************************************************
	 * MULTIGRID CONSTRUCTION
	 * **********************************************************************/
	
	@Override
	public boolean canGenerateCoarserMultigridLayer()
	{
		return false;
	}
	
	@Override
	public Shape generateCoarserMultigridLayer()
	{
		return null;
	}
}

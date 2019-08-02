package shape;

import static shape.Dimension.DimName.R;
import static shape.Dimension.DimName.THETA;
import static shape.Dimension.DimName.Z;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import shape.Dimension.DimName;
import shape.ShapeConventions.SingleVoxel;
import shape.iterator.CylindricalShapeIterator;
import shape.iterator.ShapeIterator;
import shape.resolution.ResolutionCalculator;
import surface.Rod;
import surface.Surface;
import utility.ExtraMath;

/**
 * \brief Abstract shape that is in some way cylindrical.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 * 		Friedrich-Schiller University Jena, Germany 
 */
public abstract class CylindricalShape extends Shape
{
	/**
	 * Collection of resolution calculators for each dimension.
	 */
	protected ResolutionCalculator[][] _resCalc;
	
	/* ***********************************************************************
	 * CONSTRUCTION
	 * **********************************************************************/
	
	public CylindricalShape()
	{
		super();
		this._resCalc = new ResolutionCalculator[3][];
		Dimension dim;
		/* There is no need for an r-min boundary. */
		dim = new Dimension(true, R);
		dim.setBoundaryOptional(0);
		this._dimensions.put(R, dim);
		this._resCalc[this.getDimensionIndex(R)] = new ResolutionCalculator[1];
		this._resCalc[this.getDimensionIndex(R)][0] = new SingleVoxel(dim);
		/*
		 * The theta-dimension must be significant.
		 */
		dim = new Dimension(true, THETA);
		this._dimensions.put(THETA, dim);
		this._resCalc[this.getDimensionIndex(THETA)] = new ResolutionCalculator[1];
		this._resCalc[this.getDimensionIndex(THETA)][0] = new SingleVoxel(dim);
		/*
		 * The z-dimension is insignificant, unless told otherwise later.
		 */
		dim = new Dimension(false, Z);
		this._dimensions.put(Z, dim);
		this._resCalc[this.getDimensionIndex(Z)] = new ResolutionCalculator[1];
		this._resCalc[this.getDimensionIndex(Z)][0] = new SingleVoxel(dim);
		
		this._it = this.getNewIterator();
	}
	
	@Override
	public double[][][] getNewArray(double initialValue)
	{
		if ( this.getNumberOfDimensions() < 2 )
		{
			throw new IllegalArgumentException(
					"A cylindrical array needs at least 2 dimensions");
		}
		/* We need at least one voxel in each dimension. */
		int nR, nTheta, nZ;
		nR = this._resCalc[0][0].getNVoxel();
		nZ = this._resCalc[2][0] == null ? 1 : this._resCalc[2][0].getNVoxel();
		double[][][] a = new double[nR][][];
		for ( int i = 0; i < nR; i++ )
		{
			nTheta = this._resCalc[1][i].getNVoxel();
			a[i] = Matrix.matrix(nTheta, nZ, initialValue);
		}
		return a;
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/

	@Override
	public double getTotalVolume()
	{
		/*
		 * Volume of a complete cylinder: pi * r^2 * z
		 * Half theta gives the angle (pi in complete cylinder).
		 * Need to subtract the inner cylinder from the outer one, hence
		 * rFactor = rMax^2 - rMin^2
		 */
		Dimension r = this.getDimension(R);
		double rMin = r.getExtreme(0);
		double rMax = r.getExtreme(1);
		double rFactor = ExtraMath.sq(rMax) - ExtraMath.sq(rMin);
		double thetaLength = this.getDimension(THETA).getLength();
		double zLength = this.getDimension(Z).getLength();
		return 0.5 * thetaLength * rFactor * zLength;
	}
	
	@Override
	public double getTotalRealVolume()
	{
		/*
		 * Volume of a complete cylinder: pi * r^2 * z
		 * Half theta gives the angle (pi in complete cylinder).
		 * Need to subtract the inner cylinder from the outer one, hence
		 * rFactor = rMax^2 - rMin^2
		 */
		Dimension r = this.getDimension(R);
		double rMin = r.getRealExtreme(0);
		double rMax = r.getRealExtreme(1);
		double rFactor = ExtraMath.sq(rMax) - ExtraMath.sq(rMin);
		double thetaLength = this.getDimension(THETA).getRealLength();
		double zLength = this.getDimension(Z).getRealLength();
		return 0.5 * thetaLength * rFactor * zLength;
	}
	
	public void setTotalVolume( double volume)
	{
		Log.out(Tier.CRITICAL, "Cannot adjust Cylindrical shape volume" );
	}

	@Override
	public void getLocalPositionTo(double[] destination, double[] location)
	{
		Vector.cylindrifyTo(destination, location);
	}
	
	@Override
	public void getGlobalLocationTo(double[] destination, double[] local)
	{
		Vector.uncylindrifyTo(destination, local);
	}
	
	/* ***********************************************************************
	 * DIMENSIONS
	 * **********************************************************************/
	
	@Override
	public void setDimensionResolution(DimName dName, ResolutionCalculator resC)
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

			ResolutionCalculator radiusC = this._resCalc[0][0];
			/* If R only stores a single voxel, it is most 
			 * probably not set already -> check again later */
			if ( radiusC.getNVoxel() == 1 )
			{
				this._rcStorage.put(dName, resC);
				return;
			}
			else
			{
				int nShell = radiusC.getNVoxel();
				/* NOTE For varying resolution this has to be adjusted */
				int rMin = (int)(this.getDimension(R).getExtreme(0)
						/ radiusC.getResolution());
				this._resCalc[index] = new ResolutionCalculator[nShell];
				ResolutionCalculator shellResCalc;
				for ( int i = 0; i < nShell; i++ )
				{
					shellResCalc = (ResolutionCalculator) resC.copy();
					/* since we do not allow initialization with varying 
					 * resolutions, resC.getResolution(x) should all be the 
					 * same at this point. 
					 */
					double res = ShapeHelper.scaleResolutionForShell(
							rMin + i, resC.getResolution());
					shellResCalc.setResolution(res);
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
	public ResolutionCalculator getResolutionCalculator(int[] coord, int dim)
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
			rod.init(_defaultCollision);
			radiusDim.setSurface(rod, 0);
		}
		/* We always use the outer radius. */
		radius = radiusDim.getExtreme(1);
		Surface rod = new Rod(pointA, pointB, radius);
		rod.init(_defaultCollision);
		radiusDim.setSurface(rod, 1);
		/*
		 * If theta is not cyclic, we need to add two planes.
		 */
		Dimension thetaDim = this.getDimension(THETA);
		if ( ! thetaDim.isCyclic() )
		{
			// FIXME replace with non-infinite plane starting from orient
			// REMEMBER Surfaces are always expressed in Cartesian coordinates
			//	thetaDim.setSurface(THETA, adjust normal);
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
	
	@Override
	public double getRealSurfaceArea(DimName dimN, int extreme)
	{
		switch( dimN )
		{
		case R:
		{
			/* 
			 * Area is a curved rectangle (cylinder if theta length is 2 pi).
			 */
			double rExt = this.getDimension(R).getRealExtreme(extreme);
			double thetaLength = this.getDimension(THETA).getRealLength();
			double arcLength = rExt * thetaLength;
			double zLength = this.getDimension(Z).getRealLength();
			double area = arcLength * zLength;
			return area;
		}
		case THETA:
		{
			/* 
			 * For theta boundaries, it makes no difference which extreme.
			 * Area is simply a rectangle of area (r * z).
			 */
			double rLength = this.getDimension(R).getRealLength();
			double zLength = this.getDimension(Z).getRealLength();
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
			double thetaLength = this.getDimension(THETA).getRealLength();
			Dimension r = this.getDimension(R);
			double rMin = r.getRealExtreme(0);
			double rMax = r.getRealExtreme(1);
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
	public int getTotalNumberOfVoxels()
	{
		int n = 1;
		int dimTheta = this.getDimensionIndex(THETA);
		int nR = this._resCalc[this.getDimensionIndex(R)][0].getNVoxel();
		for ( int i = 0; i < nR; i++ )
			n += this._resCalc[dimTheta][i].getNVoxel();
		return n * this._resCalc[this.getDimensionIndex(Z)][0].getNVoxel();
	}
	
	@Override
	public double getVoxelVolume(double[] origin, double[] upper){
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

	@Override
	public ShapeIterator getNewIterator(int strideLength)
	{
		return new CylindricalShapeIterator(this, strideLength);
	}
	
	/* ***********************************************************************
	 * NEIGHBOR ITERATOR
	 * **********************************************************************/
	
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
				theta1 = this._it.getIntegrationMin(1),
				theta2 = this._it.getIntegrationMax(1),
				z1 = this._it.getIntegrationMin(2),
				z2 = this._it.getIntegrationMax(2);
		/* 
		 * Compute the area element, depending along which dimension we are 
		 * currently moving. This is 
		 * Integrate[r,{z,z1,z2},{theta,theta1,theta2},{r,r1,r2}]
		 * with integration length zero for the current dimension.
		 */
		double area = 1.0;
		switch (nhbDimName)
		{
		case R: /* theta-z plane */
			area *= isNhbAhead ? r1 : r2;
			area *= theta2 - theta1;
			area *= z2 - z1;
			break;
		case THETA: /* r-z plane */
			area *= ExtraMath.sq(r2) - ExtraMath.sq(r1);
			area *= z2 - z1;
			area /= 2;
			break;
		case Z: /* r-theta plane */
			area *= ExtraMath.sq(r2) - ExtraMath.sq(r1);
			area *= theta2 - theta1;
			area /= 2;
			break;
		default: throw new IllegalArgumentException("unknown dimension " 
											+ nhbDimName + " for cylinder");
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

/**
 * 
 */
package shape;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.ObjectRef;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import grid.CartesianGrid;
import grid.CylindricalGrid;
import grid.DummyGrid;
import grid.SpatialGrid.GridGetter;
import grid.resolution.ResolutionCalculator.ResCalc;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import surface.Point;
import surface.Rod;
import utility.ExtraMath;
import surface.Ball;

/**
 * \brief Collection of instanciable {@code Shape} classes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 */
public final class ShapeLibrary
{
	
	/*************************************************************************
	 * DUMMY SHAPE (for chemostats, etc)
	 ************************************************************************/
	
	/**
	 * \brief A zero-dimensional shape, which only has a volume.
	 * 
	 * <p>Used by {@code Compartment}s without spatial structure, e.g. a
	 * chemostat.</p>
	 */
	public static class Dimensionless extends Shape
	{
		protected double _volume = 0.0;
		
		public Dimensionless()
		{
			super();
		}
		
		@Override
		public void init(Element xmlElem)
		{
			// TODO read in as a Double
			String str = XmlHandler.attributeFromUniqueNode(
										xmlElem, "volume", ObjectRef.STR);
			this._volume = Double.parseDouble(str);
		}
		
		/**
		 * \brief Set this dimensionless shape's volume.
		 * 
		 * @param volume New volume to use.
		 */
		public void setVolume(double volume)
		{
			this._volume = volume;
		}
		
		@Override
		public GridGetter gridGetter()
		{
			Log.out(Tier.DEBUG, "Dimensionless shape volume is "+this._volume);
			return DummyGrid.dimensionlessGetter(this._volume);
		}
		
		@Override
		public double[] getLocalPosition(double[] location)
		{
			return location;
		}
		
		@Override
		public double[] getGlobalLocation(double[] local)
		{
			return local;
		}
		
		public void setSurfs()
		{
			/* Do nothing! */
		}
		
		public boolean isReadyForLaunch()
		{
			if ( ! super.isReadyForLaunch() )
				return false;
			if ( this._volume <= 0.0 )
			{
				Log.out(Tier.CRITICAL,
							"Dimensionless shape must have positive volume!");
				return false;
			}
			return true;
		}

		@Override
		public void setDimensionResolution(DimName dName, ResCalc resC)
		{
			/* Do nothing! */
		}

		@Override
		protected ResCalc getResolutionCalculator(int[] coord, int axis)
		{
			return null;
		}
		
		protected void getNVoxel(int[] coords, int[] outNVoxel)
		{
			/* Dimensionless shapes have no voxels. */
			Vector.reset(outNVoxel);
		}
		
		@Override
		public double getVoxelVolume(int[] coord)
		{
			return this._volume;
		}
	}
	
	/*************************************************************************
	 * SHAPES WITH STRAIGHT EDGES
	 ************************************************************************/
	
	/**
	 * \brief One-dimensional, straight {@code Shape} class.
	 */
	public static class Line extends Shape
	{
		/**
		 * Array of resolution calculators used by all linear {@code Shape}s.
		 */
		protected ResCalc[] _resCalc;
		
		public Line()
		{
			super();
			this._dimensions.put(DimName.X, new Dimension());
			this._resCalc = new ResCalc[3];
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make 1D, 2D, and 3D getters?
			return CartesianGrid.standardGetter();
		}
		
		@Override
		public double[] getLocalPosition(double[] location)
		{
			return location;
		}
		
		@Override
		public double[] getGlobalLocation(double[] local)
		{
			return local;
		}
		
		@Override
		public void setSurfs()
		{
			this.setPlanarSurfaces(DimName.X);
		}

		@Override
		public void setDimensionResolution(DimName dName, ResCalc resC)
		{
			int index = this.getDimensionIndex(dName);
			this._resCalc[index] = resC;
		}

		@Override
		protected ResCalc getResolutionCalculator(int[] coord, int axis)
		{
			/* Coordinate is irrelevant here. */
			return this._resCalc[axis];
		}
		
		protected void getNVoxel(int[] coords, int[] outNVoxel)
		{
			for ( int dim = 0; dim < this.getNumberOfDimensions(); dim++ )
				outNVoxel[dim] = this._resCalc[dim].getNVoxel();
		}
		
		@Override
		public double getVoxelVolume(int[] coord)
		{
			double out = 1.0;
			ResCalc rC;
			// TODO handle y, z dimensions
			for ( int dim = 0; dim < 3; dim++ )
			{
				rC = this.getResolutionCalculator(coord, dim);
				out *= rC.getResolution(coord[dim]);
			}
			return out;
		}
	}
	
	/**
	 * \brief Two-dimensional, straight {@code Shape} class.
	 */
	public static class Rectangle extends Line
	{
		public Rectangle()
		{
			super();
			this._dimensions.put(DimName.Y, new Dimension());
		}
		
		@Override
		public void setSurfs()
		{
			/* Do the X dimension. */
			super.setSurfs();
			/* Now the Y dimension. */
			this.setPlanarSurfaces(DimName.Y);
		}
	}
	
	/**
	 * \brief Three-dimensional, straight {@code Shape} class.
	 */
	public static class Cuboid extends Rectangle
	{
		public Cuboid()
		{
			super();
			this._dimensions.put(DimName.Z, new Dimension());
		}
		
		public void setSurfs()
		{
			/* Do the X and Y dimensions. */
			super.setSurfs();
			/* Now the Z dimension. */
			this.setPlanarSurfaces(DimName.Z);
		}
	}
	
	/*************************************************************************
	 * SHAPES WITH ROUND EDGES
	 ************************************************************************/
	
	/**
	 * \brief Two-dimensional, round {@code Shape} class with an assumed linear
	 * thickness.
	 */
	public static class Circle extends Shape.Polar
	{
		protected ResCalc[][] _resCalc;
		
		public Circle()
		{
			super();
			/*
			 * Set to a full circle by default, let it be overwritten later.
			 */
			Dimension dim = new Dimension();
			dim.setCyclic();
			dim.setLength(2 * Math.PI);
			this._dimensions.put(DimName.THETA, dim);
			this._resCalc = new ResCalc[3][];
		}
		
		@Override
		public GridGetter gridGetter()
		{
			return CylindricalGrid.standardGetter();
		}
		
		public double[] getLocalPosition(double[] location)
		{
			return Vector.toPolar(location);
		}
		
		public double[] getGlobalLocation(double[] local)
		{
			return Vector.toCartesian(local);
		}
		
		public void setSurfs()
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
			if ( this._dimensions.containsKey(DimName.Z) )
			{
				Dimension zDim = this.getDimension(DimName.Z);
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
	}
	
	/**
	 * \brief Three-dimensional, round {@code Shape} class with a linear third
	 * dimension.
	 */
	public static class Cylinder extends Circle
	{
		public Cylinder()
		{
			super();
			this._dimensions.put(DimName.Z, new Dimension());
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
		
		public void setSurfs()
		{
			/* Do the R and THETA dimensions. */
			super.setSurfs();
			/* Now the Z dimension. */
			this.setPlanarSurfaces(DimName.Z);
		}
	}
	
	/**
	 * \brief Three-dimensional, round {@code Shape} class with both second and
	 * third dimensions angular.
	 */
	public static class Sphere extends Shape.Polar
	{
		/**
		 * Collection of resolution calculators for each dimension.
		 */
		protected ResCalc[][][] _resCalc;
		
		public Sphere()
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
		
		public void setSurfs()
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
	}
}

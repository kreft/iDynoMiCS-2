/**
 * 
 */
package shape;

import grid.CartesianGrid;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class ShapeLibrary
{
	/*************************************************************************
	 * DUMMY SHAPE (for chemostats, etc)
	 ************************************************************************/
	
	public static class Dimensionless extends Shape
	{
		public Dimensionless()
		{
			super();
			this._nDim = 0;
		}
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make DummyGrid?
			return CartesianGrid.dimensionlessGetter();
		}
		
		public boolean isInside(double[] location)
		{
			// TODO check
			return true;
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			return null;
		}
	}
	
	/*************************************************************************
	 * SHAPES WITH STRAIGHT EDGES
	 ************************************************************************/
	
	public static class Line extends Shape
	{
		protected final static int X_DIM = 0;
		
		public Line()
		{
			super();
			this._nDim = 1;
			this._requiredBoundarySides.add(BoundarySide.XMIN);
			this._requiredBoundarySides.add(BoundarySide.XMAX);
			this._sideDict.put(BoundarySide.XMIN, new int[]{X_DIM, 0});
			this._sideDict.put(BoundarySide.XMAX, new int[]{X_DIM, 1});
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make 1D, 2D, and 3D getters?
			return CartesianGrid.standardGetter();
		}
		
		public boolean isInside(double[] location)
		{
			return isInsideLocal(location);
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			if ( aSide == BoundarySide.XMIN )
				return this.subtractSideLength(loc, X_DIM);
			if ( aSide == BoundarySide.XMAX )
				return this.addSideLength(loc, X_DIM);
			return null;
		}
	}
	
	public static class Rectangle extends Line
	{
		protected final static int Y_DIM = 1;
		
		public Rectangle()
		{
			super();
			this._nDim = 2;
			this._requiredBoundarySides.add(BoundarySide.YMIN);
			this._requiredBoundarySides.add(BoundarySide.YMAX);
			this._sideDict.put(BoundarySide.YMIN, new int[]{Y_DIM, 0});
			this._sideDict.put(BoundarySide.YMAX, new int[]{Y_DIM, 1});
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			if ( aSide == BoundarySide.YMIN )
				return this.subtractSideLength(loc, Y_DIM);
			if ( aSide == BoundarySide.YMAX )
				return this.addSideLength(loc, Y_DIM);
			return null;
		}
	}
	
	public static class Cuboid extends Rectangle
	{
		protected final static int Z_DIM = 2;
		
		public Cuboid()
		{
			super();
			this._nDim = 3;
			this._requiredBoundarySides.add(BoundarySide.ZMIN);
			this._requiredBoundarySides.add(BoundarySide.ZMAX);
			this._sideDict.put(BoundarySide.ZMIN, new int[]{Z_DIM, 0});
			this._sideDict.put(BoundarySide.ZMAX, new int[]{Z_DIM, 1});
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			if ( aSide == BoundarySide.ZMIN )
				return this.subtractSideLength(loc, Z_DIM);
			if ( aSide == BoundarySide.ZMAX )
				return this.addSideLength(loc, Z_DIM);
			return null;
		}
	}
	
	/*************************************************************************
	 * SHAPES WITH ROUND EDGES
	 ************************************************************************/
	
	public static abstract class Polar extends Shape
	{
		protected final static int R_DIM = 0;
		
		public Polar()
		{
			super();
			this._requiredBoundarySides.add(BoundarySide.RMAX);
			this._sideDict.put(BoundarySide.RMIN, new int[]{R_DIM, 0});
			this._sideDict.put(BoundarySide.RMAX, new int[]{R_DIM, 1});
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			if ( aSide == BoundarySide.RMIN )
				return this.subtractSideLength(loc, R_DIM);
			if ( aSide == BoundarySide.RMAX )
				return this.addSideLength(loc, R_DIM);
			return null;
		}
	}
	
	public static class Circle extends Polar
	{
		protected final static int THETA_DIM = 1;
		
		public Circle()
		{
			super();
			this._nDim = 2;
			this._sideDict.put(BoundarySide.THETAMIN, new int[]{THETA_DIM, 0});
			this._sideDict.put(BoundarySide.THETAMAX, new int[]{THETA_DIM, 1});
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make (2D?) getter for CylindricalGrid
			return null;
		}
		
		public boolean isInside(double[] location)
		{
			double[] local = Vector.toCylindrical(location);
			return isInsideLocal(local);
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			if ( aSide == BoundarySide.THETAMIN )
				return this.subtractSideLength(loc, THETA_DIM);
			if ( aSide == BoundarySide.THETAMAX )
				return this.addSideLength(loc, THETA_DIM);
			return null;
		}
	}
	
	public static class Cylinder extends Circle
	{
		protected final static int Z_DIM = 2;
		
		public Cylinder()
		{
			super();
			this._nDim = 3;
			this._requiredBoundarySides.add(BoundarySide.ZMIN);
			this._requiredBoundarySides.add(BoundarySide.ZMAX);
			this._sideDict.put(BoundarySide.ZMIN, new int[]{Z_DIM, 0});
			this._sideDict.put(BoundarySide.ZMAX, new int[]{Z_DIM, 1});
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			if ( aSide == BoundarySide.ZMIN )
				return this.subtractSideLength(loc, Z_DIM);
			if ( aSide == BoundarySide.ZMAX )
				return this.addSideLength(loc, Z_DIM);
			return null;
		}
	}
	
	public static class Sphere extends Polar
	{
		protected final static int PHI_DIM = 1;
		protected final static int THETA_DIM = 2;
		
		public Sphere()
		{
			super();
			this._nDim = 3;
			this._sideDict.put(BoundarySide.PHIMIN, new int[]{PHI_DIM, 0});
			this._sideDict.put(BoundarySide.PHIMAX, new int[]{PHI_DIM, 1});
			this._sideDict.put(BoundarySide.THETAMIN, new int[]{THETA_DIM, 0});
			this._sideDict.put(BoundarySide.THETAMAX, new int[]{THETA_DIM, 1});
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make getter for SphericalGrid
			return null;
		}
		
		public boolean isInside(double[] location)
		{
			double[] local = Vector.toPolar(location);
			return isInsideLocal(local);
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			/*
			 * Note that spherical coordinates are (r, phi, theta).
			 */
			if ( aSide == BoundarySide.PHIMIN )
				return this.subtractSideLength(loc, PHI_DIM);
			if ( aSide == BoundarySide.PHIMAX )
				return this.addSideLength(loc, PHI_DIM);
			if ( aSide == BoundarySide.THETAMIN )
				return this.subtractSideLength(loc, THETA_DIM);
			if ( aSide == BoundarySide.THETAMAX )
				return this.addSideLength(loc, THETA_DIM);
			return null;
		}
	}
}

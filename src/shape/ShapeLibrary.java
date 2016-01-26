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
		public Line()
		{
			super();
			this._nDim = 1;
			this._requiredBoundarySides.add(BoundarySide.XMIN);
			this._requiredBoundarySides.add(BoundarySide.XMAX);
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
				return this.subtractSideLength(loc, 0);
			if ( aSide == BoundarySide.XMAX )
				return this.addSideLength(loc, 0);
			return null;
		}
	}
	
	public static class Rectangle extends Line
	{
		public Rectangle()
		{
			super();
			this._nDim = 2;
			this._requiredBoundarySides.add(BoundarySide.YMIN);
			this._requiredBoundarySides.add(BoundarySide.YMAX);
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			if ( aSide == BoundarySide.YMIN )
				return this.subtractSideLength(loc, 1);
			if ( aSide == BoundarySide.YMAX )
				return this.addSideLength(loc, 1);
			return null;
		}
	}
	
	public static class Cuboid extends Rectangle
	{
		public Cuboid()
		{
			super();
			this._nDim = 3;
			this._requiredBoundarySides.add(BoundarySide.ZMIN);
			this._requiredBoundarySides.add(BoundarySide.ZMAX);
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			if ( aSide == BoundarySide.ZMIN )
				return this.subtractSideLength(loc, 2);
			if ( aSide == BoundarySide.ZMAX )
				return this.addSideLength(loc, 2);
			return null;
		}
	}
	
	/*************************************************************************
	 * SHAPES WITH ROUND EDGES
	 ************************************************************************/
	
	public static abstract class Polar extends Shape
	{
		public Polar()
		{
			super();
			this._requiredBoundarySides.add(BoundarySide.RMAX);
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			if ( aSide == BoundarySide.RMIN )
				return this.subtractSideLength(loc, 0);
			if ( aSide == BoundarySide.RMAX )
				return this.addSideLength(loc, 0);
			return null;
		}
	}
	
	public static class Circle extends Polar
	{
		public Circle()
		{
			super();
			this._nDim = 2;
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
				return this.subtractSideLength(loc, 1);
			if ( aSide == BoundarySide.THETAMAX )
				return this.addSideLength(loc, 1);
			return null;
		}
	}
	
	public static class Cylinder extends Circle
	{
		public Cylinder()
		{
			super();
			this._nDim = 3;
			this._requiredBoundarySides.add(BoundarySide.ZMIN);
			this._requiredBoundarySides.add(BoundarySide.ZMAX);
		}
		
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			if ( aSide == BoundarySide.ZMIN )
				return this.subtractSideLength(loc, 2);
			if ( aSide == BoundarySide.ZMAX )
				return this.addSideLength(loc, 2);
			return null;
		}
	}
	
	public static class Sphere extends Polar
	{
		public Sphere()
		{
			super();
			this._nDim = 3;
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
				return this.subtractSideLength(loc, 1);
			if ( aSide == BoundarySide.PHIMAX )
				return this.addSideLength(loc, 1);
			if ( aSide == BoundarySide.THETAMIN )
				return this.subtractSideLength(loc, 2);
			if ( aSide == BoundarySide.THETAMAX )
				return this.addSideLength(loc, 2);
			return null;
		}
	}
}

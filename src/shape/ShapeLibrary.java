/**
 * 
 */
package shape;

import grid.CartesianGrid;
import grid.SpatialGrid.GridGetter;

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
	}
}

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
		}
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make DummyGrid?
			return CartesianGrid.dimensionlessGetter();
		}
		
		public double[] getLocalPosition(double[] location)
		{
			return location;
		}
		
		public double[] getGlobalLocation(double[] local)
		{
			return local;
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
			this._dimensions.put(Shape.DimName.X, new Dimension());
			
			this._requiredBoundarySides.add(BoundarySide.XMIN);
			this._requiredBoundarySides.add(BoundarySide.XMAX);
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make 1D, 2D, and 3D getters?
			return CartesianGrid.standardGetter();
		}
		
		public double[] getLocalPosition(double[] location)
		{
			return location;
		}
		
		public double[] getGlobalLocation(double[] local)
		{
			return local;
		}
	}
	
	public static class Rectangle extends Line
	{
		public Rectangle()
		{
			super();
			this._dimensions.put(Shape.DimName.Y, new Dimension());
			this._requiredBoundarySides.add(BoundarySide.YMIN);
			this._requiredBoundarySides.add(BoundarySide.YMAX);
		}
	}
	
	public static class Cuboid extends Rectangle
	{
		public Cuboid()
		{
			super();
			this._dimensions.put(Shape.DimName.Z, new Dimension());
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
			/* There is no need for an r-min boundary. */
			Dimension dim = new Dimension();
			dim.setBoundaryOptional(true);
			this._dimensions.put(Shape.DimName.R, dim);
			this._requiredBoundarySides.add(BoundarySide.RMAX);
		}
	}
	
	public static class Circle extends Polar
	{
		public Circle()
		{
			super();
			/*
			 * Set to a full circle by default, let it be overwritten later.
			 */
			Dimension dim = new Dimension();
			dim.setCyclic();
			dim.setLength(2 * Math.PI);
			this._dimensions.put(Shape.DimName.THETA, dim);
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make (2D?) getter for CylindricalGrid
			return null;
		}
		
		public double[] getLocalPosition(double[] location)
		{
			return Vector.toPolar(location);
		}
		
		public double[] getGlobalLocation(double[] local)
		{
			return Vector.toCartesian(local);
		}
	}
	
	public static class Cylinder extends Circle
	{
		public Cylinder()
		{
			super();
			this._dimensions.put(Shape.DimName.Z, new Dimension());
			this._requiredBoundarySides.add(BoundarySide.ZMIN);
			this._requiredBoundarySides.add(BoundarySide.ZMAX);
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
	}
	
	public static class Sphere extends Polar
	{
		public Sphere()
		{
			super();
			/*
			 * Set full angular dimensions by default, can be overwritten later.
			 */
			Dimension dim = new Dimension();
			dim.setCyclic();
			dim.setLength(Math.PI);
			this._dimensions.put(Shape.DimName.PHI, dim);
			dim = new Dimension();
			dim.setCyclic();
			dim.setLength(2 * Math.PI);
			this._dimensions.put(Shape.DimName.THETA, dim);
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make getter for SphericalGrid
			return null;
		}
		
		public double[] getLocalPosition(double[] location)
		{
			return Vector.toPolar(location);
		}
		
		public double[] getGlobalLocation(double[] local)
		{
			return Vector.toCartesian(local);
		}
	}
}

/**
 * 
 */
package shape;

import grid.CartesianGrid;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.ShapeDimension.DimName;

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
			this._dimensions.put(DimName.X, null);
			
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
	}
	
	public static class Rectangle extends Line
	{
		protected final static int Y_DIM = 1;
		
		public Rectangle()
		{
			super();
			this._dimensions.put(DimName.Y, null);
			this._requiredBoundarySides.add(BoundarySide.YMIN);
			this._requiredBoundarySides.add(BoundarySide.YMAX);
		}
	}
	
	public static class Cuboid extends Rectangle
	{
		protected final static int Z_DIM = 2;
		
		public Cuboid()
		{
			super();
			this._dimensions.put(DimName.Z, null);
			this._requiredBoundarySides.add(BoundarySide.ZMIN);
			this._requiredBoundarySides.add(BoundarySide.ZMAX);
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
			this._dimensions.put(DimName.R, null);
			this._requiredBoundarySides.add(BoundarySide.RMAX);
		}
	}
	
	public static class Circle extends Polar
	{
		protected final static int THETA_DIM = 1;
		
		public Circle()
		{
			super();
			this._dimensions.put(DimName.THETA, null);
		}
		
		@Override
		public GridGetter gridGetter()
		{
			// TODO Make (2D?) getter for CylindricalGrid
			return null;
		}
		
		public double[] getLocalPosition(double[] location)
		{
			return Vector.toCylindrical(location);
		}
	}
	
	public static class Cylinder extends Circle
	{
		protected final static int Z_DIM = 2;
		
		public Cylinder()
		{
			super();
			this._dimensions.put(DimName.Z, null);
			this._requiredBoundarySides.add(BoundarySide.ZMIN);
			this._requiredBoundarySides.add(BoundarySide.ZMAX);
		}
	}
	
	public static class Sphere extends Polar
	{
		protected final static int PHI_DIM = 1;
		protected final static int THETA_DIM = 2;
		
		public Sphere()
		{
			super();
			this._dimensions.put(DimName.PHI, null);
			this._dimensions.put(DimName.THETA, null);
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
	}
}

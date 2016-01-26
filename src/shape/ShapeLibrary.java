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
		
		@SuppressWarnings("incomplete-switch")
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = null;
			switch ( aSide )
			{
			case XMIN:
				out = Vector.copy(loc);
				out[0] -= this._lengths[0];
				break;
			case XMAX:
				out = Vector.copy(loc);
				out[0] += this._lengths[0];
				break;
			}
			return out;
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
		
		@SuppressWarnings("incomplete-switch")
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			switch ( aSide )
			{
			case YMIN:
				out = Vector.copy(loc);
				out[1] -= this._lengths[1];
				break;
			case YMAX:
				out = Vector.copy(loc);
				out[1] += this._lengths[1];
				break;
			}
			return out;
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
		
		@SuppressWarnings("incomplete-switch")
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			switch ( aSide )
			{
			case ZMIN:
				out = Vector.copy(loc);
				out[2] -= this._lengths[2];
				break;
			case ZMAX:
				out = Vector.copy(loc);
				out[2] += this._lengths[2];
				break;
			}
			return out;
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
		
		@SuppressWarnings("incomplete-switch")
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = null;
			switch ( aSide )
			{
			case RMIN:
				out = Vector.copy(loc);
				out[0] -= this._lengths[0];
				break;
			case RMAX:
				out = Vector.copy(loc);
				out[0] += this._lengths[0];
				break;
			}
			return out;
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
		
		@SuppressWarnings("incomplete-switch")
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			switch ( aSide )
			{
			case THETAMIN:
				out = Vector.copy(loc);
				out[1] -= this._lengths[1];
				break;
			case THETAMAX:
				out = Vector.copy(loc);
				out[1] += this._lengths[1];
				break;
			}
			return out;
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
		
		@SuppressWarnings("incomplete-switch")
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			switch ( aSide )
			{
			case ZMIN:
				out = Vector.copy(loc);
				out[2] -= this._lengths[2];
				break;
			case ZMAX:
				out = Vector.copy(loc);
				out[2] += this._lengths[2];
				break;
			}
			return out;
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
		
		@SuppressWarnings("incomplete-switch")
		public double[] getCyclicPoint(BoundarySide aSide, double[] loc)
		{
			double[] out = super.getCyclicPoint(aSide, loc);
			if ( out != null )
				return out;
			switch ( aSide )
			{
			case THETAMIN:
				out = Vector.copy(loc);
				out[1] -= this._lengths[1];
				break;
			case THETAMAX:
				out = Vector.copy(loc);
				out[1] += this._lengths[1];
				break;
			case PHIMIN:
				out = Vector.copy(loc);
				out[2] -= this._lengths[2];
				break;
			case PHIMAX:
				out = Vector.copy(loc);
				out[2] += this._lengths[2];
				break;
			}
			return out;
		}
	}
}

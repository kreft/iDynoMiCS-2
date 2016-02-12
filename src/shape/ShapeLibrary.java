/**
 * 
 */
package shape;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.tier;
import grid.CartesianGrid;
import grid.CylindricalGrid;
import grid.SpatialGrid.GridGetter;
import linearAlgebra.Vector;
import shape.ShapeConventions.DimName;
import surface.Point;
import surface.Ball;

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
		protected double _volume = 0.0;
		
		public Dimensionless()
		{
			super();
		}
		
		@Override
		public void init(Node xmlNode)
		{
			Element elem = (Element) xmlNode;
			String str = XmlHandler.loadUniqueAtribute(elem,"volume","string");
			this._volume = Double.parseDouble(str);
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
		
		public void setSurfs()
		{
			/*
			 * Do nothing!
			 */
		}
		
		public boolean isReadyForLaunch()
		{
			if ( ! super.isReadyForLaunch() )
				return false;
			if ( this._volume <= 0.0 )
			{
				Log.out(tier.CRITICAL,
							"Dimensionless shape must have positive volume!");
				return false;
			}
			return true;
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
			this._dimensions.put(DimName.X, new Dimension());
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
		
		public void setSurfs()
		{
			this.setPlanarSurfaces(DimName.X);
		}
	}
	
	public static class Rectangle extends Line
	{
		public Rectangle()
		{
			super();
			this._dimensions.put(DimName.Y, new Dimension());
		}
		
		public void setSurfs()
		{
			/* Do the X dimension. */
			super.setSurfs();
			/* Now the Y dimension. */
			this.setPlanarSurfaces(DimName.Y);
		}
	}
	
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
	
	public static abstract class Polar extends Shape
	{
		public Polar()
		{
			super();
			/* There is no need for an r-min boundary. */
			Dimension dim = new Dimension();
			dim.setBoundaryOptional(0);
			this._dimensions.put(DimName.R, dim);
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
			this._dimensions.put(DimName.THETA, dim);
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
			// TODO
		}
	}
	
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
			this._dimensions.put(DimName.PHI, dim);
			dim = new Dimension();
			dim.setCyclic();
			dim.setLength(2 * Math.PI);
			this._dimensions.put(DimName.THETA, dim);
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
	}
}

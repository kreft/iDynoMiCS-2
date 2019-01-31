package surface;

import linearAlgebra.Vector;
import utility.ExtraMath;

/**
 * This class constructs and holds the bounding box for sphere swept volumes
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class BoundingBox
{	
	/**
	 * TODO
	 */
	protected double[] _dimensions;
	
	/**
	 * TODO
	 */
	protected double[] _higher;
	
	/**
	 * TODO
	 */
	protected double[] _lower;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public BoundingBox()
	{
		
	}
	
	/**
	 * construct from a matrix of locations, a sphere radius for the sphere-
	 * swept volume and an optional margin.
	 * 
	 * @param p
	 * @param radius
	 * @param margin
	 */
	public BoundingBox get(double[][] p, double radius, double margin)
	{
		double size = radius + margin;
		this._higher = upper(p, size);
		this._lower = lower(p, size);
		this._dimensions = dimensions(p, size);
		return this;
	}
	
	/**
	 * multiple point constructor
	 * @param p
	 * @param radius
	 */
	public BoundingBox get(double[][] p, double radius)
	{
		return this.get(p, radius, 0.0);
	}
	
	/**
	 * single point constructor with margin
	 * @param p
	 * @param radius
	 * @param margin
	 */
	public BoundingBox get(double[] p, double radius, double margin)
	{
		return this.get(new double[][]{ p }, radius, margin);
	}
	
	/**
	 * single point constructor
	 * @param p
	 * @param radius
	 */
	public BoundingBox get(double[] p, double radius)
	{
		return this.get(p, radius, 0.0);
	}
	
	/**
	 * \brief Construct a bounding box directly.
	 * 
	 * @param dimensions
	 * @param lower
	 */
	public BoundingBox get(double[] dimensions, double[] lower)
	{
		Vector.checkLengths(dimensions, lower);
		this._dimensions = dimensions;
		this._lower = lower;
		return this;
	}
	
	/**
	 * \ Construct a bounding box directly from lower and upper corner
	 * @param lower
	 * @param upper
	 * @param b
	 */
	public BoundingBox get(double[] lower, double[] upper, boolean b) 
	{
		this._lower = lower;
		this._higher = upper;
		this._dimensions = Vector.minus(upper, lower);
		return this;
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/

	/**
	 * return the box as report string
	 * @return
	 */
	public String getReport()
	{
		return "lower: " + Vector.toString(this._lower) + " dimensions: " + 
				Vector.toString(this._dimensions);
	}
	
	/**
	 * return the rib lengths/ dimensions of the bounding box
	 * @return
	 */
	public double[] ribLengths()
	{
		return this._dimensions;
	}
	
	/**
	 * return the lower corner of the bounding box
	 * @return
	 */
	public double[] lowerCorner()
	{
		return this._lower;
	}
	
	public double[] higherCorner()
	{
		return this._higher;
	}

	/*************************************************************************
	 * RANDOM POSITION
	 ************************************************************************/
	
	/**
	 * @return Random position inside this bounding box.
	 */
	public double[] getRandomInside()
	{
		double[] out = Vector.randomZeroOne(this._dimensions);
		Vector.timesEquals(out, this._dimensions);
		Vector.addEquals(out, this._lower);
		return out;
	}
	
	/**
	 * @return Random position on the surface of this bounding box.
	 */
	public double[] getRandomOnPeriphery()
	{
		/* Get a random point inside this bounding box. */
		double[] out = getRandomInside();
		/*
		 * Choose a random dimension, and force the position to one of the two
		 * extremes in that dimension.
		 */
		int dim = ExtraMath.getUniRandInt(out.length);
		out[dim] = this._lower[dim];
		if ( ExtraMath.getRandBool() )
			out[dim] += this._dimensions[dim];
		return out;
	}
	
	/*************************************************************************
	 * STATIC HELPER METHODS
	 ************************************************************************/
	
	/* static calculation vectors, reduce garbage for calculations */
	protected static double[] sOut = null;
	protected static double[] sPoint = null;
	
	/**
	 * returns the lower corner of the bounding box
	 * @param radius
	 * @return coordinates of lower corner of bounding box
	 */
	private static double[] lower(double[][] points, double radius) 
	{
		/*
		 * First find the lowest position in each dimension.
		 */
		sOut = points[0].clone();
		for ( int pointIndex = 1; pointIndex < points.length; pointIndex++ )
		{
			sPoint = points[pointIndex];
			for ( int dim = 0; dim < sOut.length; dim++ )
				sOut[dim] = Math.min(sOut[dim], sPoint[dim]);
		}
		/*
		 * Subtract the radius from this in each dimension.
		 */
		Vector.addEquals(sOut, - radius);
		return sOut;
	}
	
	/**
	 * returns the upper corner of the bounding box
	 * @param radius
	 * @return coordinates of upper corner of bounding box
	 */
	private static double[] upper(double[][] points, double radius) 
	{
		/*
		 * First find the greatest position in each dimension.
		 */
		sOut = points[0].clone();
		for ( int pointIndex = 1; pointIndex < points.length; pointIndex++ )
		{
			sPoint = points[pointIndex];
			for ( int dim = 0; dim < sOut.length; dim++ )
				sOut[dim] = Math.max(sOut[dim], sPoint[dim]);
		}
		/*
		 * Add the radius to this in each dimension.
		 */
		Vector.addEquals(sOut, radius);
		return sOut;
	}
	
	/**
	 * returns the rib lengths of the bounding box
	 * @param radius
	 * @return dimensions of the bounding box
	 */
	private static double[] dimensions(double[][] points, double radius) 
	{
		if ( points.length == 1 )
			return Vector.vector(points[0].length, 2 * radius);
		return Vector.minus(upper(points, radius), lower(points, radius));
	}
}

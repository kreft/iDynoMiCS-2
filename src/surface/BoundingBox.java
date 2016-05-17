package surface;

import linearAlgebra.Vector;

/**
 * This class constructs and holds the bounding box for sphere swept volumes
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
	protected double[] _lower;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * construct from a matrix of locations, a sphere radius for the sphere-
	 * swept volume and an optional margin.
	 * 
	 * @param p
	 * @param radius
	 * @param margin
	 */
	public BoundingBox(double[][] p, double radius, double margin)
	{
		double size = radius + margin;
		this._dimensions = dimensions(p, size);
		this._lower = lower(p, size);	
	}
	
	/**
	 * multiple point constructor
	 * @param p
	 * @param radius
	 */
	public BoundingBox(double[][] p, double radius)
	{
		this(p, radius, 0.0);
	}
	
	/**
	 * single point constructor with margin
	 * @param p
	 * @param radius
	 * @param margin
	 */
	public BoundingBox(double[] p, double radius, double margin)
	{
		this(new double[][]{ p }, radius, margin);
	}
	
	/**
	 * single point constructor
	 * @param p
	 * @param radius
	 */
	public BoundingBox(double[] p, double radius)
	{
		this(p, radius, 0.0);
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

	/*************************************************************************
	 * STATIC HELPER METHODS
	 ************************************************************************/
	
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
		double[] out = points[0];
		double[] point;
		for ( int pointIndex = 1; pointIndex < points.length; pointIndex++ )
		{
			point = points[pointIndex];
			for ( int dim = 0; dim < out.length; dim++ )
				out[dim] = Math.min(out[dim], point[dim]);
		}
		/*
		 * Subtract the radius from this in each dimension.
		 */
		Vector.addEquals(out, - radius);
		return out;
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
		double[] out = points[0];
		double[] point;
		for ( int pointIndex = 1; pointIndex < points.length; pointIndex++ )
		{
			point = points[pointIndex];
			for ( int dim = 0; dim < out.length; dim++ )
				out[dim] = Math.max(out[dim], point[dim]);
		}
		/*
		 * Add the radius to this in each dimension.
		 */
		Vector.addEquals(out, radius);
		return out;
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

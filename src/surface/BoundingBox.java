package surface;

import linearAlgebra.Vector;
import spatialRegistry.Area;

/**
 * This class constructs and holds the bounding box for sphere swept volumes
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class BoundingBox extends Area
{	

	
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
		super(lower(p, radius + margin), upper(p, radius + margin));
	}
	
	/**
	 * multiple point constructor
	 * @param p
	 * @param radius
	 */
	public BoundingBox(double[][] p, double radius)
	{
		super(lower(p, radius), upper(p, radius));
	}
	
	/**
	 * single point constructor with margin
	 * @param p
	 * @param radius
	 * @param margin
	 */
	public BoundingBox(double[] p, double radius, double margin)
	{
		super(lower(new double[][]{ p }, radius + margin),
				upper(new double[][]{ p }, radius + margin));
	}
	
	/**
	 * single point constructor
	 * @param p
	 * @param radius
	 */
	public BoundingBox(double[] p, double radius)
	{
		super(lower(new double[][]{ p }, radius),
				upper(new double[][]{ p }, radius));
	}
	
	/**
	 * \brief Construct a bounding box directly.
	 * 
	 * @param dimensions
	 * @param lower
	 */
	public BoundingBox(double[] lower, double[] higher)
	{
		super(lower, higher);
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
		return "lower: " + Vector.toString(this.getLow()) + " higher: " + 
				Vector.toString(this.getHigh());
	}
	
	/**
	 * return the rib lengths/ dimensions of the bounding box
	 * @return
	 */
	public double[] ribLengths()
	{
		return Vector.minus(this.getHigh(), this.getLow());
	}

	/*************************************************************************
	 * RANDOM POSITION
	 ************************************************************************/
	
	/**
	 * @return Random position inside this bounding box.
	 */
	public double[] getRandomInside()
	{
		double[] out = Vector.randomZeroOne(this.ribLengths());
		Vector.timesEquals(out, this.ribLengths());
		Vector.addEquals(out, this.getLow());
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

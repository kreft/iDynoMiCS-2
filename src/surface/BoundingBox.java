package surface;

import linearAlgebra.Vector;

/**
 * This class constructs and holds the bounding box for sphere swept volumes
 * @author baco
 *
 */
public class BoundingBox {
	
	protected double[] dimensions;
	protected double[] lower;
	
	/**
	 * construct from a matrix of locations, a sphere radius for the sphere-
	 * swept volume and an optional margin.
	 * @param p
	 * @param radius
	 * @param margin
	 */
	public BoundingBox(double[][] p, double radius, double margin)
	{
		this.dimensions = dimensions(p,radius+margin);
		this.lower = lower(p,radius+margin);	
	}
	
	/**
	 * multiple point constructor
	 * @param p
	 * @param radius
	 */
	public BoundingBox(double[][] p, double radius)
	{
		this(p,radius,0);
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
		this(p, radius, 0);
	}
	
	/**
	 * return the box as report string
	 * @return
	 */
	public String getReport()
	{
		return "lower: " + Vector.dblToString(lower) + " dimensions: " + 
				Vector.dblToString(dimensions);
	}
	
	/**
	 * return the rib lengths/ dimensions of the bounding box
	 * @return
	 */
	public double[] ribLengths()
	{
		return dimensions;
	}
	
	/**
	 * return the lowser corner of the bounding box
	 * @return
	 */
	public double[] lowerCorner()
	{
		return lower;
	}

	/**************************************************************************
	 * Single point (Sphere) bounding box methods
	 */
	
	/**
	 * returns the lower corner of the bounding box
	 * @param p
	 * @param radius
	 * @return
	 */
	private double[] lower(double[] p, double radius) 
	{
		double[] coord = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			coord[i] = p[i] - radius;
		return coord;
	}
	
	/**
	 * returns the rib lengths of the bounding box
	 * @param p
	 * @param radius
	 * @return
	 */
	private double[] dimensions(double[] p, double radius) 
	{
		double[] dimensions = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			dimensions[i] = radius * 2.0;
		return dimensions;
	}
	
	/**
	 * returns the upper corner of the bounding box
	 * @param p
	 * @param radius
	 * @return
	 */
	private double[] upper(double[] p, double radius) 
	{
		double[] coord = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			coord[i] = p[i] + radius;
		return coord;
	}
	
	/**************************************************************************
	 * multiple point bounding box (assuming sphere swept volumes with constant
	 * radius.
	 */
	
	/**
	 * returns the lower corner of the bounding box
	 * @param radius
	 * @return coordinates of lower corner of bounding box
	 */
	private double[] lower(double[][] points, double radius) 
	{
		double[] coord = lower(points[0], radius);
		if(points.length == 1)
			return coord;
		for (double[] o: points)  
			for ( int i = 0; i < points[0].length; i++ ) 
				coord[i] = Math.min( coord[i], lower(o, radius)[i] );
		return coord;
	}
	
	/**
	 * returns the upper corner of the bounding box
	 * @param radius
	 * @return coordinates of upper corner of bounding box
	 */
	private double[] upper(double[][] points, Double radius) 
	{
		double[] upper = new double[points[0].length];
		for (double[] o: points) 
			for ( int i = 0; i < points[0].length; i++ ) 
				upper[i] = Math.max( upper[i], upper(o,radius)[i] );
		return upper;
	}
	
	/**
	 * returns the rib lengths of the bounding box
	 * @param radius
	 * @return dimensions of the bounding box
	 */
	private double[] dimensions(double[][] points, Double radius) 
	{
		if(points.length == 1)
			return dimensions(points[0], radius);
		double[] coord 		= lower(points, radius);
		double[] upper 		= upper(points, radius);
		double[] dimensions	= new double[points[0].length];
		for (int i = 0; i < points[0].length; i++)
			dimensions[i] = upper[i] - coord[i];
		return dimensions;
	}
}

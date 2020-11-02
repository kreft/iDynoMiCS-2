package dataIO;

import dataIO.Log.Tier;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import shape.Shape;
import surface.Ball;
import surface.Rod;
import surface.Surface;

/**
 * General interface for graphical output writers such as Svg and Pov export
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public interface GraphicalExporter extends Instantiable {
	
	
	/*************************************************************************
	 * File handling
	 ************************************************************************/
	
	/**
	 * 
	 * @param prefix
	 */
	public void createFile(String prefix);
	
	/**
	 * 
	 */
	public void closeFile();
	
	/*************************************************************************
	 * Drawing objects
	 ************************************************************************/
	
	/**
	 * 
	 * @param surface
	 * @param pigment
	 */
	public default void draw(Surface surface, Object pigment)
	{
		String pigmentString = this.resolveColour( this.pigment( pigment) );
		if (surface instanceof Ball)
			this.draw((Ball) surface, pigmentString);
		if (surface instanceof Rod)
			this.draw((Rod) surface, pigmentString);
	}
	
	public default Object pigment(Object pigment)
	{
		float[] pigmentArray = new float[3];
		if (pigment instanceof String)
		{
			return (String) pigment;
		}
		else if (pigment instanceof double[])
		{
			pigmentArray = Vector.toFloat((double[]) pigment);
		}
		else if (pigment instanceof float[])
		{
			pigmentArray = (float[]) pigment;
		}
		return pigmentArray;
	}
	
	/**
	 * 
	 * @param ball
	 */
	public void draw(Ball ball, String pigment);
	
	/**
	 * 
	 * @param rod
	 */
	public void draw(Rod rod, String pigment);
	
	/**
	 * 
	 */
	public String resolveColour(Object pigment);
	
	/*************************************************************************
	 * Drawing basic shapes
	 ************************************************************************/
	
	/**
	 * 
	 * @param center
	 * @param radius
	 * @param pigment
	 */
	public void sphere(double[] center, double radius, String pigment);
	
	/**
	 * 
	 * @param center
	 * @param radius
	 * @param pigment
	 */
	public void circle(double[] center, double radius, String pigment);
	
	/**
	 * 
	 * @param circle_center
	 * @param element_origin
	 * @param dimension
	 * @param numPointsOnArc
	 * @param pigment
	 */
	public void circleElement(double[] circle_center, double[] element_origin, 
			double[] dimension ,double numPointsOnArc, String pigment);
	
	/**
	 * 
	 * @param base
	 * @param top
	 * @param radius
	 * @param pigment
	 */
	public void cylinder(double[] base, double[] top, double radius, 
			String pigment);
	
	/**
	 * 
	 * @param lowerCorner
	 */
	public void cube(double[] lowerCorner, double[] dimensions, 
			String pigment);
	
	/**
	 * 
	 * @param lowerCorner
	 * @param dimensions
	 * @param pigment
	 */
	public void rectangle(double[] base, double[] top, double width, 
			String pigment);
	
	/**
	 * 
	 * @param location
	 * @param dimensions
	 * @param pigment
	 */
	public void rectangle(double[] location, double[] dimensions, 
			String pigment);
	
	/*************************************************************************
	 * Helper methods
	 ************************************************************************/
	
	/**
	 * 
	 * @param vector
	 * @return
	 */
	public default double[] to2D(double[] vector)
	{
		return Vector.subset(vector, 2);
	}
	
	/**
	 * 
	 * @param vector
	 * @return
	 */
	public default double[] to3D(double[] vector)
	{
		if (vector.length < 3)
			return to3D(Vector.append(vector, 0.0));
		else if (vector.length > 3)
		{
			Log.out(Tier.CRITICAL, "Warning 4 dimensional vector in graphical "
					+ "exporter, returning null");
			return null;
		}
		else
			return vector;
	}

	/**
	 * 
	 * @param _prefix
	 * @param shape
	 */
	public default void init(String _prefix, Shape shape)
	{
		
	}

	public void createCustomFile(String fileName);
	
}

package dataIO;

import linearAlgebra.Vector;
import surface.Ball;
import surface.Rod;
import surface.Surface;

public interface GraphicalExporter {
	
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
	public default void draw(Surface surface, String pigment)
	{
		if (surface instanceof Ball)
			this.draw((Ball) surface, pigment);
		if (surface instanceof Rod)
			this.draw((Rod) surface, pigment);
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
		return Vector.appendDouble(vector, 0.0);
	}
	
}

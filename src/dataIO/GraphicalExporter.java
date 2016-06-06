package dataIO;

import surface.Ball;
import surface.Rod;

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
	 * @param ball
	 */
	public void draw(Ball ball);
	
	/**
	 * 
	 * @param rod
	 */
	public void draw(Rod rod);
}

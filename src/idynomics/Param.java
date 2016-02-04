package idynomics;

import org.w3c.dom.Element;

/**
 * Class holds global parameters typically used throughout multiple compartments
 * and classes.
 * @author baco
 *
 */
public class Param {
	
	/**
	 * Simulation name
	 */
	public static String simulationName;
	
	/**
	 * String is set to the location of the protocol file
	 */
	public static String protocolFile;
	
	/**
	 * xml document root element.
	 */
	public static Element xmlDoc;
	
	/**
	 * the simulation folder will be placed in this folder
	 */
	public static String outputRoot = "../../Simulations/";
	
	/**
	 * All output is written to this folder and sub-folders
	 */
	public static String outputLocation;

	/**
	 * If a comment is defined in the protocol file it will be stored here.
	 */
	public static String simulationComment;
	
	/***************************************************************************
	 * GENERAL PARAMETERS 
	 * all directly loaded from xml file as string.
	 */
	
	public static String timeStepSize;
	
	public static String endOfSimulation;
	
}

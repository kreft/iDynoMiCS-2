package idynomics;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.tier;
import utility.Helper;

/**
 * Class holds global parameters typically used throughout multiple compartments
 * and classes.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Param
{
	/**************************************************************************
	 * GENERAL PARAMETERS 
	 * all directly loaded from xml file as string.
	 *************************************************************************/
	
	/**
	 * Simulation name.
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
	
	/**
	 * 
	 */
	@Deprecated
	public static String timeStepSize;
	
	/**
	 * 
	 */
	@Deprecated
	public static String endOfSimulation;
	
	/**************************************************************************
	 * LOADING
	 *************************************************************************/
	
	/**
	 * \brief Method for loading the 
	 * 
	 * @param elem
	 */
	public static void init(Element elem)
	{
		/*
		 *   
		 */
		// TODO safety: check the root exists, and the name is acceptable
		outputRoot = XmlHandler.obtainAttribute(elem, XmlLabel.outputFolder);
		simulationName = 
					XmlHandler.obtainAttribute(elem, XmlLabel.nameAttribute);
		outputLocation = outputRoot + "/" + simulationName + "/";
		/* 
		 * Set up the log file.
		 */
		// TODO check that this will be a new log file if we're running
		// multiple simulations.
		tier t = null;
		while ( t == null ) 
		{
			try
			{
				t = tier.valueOf(
						XmlHandler.obtainAttribute(elem, XmlLabel.logLevel));
			}
			catch (IllegalArgumentException e)
			{
				System.out.println("log level not recognized, use: " + 
						Helper.enumToString(tier.class));
			}
		}
		if ( ! Log.isSet() )
			Log.set(t);
		/* 
		 * 
		 */
		simulationComment = 
				XmlHandler.gatherAttribute(elem, XmlLabel.commentAttribute);
	}
}

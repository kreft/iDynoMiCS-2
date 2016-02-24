package idynomics;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.tier;
import utility.Helper;

/**
 * Class holds global parameters typically used throughout multiple compartments
 * and classes.
 * @author baco
 *
 */
public class Param
{
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
	
	/***************************************************************************
	 * GENERAL PARAMETERS 
	 * all directly loaded from xml file as string.
	 */
	
	public static String timeStepSize;
	
	public static String endOfSimulation;
	
	
	public static void init(Node xmlNode)
	{
		Element elem = (Element) xmlNode;
		/*
		 *   
		 */
		// TODO safety: check the root exists, and the name is acceptable
		outputRoot = XmlHandler.obtainAttribute(elem, XmlLabel.outputFolder);
		simulationName = XmlHandler.obtainAttribute(elem, XmlLabel.nameAttribute);
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
		simulationComment = XmlHandler.gatherAttribute(elem,
												XmlLabel.commentAttribute);
	}
}

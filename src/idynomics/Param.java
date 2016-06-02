package idynomics;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
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
	public String simulationName;
	
	/**
	 * String is set to the location of the protocol file
	 */
	public String protocolFile;
	
	/**
	 * xml document root element.
	 */
	public Element xmlDoc;
	
	/**
	 * the simulation folder will be placed in this folder
	 */
	public String outputRoot;
	
	/**
	 * All output is written to this folder and sub-folders
	 */
	public String outputLocation;

	/**
	 * If a comment is defined in the protocol file it will be stored here.
	 */
	public String simulationComment;

	/**
	 * Root folder for this simulation
	 */
	public String idynomicsRoot = "";
	
	/**************************************************************************
	 * LOADING
	 *************************************************************************/
	
	public static void init()
	{
	// TODO safety: check the root exists, and the name is acceptable
			Idynomics.global.outputRoot = 
					Helper.obtainInput(Idynomics.global.outputRoot, 
							"Required " + XmlLabel.outputFolder, true);
			Idynomics.global.simulationName = 
					Helper.obtainInput(Idynomics.global.simulationName,
							"Required simulation name", true);
			setOutputLocation();
	}
	
	public static void setOutputLocation()
	{
		Idynomics.global.outputLocation = 
				Idynomics.global.outputRoot + "/" +
				Idynomics.global.simulationName + "/";
	}
	
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
		Idynomics.global.outputRoot = XmlHandler.obtainAttribute(elem, XmlLabel.outputFolder);
		Idynomics.global.simulationName = 
					XmlHandler.obtainAttribute(elem, XmlLabel.nameAttribute);
		Idynomics.global.outputLocation = Idynomics.global.outputRoot + "/" + Idynomics.global.simulationName + "/";
		/* 
		 * Set up the log file.
		 */
		// TODO check that this will be a new log file if we're running
		// multiple simulations.
		Tier t = null;
		while ( t == null ) 
		{
			try
			{
				t = Tier.valueOf(
						XmlHandler.obtainAttribute(elem, XmlLabel.logLevel));
			}
			catch (IllegalArgumentException e)
			{
				System.out.println("Log level not recognized, use: " + 
						Helper.enumToString(Tier.class));
			}
		}
		if( ! Log.isSet() )
			Log.set(t);
		/* 
		 * 
		 */
		Idynomics.global.simulationComment = 
				XmlHandler.gatherAttribute(elem, XmlLabel.commentAttribute);
	}
}

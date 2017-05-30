package idynomics;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import referenceLibrary.XmlRef;
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
		if (Idynomics.global.idynomicsRoot == null || 
				Idynomics.global.simulationName == null)
		{
			Idynomics.global.outputRoot = 
					Helper.obtainInput(Idynomics.global.outputRoot, 
							"Required " + XmlRef.outputFolder, false);
			Idynomics.global.simulationName = 
					Helper.obtainInput(Idynomics.global.simulationName,
							"Required simulation name", false);
			setOutputLocation();
		}
	}
	
	public static void setOutputLocation()
	{
		/* set date format for folder naming */
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_");
		
		/* set output root for this simulation */
		Idynomics.global.outputLocation = 
				Idynomics.global.outputRoot + "/" + 
				dateFormat.format(new Date()) + 
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
		 *   set output root from xml file
		 */
		Idynomics.global.outputRoot = 
				XmlHandler.obtainAttribute(elem, XmlRef.outputFolder, XmlRef.simulation);
		
		/* set simulation name from xml file */
		Idynomics.global.simulationName = 
					XmlHandler.obtainAttribute(elem, XmlRef.nameAttribute, XmlRef.simulation);
		
		setOutputLocation();
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
						XmlHandler.obtainAttribute(elem, XmlRef.logLevel, XmlRef.simulation));
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
				XmlHandler.gatherAttribute(elem, XmlRef.commentAttribute);
	}
}

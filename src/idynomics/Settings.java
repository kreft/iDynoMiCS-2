package idynomics;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import referenceLibrary.SettingsRef;
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * Class holds global parameters typically used throughout multiple compartments
 * and classes.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Settings
{
	/**************************************************************************
	 * Default settings from cfg file
	 *************************************************************************/
	
	/**
	 * default settings from cfg file
	 */
	public static Properties settings = defaults();
	
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
	
	/**
	 * 
	 */
	public Boolean ignore_protocol_out =  Boolean.valueOf( 
			settings.getProperty( SettingsRef.ignore_protocol_out ) );

	/**
	* Version number of this iteration of iDynoMiCS - required by update
	* procedure.
	*/
	public static String version_number = 
			settings.getProperty( SettingsRef.version_number );

	/**
	 * Version description.
	 */
	public static String version_description = 
			settings.getProperty( SettingsRef.version_description );
	
	public static String exitCommand;
	

	/**************************************************************************
	 * Appearance
	 * Still also suplying default value's for if the cfg file is corrupted.
	 *************************************************************************/
	
	public static Color console_color = Helper.obtainColor( 
			SettingsRef.console_color, settings, "38,45,48");
	
	public static Color text_color = Helper.obtainColor( 
			SettingsRef.text_color, settings, "220,220,220");
	
	public static Color error_color = Helper.obtainColor( 
			SettingsRef.error_color, settings, "250,50,50");
	
	public static String font =  Helper.setIfNone( settings.getProperty( 
			SettingsRef.console_font ), "consolas" );
	
	public static int font_size = Helper.setIfNone( Integer.valueOf( 
			settings.getProperty( SettingsRef.console_font_size ) ), 12 );
	
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
		 *   set output root from xml file
		 */
		Idynomics.global.outputRoot = XmlHandler.obtainAttribute( elem, 
				XmlRef.outputFolder, XmlRef.simulation);
		
		/* set simulation name from xml file */
		Idynomics.global.simulationName = XmlHandler.obtainAttribute( elem, 
				XmlRef.nameAttribute, XmlRef.simulation);
		
		updateSettings();
		/* 
		 * Set up the log file.
		 * 
		 * TODO check that this will be a new log file if we're running
		 * multiple simulations.
		 */
		Tier t = null;
		while ( t == null ) 
		{
			try
			{
				t = Tier.valueOf( XmlHandler.obtainAttribute( elem, 
						XmlRef.logLevel, XmlRef.simulation ) );
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
	
	
	public static Properties defaults()
	{
		Properties settings = new Properties();
		try {
			settings.load(new FileInputStream("default.cfg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return settings;
	}
	
	public static void updateSettings()
	{
		/* if no Root location is set use the default out.
		 * TODO safety: check the root exists, and the name is acceptable
		 */
		if (Idynomics.global.idynomicsRoot == null || Idynomics.global.ignore_protocol_out )
		{
			Idynomics.global.outputRoot = 
					settings.getProperty( SettingsRef.default_out );
		}
		
		/* if no simulation name is given ask the user */
		if (Idynomics.global.simulationName == null)
		{
			Idynomics.global.simulationName = 
					Helper.obtainInput(Idynomics.global.simulationName,
							"Required simulation name", true);
		}
		
		if ( Idynomics.global.outputLocation == null )
		{
			/* set date format for folder naming */
			SimpleDateFormat dateFormat = 
					new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_SSS_");
			
			/* set output root for this simulation */
			Idynomics.global.outputLocation = 
					Idynomics.global.outputRoot + "/" + 
					dateFormat.format(new Date()) + 
					Idynomics.global.simulationName + "/";
		}
	}
}

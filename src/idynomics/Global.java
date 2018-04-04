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
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * Class holds global parameters typically used throughout multiple compartments
 * and classes.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Global extends ParameterSet
{
	/**************************************************************************
	 * Default settings from cfg file
	 *************************************************************************/
	
	public Global()
	{
		Properties settings = new Properties();
		try {
			settings.load(new FileInputStream("default.cfg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		set( settings );
	}
	
	public Global(Properties... properties)
	{
		super( properties );

	}

	/**************************************************************************
	 * GENERAL PARAMETERS 
	 * all directly loaded from xml file as string.
	 *************************************************************************/
	
	/**
	 * Version description.
	 */
	public static String version_description = "version_description";

	/**
	* Version number of this iteration of iDynoMiCS - required by update
	* procedure.
	*/
	public static String version_number = "version_number";

	/**
	 * default output location
	 */
	public String default_out = "default_out";
	/**
	 * console font
	 */
	public static String console_font = "consolas";
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
	 * Sub folder structure for sensitivity analysis/parameter estimation
	 */
	public String subFolderStruct = "";
	/**
	 * 
	 */
	public static Boolean ignore_protocol_out = false;

	public int outputskip = 0;

	public static String exitCommand;
	
	public static double densityScale = 1.0;

	/**************************************************************************
	 * Appearance
	 * Still also supplying default value's for if the cfg file is corrupted.
	 *************************************************************************/
	
	public static Color console_color = Helper.obtainColor( "38,45,48" );
	
	public static Color text_color = Helper.obtainColor( "220,220,220" );
	
	public static Color error_color = Helper.obtainColor( "250,50,50" );
	
	public static int font_size = 12;
	
	/**************************************************************************
	 * Global simulation settings
	 *************************************************************************/
	
	/**
	 * Any voxels with a relative well-mixed value of this or greater are
	 * considered well-mixed. this is particularly important to the multi-grid
	 * PDE solver.
	 */
	public double relativeThresholdWellMixedness = 0.9;
	
	/**************************************************************************
	 * LOADING
	 *************************************************************************/
	
	/**
	 * \brief Method for loading the 
	 * 
	 * @param elem
	 */
	public void init(Element elem)
	{
		/*
		 *   set output root from xml file
		 */
		Idynomics.global.outputRoot = XmlHandler.obtainAttribute( elem, 
				XmlRef.outputFolder, XmlRef.simulation);
		/*
		 *  set output sub folder structure from protocol file
		 */
		if ( XmlHandler.hasAttribute(elem, XmlRef.subFolder) )
			Idynomics.global.subFolderStruct = XmlHandler.gatherAttribute(
					elem, XmlRef.subFolder) + "/";
		
		/* set simulation name from xml file */
		Idynomics.global.simulationName = XmlHandler.obtainAttribute( elem, 
				XmlRef.nameAttribute, XmlRef.simulation);
		
		if ( XmlHandler.hasAttribute(elem, XmlRef.outputskip) )
			Idynomics.global.outputskip = Integer.valueOf( 
					XmlHandler.obtainAttribute( elem, XmlRef.outputskip, 
					XmlRef.simulation));
		
		this.updateSettings();
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
	
	public void updateSettings()
	{
		/* if no Root location is set use the default out.
		 * TODO safety: check the root exists, and the name is acceptable
		 */
		if (this.idynomicsRoot == null || ignore_protocol_out )
		{
			this.outputRoot = this.default_out ;
		}
		
		/* if no simulation name is given ask the user */
		if ( this.simulationName == null)
		{
			this.simulationName = Helper.obtainInput( this.simulationName,
					"Required simulation name", false); /* keep this false!! .. 
			the output location is not set here! */
		}
		
		if ( this.outputLocation == null )
		{
			/* set date format for folder naming */
			SimpleDateFormat dateFormat = 
					new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_SSS_");
			
			/* set output root for this simulation */
			this.outputLocation = 
					this.outputRoot + "/" + 
							this.subFolderStruct + "/" + 
					dateFormat.format(new Date()) + 
					this.simulationName + "/";
		}
	}
}

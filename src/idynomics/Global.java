package idynomics;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import processManager.ProcessDiffusion.DistributionMethod;
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
	 * Constructing and loading
	 *************************************************************************/
	
	public Global()
	{
		set( "default.cfg" );
		set( supplementary_property_files );
	}

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
		
		if ( XmlHandler.hasAttribute(elem, XmlRef.configuration) )
		{
			Global.supplementary_property_files = 
					Helper.concatinate(supplementary_property_files, 
					XmlHandler.obtainAttribute( elem, XmlRef.configuration, 
					XmlRef.simulation).split(",") );
		}
		
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
				Log.set(t);
			}
			catch (IllegalArgumentException e)
			{
				System.out.println("Log level not recognized, use: " + 
						Helper.enumToString(Tier.class));
			}
		}
		/* 
		 * 
		 */
		Idynomics.global.simulationComment = 
				XmlHandler.gatherAttribute(elem, XmlRef.commentAttribute);
	}
	
	public void updateSettings()
	{
		/* set any additionally suplied property files */
		set( supplementary_property_files );
		
		/* if no Root location is set use the default out. */
		if (this.idynomicsRoot == null || ignore_protocol_out )
		{
			this.outputRoot = this.default_out;
		}
		
		/* if no simulation name is given ask the user */
		if ( this.simulationName == null)
		{
			this.simulationName = Helper.obtainInput( this.simulationName,
					"Required simulation name", false); /* keep this false!! .. 
			the output location is not always set here! */
		}
		
		if ( this.outputLocation == null )
		{		
			/* set output root for this simulation */
			this.outputLocation = 
					this.outputRoot + "/" + 
							this.subFolderStruct + "/" + 
					long_date_format.format(new Date()) + 
					this.simulationName + "/";
		}
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
	public static String default_out = "default_out";
	
	/**
	 * Allows to disable writing anything to disc
	 * 
	 * useful in scenario's where output do not need to be stored such as with 
	 * unit tests
	 */
	public static boolean write_to_disc = true;
	
	/**
	 * 
	 */
	public static boolean output_compression = false;
	
	/**
	 * enable bookkeeping.
	 */
	public static boolean bookkeeping = true;
	
	/**
	 * enable csv bookkeeping
	 */
	public static boolean csv_bookkeeping = true;
	
	/**
	 * enable xml bookkeeping (also logging complete agent xml)
	 * Warning: very slow
	 */
	public static boolean xml_bookkeeping = true;
	
	/**
	 * console font
	 */
	public static String console_font = "consolas";
	
	/**
	 * Date format used for folder naming
	 */
	public static SimpleDateFormat long_date_format = 
			new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_SSS_");
	
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
	 * Ignore the output location defined in the protocol file, and use the
	 * location as specified in the default.cfg file instead.
	 */
	public static Boolean ignore_protocol_out = false;
	
	/**
	 * Skip writing xml output for this number of global time steps
	 */
	public int outputskip = 1;
	
	/**
	 * The exit command is passed to kernel once the simulation is finished
	 */
	public static String exitCommand;
	
	/**
	 * Supplementary property files to be loaded in after default.
	 */
	public static String[] supplementary_property_files;

	/**************************************************************************
	 * Appearance
	 *************************************************************************/

	public static String default_palette = "colours.xml";
	
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
	public static double relativeThresholdWellMixedness = 0.9;
	
	/**
	 * Only determine location of agent based on primary mass point
	 */
	public static boolean fastAgentDistribution = true;

	public static String agentDistribution = DistributionMethod.MIDPOINT.toString();
	/**
	 * dynamic viscosity of the medium
	 */
	public static double dynamic_viscosity = 0.0;
	
	/**
	 * 
	 */
	public static double collision_scalar = 0.0;
	
	/**
	 * 
	 */
	public static double pull_scalar = 0.0;
	
	/**
	 * 
	 */
	public static double default_attachment_pull_distance = 0.2;
	
	/**
	 * 
	 */
	public static double agent_move_safety = 0.001;
	
	/**
	 * default density difference of microbial cells with medium
	 */
	public static double density_difference = 0.1;
	
	/**
	 * stress scaling introduced to prevent incompatibility with old protocol
	 * files that use the old function, this should be 1 for all new protocol
	 * files and should be removed as soon as all protocols have been updated.
	 */
	public static double agent_stress_scaling = 1;
	
	/**
	 * pass additional collision variables (required for more advanced collision
	 * models but may cause slight slow down for models that do not use them).
	 */
	public static boolean additional_collision_variables = true;
	
	/**
	 * Collision model
	 */
	public static String collision_model = 
			surface.collision.model.DefaultPushFunction.class.getName();
	
	/**
	 * Attraction model
	 */
	public static String attraction_model = 
			surface.collision.model.DefaultPullFunction.class.getName();
	
	/**
	 * Default base time step.
	 */
	public static double mechanical_base_step = 0.0003;
	
	/* 
	 * Default maximum displacement per step, set default if none.
	 */
	public static double mechanical_max_movement = 0.01;
	
	/* 
	 * Default maximum displacement per step, set default if none.
	 */
	public static Integer mechanical_max_iterations = 10000;
	
	/*
	 * Default mechanical stress threshold at which a system may be considered
	 * relaxed.
	 */
	public static double mechanical_low_stress_skip = 0.0;
	
	/**
	 * {@Link SplitTree} atomic length, the smallest length scale for leafnodes
	 */
	public static double atomic_length = 0.05;
	
	/**
	 * {@Link Decompress} fraction of local stress traversing outwards in
	 * decompression algorithm
	 */
	public static double traversing_fraction = 0.02;
	
	/**
	 * Default decompression parameters
	 */
	public static double damping_factor = 0.9;
	
	public static int file_number_of_digits = 5;
}

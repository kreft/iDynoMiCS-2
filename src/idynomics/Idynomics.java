/**
* \package idyno
* \brief Package of classes used to launch iDynomics
*
* Package of classes used to launch and iDynoMiCS simulation, and to update
* the package to the latest stable release. This package is part of iDynoMiCS
* v2.0, governed by the CeCILL license under French law and abides by the
* rules of distribution of free software. You can use, modify and/or
* redistribute iDynoMiCS under the terms of the CeCILL license as circulated
* by CEA, CNRS and INRIA at http://www.cecill.info
*/
package idynomics;

import org.w3c.dom.Element;

import dataIO.Log;

import static dataIO.Log.Tier.*;

import dataIO.XmlHandler;
import dataIO.Log.Tier;
import idynomics.launchable.ConsoleLaunch;
import idynomics.launchable.ExitCommand;
import idynomics.launchable.GeneticAlgorithmLaunch;
import idynomics.launchable.GuiLaunch;
import idynomics.launchable.HelpLaunch;
import idynomics.launchable.Launchable;
import idynomics.launchable.ProtocolLaunch;
import idynomics.launchable.SamplerLaunch;
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public strictfp class Idynomics
{
	/**
	 * {@code Simulator} object: there can only be one. 
	 */
	public static Simulator simulator;
	
	/**
	 * global parameters
	 */
	public static Global global = new Global();
	
	/**
	 * Simulator thread
	 */
	public static Thread simThread;
	
	/**
	 * Contains all predefined className package association for easy class
	 * initiation from xml file.
	 */
	public static XMLableLibrary xmlPackageLibrary = new XMLableLibrary();
	
	/**
	 * iDynoMiCS arguments and description
	 */
	public enum Arguments {
		
		HELP( new String[]{"h", "help"}, 
				"Print help to screen.", new HelpLaunch()),
		
		PROTOCOL( new String[]{"p", "protocol"}, 
				"Run one or multiple protocol files", new ProtocolLaunch()),
		
		GUI( new String[]{"g", "gui"}, 
				"Initiate gui", new GuiLaunch()),
		
		CONSOLE( new String[]{"c", "console"}, 
				"Initiate console", new ConsoleLaunch()),
		
		SAMPLE( new String[]{"s", "sample"}, 
				"Generate child protocol files from master protocol with "
				+ "defined sampling space.", new SamplerLaunch() ),
		
		GA( new String[]{"ga", "geneticAlgorithm"}, 
				"Interpret model results generated by the genetic algorithm and"
				+ " stop when exit conditions are met or generate a new "
				+ "generation", new GeneticAlgorithmLaunch()),
		
		EXIT( new String[] {"exit"}, "execute exit command", new ExitCommand());
		
		private final String[] _flags;
	    private final String _description;
	    private final Launchable _initialization;

	    /**
	     * 
	     * @param flags
	     * @param description
	     */
		Arguments(String[] flags, String description, Launchable initClass)
		{
			this._flags = flags;
			this._description = description;
			this._initialization = initClass;
		}
		
		public void init(String[] args)
		{
			_initialization.initialize( args );
		}
		
		public String toString()
		{
			String out = "";
			for( String s : _flags )
				out += "-" + s + " ";
			out += "\n";
			return out + Helper.limitLineLength(_description, 60, "\t");
		}
	}

	/**
	* \brief Main method used to start iDynoMiCS.
	* 
	* @param args Protocol file paths passed from the command line.
	*/
	public static void main(String[] args)
	{
		
		if ( args.length == 0 )
		{
			System.out.println( "Initiating gui..." );
			Arguments.GUI.init( args );
		}
		else
		{
			Log.out(Tier.NORMAL, "command: "+ Helper.stringAToString( 
					Helper.subset( args, 0, args.length) , " ") );
			for( int i = 0; i < args.length; i++)
			{
				if( args[i].startsWith("-") )
				{
					for( Arguments a : Arguments.values())
					{
						for( String s : a._flags )
							if( args[i].equals("-" + s) )
							{
								a._initialization.initialize(
										Helper.subset( args, i, args.length) );
							}
					}
				}
			}
		}
		
		/* execute exit command if any and if it is not handled by a simulator*/
		if( !Helper.isNullOrEmpty( Global.exitCommand ) && simulator == null )
			Helper.executeCommand( Global.exitCommand );
	}

	
	/**
	 * \brief Set up a simulation from protocol file, check it is ready to
	 * launch, and then launch it.
	 * 
	 * @param protocolPath Path to the XML protocol file.
	 * TODO: change this to return a boolean? check exact usage of this method
	 */
	public static void checkLaunch(String protocolPath)
	{
		if ( ! simulator.isReadyForLaunch() )
		{
			/* prevent writing logFile before tier and location is set */
			Log.printToScreen(
					"Protocol file incomplete! Skipping "+protocolPath, true);
			return;
		}
		
	}
	
	public static void setupGlobals(String protocolPath)
	{
		if ( protocolPath == null )
		{
			/* prevent writing logFile before tier and location is set */
			Log.printToScreen("No protocol path set!", true);
			return;
		}
		/* 
		 * Load the protocol file and find the elements we need
		 */
		Idynomics.global.protocolFile = protocolPath;
		Idynomics.global.xmlDoc = XmlHandler.loadDocument(protocolPath);
		
		Element simElem = XmlHandler.findUniqueChild( Idynomics.global.xmlDoc, 
				XmlRef.simulation );
		/*
		 * Initialise the global parameters.
		 */
		global.init(simElem);
	}
	
	/**
	 * \brief Set up a simulation from XML protocol file.
	 * 
	 * FIXME: why is setupSimulator part of Idynomics rather than Simulator?
	 * @param protocolPath Path to the XML protocol file.
	 */
	public static void setupSimulator(String protocolPath)
	{
		if ( protocolPath == null )
		{
			/* prevent writing logFile before tier and location is set */
			Log.printToScreen("No protocol path set!", true);
			return;
		}
		/* prevent writing logFile before tier and location is set */
			Log.printToScreen("Initiating from: " + protocolPath + 
				"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~", false);
			

		/* 
		 * Load the protocol file and find the elements we need
		 */
		Idynomics.global.protocolFile = protocolPath;
		Idynomics.global.xmlDoc = XmlHandler.loadDocument(protocolPath);
		
		Element simElem = XmlHandler.findUniqueChild( Idynomics.global.xmlDoc, 
				XmlRef.simulation );
		/*
		 * Initialise the global parameters.
		 */
		setupGlobals( protocolPath );
		
		if( Log.shouldWrite(Tier.NORMAL) && 
				!Helper.isNullOrEmpty( Idynomics.global.simulationComment ) )
			Log.out(NORMAL, "Protocol comments:\n" + 
					Idynomics.global.simulationComment );

		
		if( Global.write_to_disc )
		{
			if( Log.shouldWrite(Tier.NORMAL) )
				Log.out(NORMAL, "Storing results in " + 
						Idynomics.global.outputLocation	);
		} else {
			Log.out(CRITICAL, "Warning: Writing to disc is disabled in Global"
					+ " parameters.");
		}

		/*
		 * Create a new Simulator object and intialise it.
		 */
		simulator = new Simulator();
		simulator.instantiate(simElem, null);
	}
	
	/**
	 * \brief Launch the simulator in a new {@code Thread}.
	 */
	public static void launchSimulator()
	{
		simThread = new Thread(simulator);
		simThread.start();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public static String fullDescription()
	{
		return "iDynoMiCS "+Global.version_number+
				" ("+Global.version_description+")";
	}
}

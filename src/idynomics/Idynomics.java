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
import referenceLibrary.XmlRef;

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
	public static Settings global = new Settings();
	
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
	* \brief Main method used to start iDynoMiCS.
	* 
	* @param args Protocol file paths passed from the command line.
	*/
	public static void main(String[] args)
	{
		if ( args.length == 0 )
		{
			System.out.println("Running test protocol");
			setupSimulator("protocol/test.xml");
			launchSimulator();
		}
		for ( String a : args )
		{
			setupSimulator(a);
			launchSimulator();
		}
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
				+ "~~~~~~~~~~~~~~~~~~~~~~~~\n", false);
			

		/* 
		 * Load the protocol file and find the elements we need
		 */
		Idynomics.global.protocolFile = protocolPath;
		Idynomics.global.xmlDoc = XmlHandler.loadDocument(protocolPath);
		
		Element simElem = XmlHandler.loadUnique(Idynomics.global.xmlDoc, XmlRef.simulation);
		/*
		 * Initialise the global parameters.
		 */
		Settings.init(simElem);
		Log.out(NORMAL, Idynomics.global.simulationComment);
		Log.out(NORMAL, "Storing results in " + Idynomics.global.outputLocation+"\n");
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
		return "iDynoMiCS "+Settings.version_number+
				" ("+Settings.version_description+")";
	}
}

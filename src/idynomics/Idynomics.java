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
import dataIO.XmlLabel;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Idynomics
{
	/**
	* Version number of this iteration of iDynoMiCS - required by update
	* procedure.
	*/
	public static String version_number = "2.0";
	
	/**
	 * Version description.
	 */
	public static String version_description = "alpha build 2016.02.19";
	
	/**
	 * {@code Simulator} object: there can only be one. 
	 */
	public static Simulator simulator;
	
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
			setupCheckLaunch("protocol/test.xml");
		}
		for ( String a : args )
			setupCheckLaunch(a);
	}
	
	/**
	 * \brief Set up a simulation from protocol file, check it is ready to
	 * launch, and then launch it.
	 * 
	 * @param protocolPath Path to the XML protocol file.
	 */
	public static void setupCheckLaunch(String protocolPath)
	{
		setupSimulator(protocolPath);
		if ( ! simulator.isReadyForLaunch() )
		{
			System.out.println(
					"Protocol file incomplete! Skipping "+protocolPath);
			return;
		}
		launchSimulator();
	}
	
	/**
	 * \brief Set up a simulation from XML protocol file.
	 * 
	 * @param protocolPath Path to the XML protocol file.
	 */
	public static void setupSimulator(String protocolPath)
	{
		System.out.print("Initiating from: " + protocolPath + 
				"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~\n");
		/* 
		 * Load the protocol file and find the elements we need
		 */
		Element idynoElem = XmlHandler.loadDocument(protocolPath);
		Element simElem = XmlHandler.loadUnique(idynoElem, XmlLabel.simulation);
		/*
		 * Initialise the global parameters.
		 */
		Param.init(simElem);
		Log.out(NORMAL, Param.simulationComment);
		Log.out(NORMAL, "Storing results in " + Param.outputLocation+"\n");
		/*
		 * Create a new Simulator object and intialise it.
		 */
		simulator = new Simulator();
		simulator.init(simElem);
	}
	
	/**
	 * \brief Launch the simulator in a new {@code Thread}.
	 */
	public static void launchSimulator()
	{
		simThread = new Thread(simulator);
	    simThread.start();
	}
}

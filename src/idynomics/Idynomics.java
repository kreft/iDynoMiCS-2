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

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import org.w3c.dom.Element;

import static dataIO.Log.tier.*;
import dataIO.XmlLoad;

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
	public static Double version_number = 2.0;
	
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
		for ( String a : args )
		{
			setupSimulator(a);
			if ( ! simulator.isReadyForLaunch() )
			{
				System.out.println("Protocol file incomplete! Skipping "+a);
				continue;
			}
			launch();
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param protocolPath
	 */
	public static void setupSimulator(String protocolPath)
	{
		System.out.print("Initiating from: " + protocolPath + 
				"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~\n");
		XmlLoad.xmlInit(protocolPath);
		Log.out(NORMAL, Param.simulationComment);
		Log.out(NORMAL, "storing results in " + Param.outputLocation);
		/* Load the protocol file and find the elements we need. */
		Element idynoElem = XmlHandler.loadDocument(protocolPath);
		Element simElem = XmlHandler.loadUnique(idynoElem, XmlLabel.simulation);
		/* Create a new Simulator object and intialise it. */
		simulator = new Simulator();
		simulator.init(simElem);
	}
	
	/**
	 * \brief TODO
	 *
	 */
	public static void launch()
	{
		simThread = new Thread(simulator);
	    simThread.start();
	}
	
	/**
	 * \brief TODO
	 *
	 */
	public static void runXml()
	{
		/*
		 * Report and initiate our protocol file.
		 */
		System.out.print("Initiating from: " + Param.protocolFile + 
				"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~\n");
		
		XmlLoad.xmlInit(Param.protocolFile);
		Log.out(NORMAL, Param.simulationComment);
		Log.out(NORMAL, "storing results in " + Param.outputLocation);
		
		/*
		 * Reset the simulator and Timer to prevent continuation of previous.
		 * Construct the full simulation from the previously loaded xmlDoc.
		 */
		Timer.reset();
		simulator = new Simulator();
		simulator.init(Param.xmlDoc);
		XmlLoad.constructSimulation();
		//Idynomics.simulator.init(xmlNode);
		
		/*
		 * Launch the simulation.
		 */
	    simThread = new Thread(simulator);
	    simThread.start();
	}
	
	
}

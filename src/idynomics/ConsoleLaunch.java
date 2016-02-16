package idynomics;

import java.util.Scanner;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.XmlLoad;
import dataIO.Log;
import dataIO.Log.tier;

/**
 * General class to launch simulation from the console, accepts protocol file
 * from input arguments, asks for input if arguments are missing
 * 
 * @author baco
 *
 */
public class ConsoleLaunch {
	
	public static void main(String[] args) {
		
		System.out.print("Starting iDynoMiCS " +Idynomics.version_number+ "\n");
		
		/*
		 * Acquire our protocol file
		 */
		if(args == null || args.length == 0 || args[0] == null)
		{
			@SuppressWarnings("resource")
			Scanner user_input = new Scanner( System.in );
			System.out.print("Enter protocol file path: ");
			Param.protocolFile = user_input.next( );
		}
		else
		{
			Param.protocolFile = args[0];
		}
		
		/*
		 * Report and initiate our protocol file
		 */
		System.out.print("initiating from: " + Param.protocolFile + 
				"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
				+ "~~~~~~~~~~~~~~~~~~~~~~~~\n");
		
		XmlLoad.xmlInit(Param.protocolFile);
		Log.out(tier.NORMAL, Param.simulationComment);
		Log.out(tier.NORMAL, "storing results in " + 
				Param.outputLocation);
		
		/**
		 * construct the full simulation from the previously loaded xmlDoc
		 */
		XmlLoad.constructSimulation();
		
		/**
		 * Start timing just before simulation starts.
		 */
		double tic = System.currentTimeMillis();
		
		/*
		 * Launch the simulation.
		 */
		Idynomics.simulator.launch();
		
		/*
		 * Print the results.
		 */
		Idynomics.simulator.printAll();
		
		Log.out(tier.QUIET, "Simulation finished in: " + 
				(System.currentTimeMillis() - tic) * 0.001 + " seconds");
		
		// We may include an extensive protocol file check here and ask for
		// additional input if needed.
		
	}

}

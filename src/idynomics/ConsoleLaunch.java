package idynomics;

import java.util.Scanner;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.XmlLoad;
import utility.Helper;
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
		
		runXml();
		
	}
	
	public static void runXml()
	{
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
		 * reset the simulator to prevent loading old simulator
		 * construct the full simulation from the previously loaded xmlDoc
		 */
		Idynomics.simulator = new Simulator();
		XmlLoad.constructSimulation();
		
		/*
		 * Launch the simulation.
		 */
        Idynomics.simThread = new Thread(Idynomics.simulator);
        Idynomics.simThread.start();
	}

}

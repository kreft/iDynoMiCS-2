package idynomics.launchable;

import java.util.Scanner;

import idynomics.Global;
import idynomics.Idynomics;

/**\brief General class to launch simulation from the console, asks user for
 * protocol file path as input.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public strictfp class ConsoleLaunch implements Launchable
{
	/**
	 * \brief Launch a simulation from the Eclipse console.
	 * 
	 * @param args Irrelevant.
	 */
	public static void main(String[] args)
	{
		ConsoleLaunch con = new ConsoleLaunch();
		con.initialize(args);
	}

	@Override
	public void initialize(String[] args) {
		System.out.print("Starting iDynoMiCS " +Global.version_number+ "\n");
		/* Acquire a protocol file. */
		String protocolPath;
		Scanner userInput = null;
		if ( args == null || args.length <= 1 || args[1] == null )
		{
			userInput = new Scanner( System.in );
			System.out.print("Enter protocol file path: ");
			protocolPath = userInput.next();
		}
		else
		{
			protocolPath = args[1];
		}
		/* Now run the simulation with the given protocol file. */
		Idynomics.setupSimulator(protocolPath);
		Idynomics.launchSimulator();
	}
}
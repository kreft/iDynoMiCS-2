package idynomics;

import java.util.Scanner;

/**\brief General class to launch simulation from the console, asks user for
 * protocol file path as input.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public strictfp class ConsoleLaunch
{
	/**
	 * \brief Launch a simulation from the Eclipse console.
	 * 
	 * @param args Irrelevant.
	 */
	public static void main(String[] args)
	{
		System.out.print("Starting iDynoMiCS " +Idynomics.version_number+ "\n");
		/* Acquire a protocol file. */
		String protocolPath;
		Scanner userInput = null;
		if ( args == null || args.length == 0 || args[0] == null )
		{
			userInput = new Scanner( System.in );
			System.out.print("Enter protocol file path: ");
			protocolPath = userInput.next();
		}
		else
		{
			protocolPath = args[0];
		}
		/* Now run the simulation with the given protocol file. */
		Idynomics.setupSimulator(protocolPath);
		Idynomics.launchSimulator();
		/* If a scanner was created, close it down before finishing. */
		if ( userInput != null )
		{
			userInput.close();
		}
	}
}
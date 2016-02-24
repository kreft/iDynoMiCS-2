package idynomics;

import java.util.Scanner;

/**\brief General class to launch simulation from the console, asks user for
 * protocol file path as input.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class ConsoleLaunch
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
		@SuppressWarnings("resource")
		// TODO Rob[24Fec2016]: Is is a problem that we don't close this?
		Scanner user_input = new Scanner( System.in );
		System.out.print("Enter protocol file path: ");
		Param.protocolFile = user_input.next();
		/* Now run the simulation with the given protocol file. */
		Idynomics.runXml();
		//user_input.close();
	}
}
package idynomics;

import java.util.Scanner;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.XmlLoad;
import dataIO.Feedback;
import dataIO.Feedback.LogLevel;

/**
 * General class to launch simulation from the console, accepts protocol file
 * from input arguments, asks for input if arguments are missing
 * 
 * @author baco
 *
 */
public class ConsoleLaunch {
	
	public static void main(String[] args) {
		
		System.out.print("Starting iDynoMiCS " + Idynomics.version_number + 
				"\n");
		
		if(args == null || args.length == 0 || args[0] == null)
		{
			@SuppressWarnings("resource")
			Scanner user_input = new Scanner( System.in );
			System.out.print("Enter protocol file path: ");
			Param.protocolFile = user_input.next( );
			System.out.print("initiating from: " + Param.protocolFile + 
					"\n════════════════════════════════════════════════════════"
					+ "════════════════════════\n");
			
			/*
			 * This will probably done somewhere else, here now for testing
			 */
			Element doc = XmlLoad.loadDocument(Param.protocolFile);
			NodeList general = doc.getElementsByTagName("general");
			for (int i = 0; i < general.getLength(); i++) 
			{
				XmlLoad.loadGeneralParameters(general.item(i));
			}
			Feedback.set(LogLevel.valueOf(Param.outputLevel));
		}
		else
		{
			Param.protocolFile = args[0];
		}
		
		// We may include an extensive protocol file check here and ask for
		// additional input if needed.
		
	}

}

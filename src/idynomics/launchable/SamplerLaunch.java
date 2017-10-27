package idynomics.launchable;

import java.io.IOException;
import java.util.Scanner;

import dataIO.XmlHandler;
import sensitivityAnalysis.XmlCreate;

public class SamplerLaunch implements Launchable {
	
	public enum SampleMethod {
		MORRIS,
		LHC;
	}

	@Override
	public void initialize(String[] args) 
	{
		String xmlFilePath;
		SampleMethod samplingChoice;
		
		@SuppressWarnings("resource")
		Scanner user_input = new Scanner( System.in );
		int p, r;
		if ( args == null || args.length == 1 || args[1] == null )
		{
			System.out.print("Enter the sampling method choice: \n");
			for ( SampleMethod s : SampleMethod.values() )
				System.out.print( s.toString() + " " );
			System.out.print( "\n");
			samplingChoice = SampleMethod.valueOf( user_input.next() );
		}
		else
			samplingChoice = SampleMethod.valueOf( args[1] );
		
		if ( args == null || args.length == 2 || args[2] == null )
		{
			System.out.print("Enter protocol file path: ");
			xmlFilePath = user_input.next();

		}
		else
			xmlFilePath = args[2];
		
		if ( args == null || args.length == 3 || args[3] == null )
		{
			System.out.print("Number sampling levels/ stripes: ");
			p = Integer.valueOf( user_input.next() );

		}
		else
			p = Integer.valueOf( args[3] );
		
		if ( samplingChoice == SampleMethod.MORRIS && 
				( args == null || args.length == 4 || args[4] == null ) )
		{
			System.out.print("Number of repetitions: ");
			r = Integer.valueOf( user_input.next() );

		}
		else if ( samplingChoice == SampleMethod.MORRIS )
			r = Integer.valueOf( args[4] );
		else
			r = 563854578;
		
		try {
			XmlCreate.xmlCopy( XmlHandler.xmlLoad(xmlFilePath), 
					samplingChoice, p, r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

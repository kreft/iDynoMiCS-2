package idynomics.launchable;

import java.util.Scanner;

import optimization.sampling.Sampler;
import sensitivityAnalysis.ProtocolCreater;

public class SamplerLaunch implements Launchable {
	
	@Override
	public void initialize(String[] args) 
	{
		String xmlFilePath;
		Sampler.SampleMethod samplingChoice;
		
		@SuppressWarnings("resource")
		Scanner user_input = new Scanner( System.in );
		int p, r;
		if ( args == null || args.length == 1 || args[1] == null )
		{
			System.out.print("Enter the sampling method choice: \n");
			for ( Sampler.SampleMethod s : Sampler.SampleMethod.values() )
				System.out.print( s.toString() + " " );
			System.out.print( "\n");
			samplingChoice = Sampler.SampleMethod.valueOf( user_input.next() );
		}
		else
			samplingChoice = Sampler.SampleMethod.valueOf( args[1] );
		
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
		
		if ( samplingChoice == Sampler.SampleMethod.MORRIS && 
				( args == null || args.length == 4 || args[4] == null ) )
		{
			System.out.print("Number of repetitions: ");
			r = Integer.valueOf( user_input.next() );

		}
		else if ( samplingChoice == Sampler.SampleMethod.MORRIS )
			r = Integer.valueOf( args[4] );
		else
			r = 0000000; //not used
		
		ProtocolCreater xmlc = new ProtocolCreater( xmlFilePath );
		xmlc.setSampler( samplingChoice, p, r);
		xmlc.xmlWrite();
	}

}

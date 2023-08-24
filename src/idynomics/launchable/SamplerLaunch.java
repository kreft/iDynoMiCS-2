package idynomics.launchable;

import idynomics.Idynomics;
import optimization.sampling.Sampler;
import sensitivityAnalysis.ProtocolCreater;
import utility.Helper;

public class SamplerLaunch implements Launchable {
	
	@Override
	public void initialize(String[] args) 
	{
		String xmlFilePath;
		Sampler.SampleMethod samplingChoice;
		
		boolean customSampling = false;

		int p, r;
		if ( args == null || args.length == 1 || args[1] == null )
		{
			samplingChoice = Sampler.SampleMethod.valueOf( 
					Helper.obtainInput(	Sampler.SampleMethod.values(), 
					"Enter the sampling method choice:", true) );
		}
		else
			samplingChoice = Sampler.SampleMethod.valueOf( args[1] );
		
		if ( args == null || args.length <= 2 || args[2] == null )
		{
			if( Idynomics.global.protocolFile == null )
			{
				xmlFilePath = Helper.obtainInput("",
						"Enter protocol file path: ",true);
			}
			else
			{
				xmlFilePath = Idynomics.global.protocolFile;
			}
		}
		else
			xmlFilePath = args[2];
		if ( samplingChoice == Sampler.SampleMethod.CUSTOM || samplingChoice == Sampler.SampleMethod.EXTERNAL )
		{
			customSampling = true;
			p = 0; /* not used */
		}
		else if ( args == null || args.length <= 3 || args[3] == null )
		{
			p = Integer.valueOf( Helper.obtainInput("",
					"Number sampling levels/ stripes: ",true) );
		}
		else
			p = Integer.valueOf( args[3] );
		
		if ( samplingChoice == Sampler.SampleMethod.MORRIS && 
				( args == null || args.length <= 4 || args[4] == null ) )
		{
			r = Integer.valueOf( Helper.obtainInput("",
					"Number of repetitions: ",true) );

		}
		else if ( ( samplingChoice == Sampler.SampleMethod.SIMPLE ||
					samplingChoice == Sampler.SampleMethod.LHC )	&&
				( args == null || args.length <= 4 || (args.length == 5 && args[4] == null) ) )
		{
			boolean force_round = Helper.obtainInput("Force rounding?",true);
			if( force_round )
				r = 1;
			else
				r = 0;
		}
		else if ( samplingChoice == Sampler.SampleMethod.MORRIS )
			r = Integer.valueOf( args[4] );
		else  if ( samplingChoice == Sampler.SampleMethod.CUSTOM || samplingChoice == Sampler.SampleMethod.EXTERNAL )
			r = 0;
		else
			r = Integer.valueOf( args[4] ); //switch
		
		Idynomics.setupGlobals( xmlFilePath );
		
		ProtocolCreater xmlc = new ProtocolCreater(xmlFilePath,customSampling);
		xmlc.setSampler( samplingChoice, p, r);
		xmlc.xmlWrite();
	}

}

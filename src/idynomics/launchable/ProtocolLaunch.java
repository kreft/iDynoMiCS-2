package idynomics.launchable;

import idynomics.Idynomics;

public class ProtocolLaunch implements Launchable {

	@Override
	public void initialize(String[] args)
	{
		for ( int i = 1; i < args.length; i++ )
		{
			if( args[i].regionMatches(0, "-", 0, 1) )
				break;
			Idynomics.setupSimulator( args[i] );
			Idynomics.launchSimulator();
			try {
				Idynomics.simThread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

}

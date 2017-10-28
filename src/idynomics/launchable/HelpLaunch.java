package idynomics.launchable;

import idynomics.Idynomics.Arguments;

public class HelpLaunch implements Launchable {

	@Override
	public void initialize(String[] args) 
	{
		for( Arguments a : Arguments.values() )
		{
			System.out.println( a );
		}
	}

}

package idynomics.launchable;

import idynomics.Idynomics;
import utility.Helper;

public class ExitCommand implements Launchable {

	@Override
	public void initialize(String[] args) {
//		Idynomics.global.exitCommand = args[1];
		Idynomics.global.exitCommand = Helper.stringAToString( 
				Helper.subset(args, 1, args.length), " ");
	}

}

package idynomics.launchable;

import idynomics.Idynomics;

public class ExitCommand implements Launchable {

	@Override
	public void initialize(String[] args) {
		Idynomics.global.exitCommand = args[1];
	}

}

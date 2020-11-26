package idynomics;

import java.io.File;
import java.util.List;

import dataIO.CumulativeLoad;

public class PostProcess implements Runnable {

	/**
	 * Files to be post processed
	 */
	private List<File> _files;
	
	/**
	 * Post processing script
	 */
	private File _script;
	
	private CumulativeLoad loader;

	public PostProcess(File script, List<File> files)
	{
		this._script = script;
		this._files = files;
		this.loader = new CumulativeLoad( this._script.getAbsolutePath() );
	}
	
	@Override
	public void run() 
	{
		int num = 0;
		for( File f : _files)
		{
			Idynomics.setupSimulator( f.getAbsolutePath() );
			loader.postProcess(num);
			Idynomics.simulator = new Simulator();
			num++;
		}
	}
}

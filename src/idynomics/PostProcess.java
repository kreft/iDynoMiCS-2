package idynomics;

import java.io.File;
import java.util.List;

public class PostProcess implements Runnable {

	/**
	 * Files to be post processed
	 */
	private List<File> _files;
	
	/**
	 * Post processing script
	 */
	private File _script;

	public PostProcess(File script, List<File> files)
	{
		this._script = script;
		this._files = files;
	}
	
	@Override
	public void run() 
	{
		for( File f : _files)
		{
			Idynomics.setupSimulator( f.getAbsolutePath() );
		}
	}
}

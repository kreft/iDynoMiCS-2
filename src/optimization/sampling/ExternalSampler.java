package optimization.sampling;

import optimization.geneticAlgorithm.DataFromCSV;

public class ExternalSampler extends Sampler {

	private String _filePath;
	
	public ExternalSampler(String filename) 
	{
		this._filePath = filename;
	}

	@Override
	public int size() 
	{
		return DataFromCSV.getInput(_filePath).length;
	}

	@Override
	public double[][] sample() 
	{
		return DataFromCSV.getInput(_filePath);
	}
	

}

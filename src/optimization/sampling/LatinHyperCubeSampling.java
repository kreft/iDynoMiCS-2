package optimization.sampling;

import java.util.ArrayList;
import java.util.List;
import utility.ExtraMath;

public class LatinHyperCubeSampling extends Sampler {
	
	private int _stripes, _dimensions;
	
	public int size()
	{
		return this._stripes;
	}
	
	public LatinHyperCubeSampling()
	{
		
	}
	
	public LatinHyperCubeSampling(int stripes, int dimensions)
	{
		this._stripes = stripes;
		this._dimensions = dimensions;
	}
	

	public double[][] sample(int stripes, int dimensions) 
	{
		this._stripes = stripes;
		this._dimensions = dimensions;
		return sample();
	}
		
	/**
	 * 
	 */
	@Override
	public double[][] sample() {	
		/* initialise random number generator */
		if( !ExtraMath.isAvailable() )
			ExtraMath.initialiseRandomNumberGenerator();
		
		double[][] out = new double[_stripes][_dimensions];
		double[] temp = new double[_stripes];
		
		/* split sampling space 0 to 1 by amount of stripes */
		double d = 1.0 / _stripes;

		for (int i = 0; i < _dimensions; i++) 
		{
			List<Double> rowShuffle = new ArrayList<Double>();
			for (int j = 0; j < _stripes; j++) 
			{
				rowShuffle.add( ExtraMath.getUniRand(j * d, (j + 1) * d) );
			}			
			
			for ( int j = 0; j < _stripes; j++ )
			{
				temp[j] = rowShuffle.remove( ExtraMath.getUniRandInt( 
						rowShuffle.size() ) );
			}

			for (int j = 0; j < _stripes; j++) 
				out[j][i] = temp[j];
		}
		return out;
	}	
}

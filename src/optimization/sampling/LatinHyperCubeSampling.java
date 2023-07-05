package optimization.sampling;

import java.util.ArrayList;
import java.util.List;

import dataIO.Log;
import dataIO.Log.Tier;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief Latin hypercube can be used to draw a quasi-random sample from unit-
 * space whilst maintaining a certain degree of sample spreading. See also:
 * https://en.wikipedia.org/wiki/Latin_hypercube_sampling 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class LatinHyperCubeSampling extends Sampler {
	
	/**
	 * number of stripes and dimensions for this sampler
	 */
	private int _s, _d;
	
	/**
	 * returns the number of samples this sampler will return
	 */
	public int size()
	{
		return this._s;
	}
	
	/**
	 * \brief construct LHC sampler without setting the LHC parameters.
	 */
	public LatinHyperCubeSampling()
	{
		
	}
	
	/**
	 * \brief construct LHC sampler, preset number of stripes and dimensions.
	 * @param stripes
	 * @param dimensions
	 */
	public LatinHyperCubeSampling(int stripes, int dimensions)
	{
		this._s = stripes;
		this._d = dimensions;
	}
	
	/**
	 * \brief draw a LHC sample (stripes equals the number of samples) points 
	 * from unit space with the provided number of dimensions.
	 * 
	 * @param stripes
	 * @param dimensions
	 * @return Matrix with random numbers, contains 
	 * {@link LatinHyperCubeSampling#_s} number of rows with 
	 * {@link LatinHyperCubeSampling#_d} number of columns.
	 */
	public double[][] sample(int stripes, int dimensions) 
	{
		this._s = stripes;
		this._d = dimensions;
		return sample();
	}
		
	/**
	 * \brief draw a LHC sample points from unit space
	 * 
	 * @return Matrix with random numbers, contains 
	 * {@link LatinHyperCubeSampling#_s} number of rows with 
	 * {@link LatinHyperCubeSampling#_d} number of columns.
	 */
	@Override
	public double[][] sample() 
	{	
		/* check initiation */
		if ( Helper.isNullOrEmpty(_s) || Helper.isNullOrEmpty(_d) )
		{
			if ( Log.shouldWrite(Tier.NORMAL) )
				Log.out(Tier.NORMAL, "Error: LHC sampler must be initiated"
						+ "before sampling");
			return null;
		}
		
		/* initialise random number generator */
		if( ExtraMath.random == null )
    		ExtraMath.initialiseRandomNumberGenerator();
		
		double[][] out = new double[_s][_d];
		double[] temp = new double[_s];
		
		/* split unit space into equally spaced (d) stripes */
		double d = 1.0 / _s;

		/* cycle over each dimension */
		for (int i = 0; i < _d; i++) 
		{
			List<Double> rows = new ArrayList<Double>();
			/* add a uniform random number within the bounds of each stripe */
			for (int j = 0; j < _s; j++) 
				rows.add( ExtraMath.getUniRand(j * d, (j + 1) * d) );		
			
			/* shuffle stripes in random order */
			for ( int j = 0; j < _s; j++ )
				temp[j] = rows.remove( ExtraMath.getUniRandInt( rows.size() ) );

			/* add the random points for each stripe in this dimension to the 
			 * out matrix */
			for (int j = 0; j < _s; j++) 
				out[j][i] = temp[j];
		}
		return out;
	}	
}

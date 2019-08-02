package optimization.sampling;


import dataIO.Log;
import dataIO.Log.Tier;
import utility.Helper;

/**
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class SimpleSampler extends Sampler {
	
	/**
	 * number of stripes and dimensions for this sampler
	 */
	private int _s, _d;
	
	/**
	 * returns the number of samples this sampler will return
	 */
	public int size()
	{
		return (int) Math.pow(_s+1,_d);
	}
	
	/**
	 * \brief construct LHC sampler without setting the LHC parameters.
	 */
	public SimpleSampler()
	{
		
	}
	
	/**
	 * \brief construct LHC sampler, preset number of stripes and dimensions.
	 * @param stripes
	 * @param dimensions
	 */
	public SimpleSampler(int stripes, int dimensions)
	{
		this._s = stripes-1;
		this._d = dimensions;
	}
	
	/**
	 * @param stripes
	 * @param dimensions
	 * @return Matrix with random numbers, contains 
	 * {@link SimpleSampler#_s} number of rows with 
	 * {@link SimpleSampler#_d} number of columns.
	 */
	public double[][] sample(int stripes, int dimensions) 
	{
		this._s = stripes-1;
		this._d = dimensions;
		return sample();
	}
		
	/**
	 * @return Matrix with random numbers, contains 
	 * {@link SimpleSampler#_s} number of rows with 
	 * {@link SimpleSampler#_d} number of columns.
	 */
	@Override
	public double[][] sample() 
	{	
		/* check initiation */
		if ( Helper.isNullOrEmpty(_s) || Helper.isNullOrEmpty(_d) )
		{
			if ( Log.shouldWrite(Tier.NORMAL) )
				Log.out(Tier.NORMAL, "Error: Simple sampler must be initiated"
						+ "before sampling");
			return null;
		}

		double[][] out = new double[size()][_d];
		
		/* split unit space into equally spaced (d) stripes */
		double d = 1.0 / _s;

		for( int i = 0; i < out[0].length; i++)
			out[0][i] = 0.0;

		/* cycle over each dimension */
		for (int i = 0; i < (_d); i++) 
		{
			double cur = 0.0;
			if( i == 0) {
				for (int j = 1; j < out.length ; j++) 
				{
					cur = next(d, cur);
					out[j][i] = cur;
				}
			}
			else
			{
				for (int j = 1; j < out.length ; j++) 
				{
					boolean tally = true;
					for (int k = i-1; k >= 0 ; k--) 
					{
						if( out[j][k] != 0.0 )
						{
							tally = false;
							break;
						}
					}
					if(tally)
						cur = next(d, cur);
					out[j][i] = cur;
				}
			}
		}
		return out;
	}	
	
	public double next(double d, double current)
	{
		return( ( current+d > 1.0001 ? 0.0 : current+d ) );
	}
}

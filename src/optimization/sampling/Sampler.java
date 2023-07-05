package optimization.sampling;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import sensitivityAnalysis.MorrisSampling;
import utility.Helper;

/**
 * \brief Super class for methods sampling in unit space;
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public abstract class Sampler {
	
	/**
	 * Available sampling methods
	 */
	public enum SampleMethod {
		MORRIS,
		LHC,
		SIMPLE,
		CUSTOM,
		EXTERNAL;
	}

	/**
	 * 
	 * @return number of samples
	 */
	public abstract int size();
	
	/**
	 * 
	 * @return sample drawn from unit space (between 0 and 1)
	 */
	public abstract double[][] sample();
	
	public static boolean forceRound = false;

	public double[][] sample( double[][] bounds )
	{
		return scale( sample(), bounds);
	}
	
	public double[][] sample( double[][] bounds, int[] type )
	{
		if( this instanceof CustomSampler || this instanceof ExternalSampler )
			return sample();
		else
			/* still returns normal scaling if that is requested */
			return expScale( sample(), bounds, type);
	}
	
	public static Sampler getSampler(Sampler.SampleMethod method, 
			double[][] ranges, int... pars)
	{
		int k = 0, p = 0, r = 0;
		String filename = null;
		/* externally supplied samples (CSV) */
		if (method == Sampler.SampleMethod.EXTERNAL)
			filename = Helper.obtainInput( "", "Sample file", false);
		else
		{
			if ( pars.length < 1)
				/* Number of levels. */
				k = Integer.valueOf( Helper.obtainInput( "", 
						"Number of dimensions in sampling space", false));
			else
				k = pars[0];
			
			if ( pars.length < 2)
				/* Number of levels. */
				p = Integer.valueOf( Helper.obtainInput( "", 
						"Number of sampling levels.", false));
			else
				p = pars[1];
		}
		
		switch ( method )
		{
		case MORRIS :
			/* Parameters for Morris method */
			
			if ( pars.length < 3)
				/* Number of repetitions. From input? */
				r = Integer.valueOf( Helper.obtainInput( "", 
						"Number of repetitions", false));    
			else
				r = pars[2];
			return new MorrisSampling(k,p,r);
		
		case LHC :
			return new LatinHyperCubeSampling(p, k);
		case SIMPLE :
			if ( pars.length > 2)
				r = pars[2];
			if( r == 1 )
				forceRound = true;
			return new SimpleSampler(p, k);
		case CUSTOM :
			return new CustomSampler(ranges);
		case EXTERNAL:
			return new ExternalSampler(filename);
		}
		return null;
	}
	
	public double[][] scale(double[][] probs, double[][] bounds)
	{
		double[] ones = Vector.onesDbl( this.size() );
		double[][] out = Matrix.add(Vector.outerProduct(ones, bounds[0]),
				Matrix.elemTimes(Vector.outerProduct( ones, 
				Vector.minus(bounds[1], bounds[0]) ), probs) );
		if( forceRound )
		{
			for(int i = 0; i < out.length; i++)
			{
				for(int j = 0; j < out[0].length; j++) 
				{
					out[i][j] = (double) Math.round(out[i][j]);
				}
			}
		}
		return out;
	}
	
	/* may be done nicer but works */
	public double[][] expScale(double[][] probs, double[][] bounds, int[] type)
	{
			for(int i = 0; i < probs.length; i++)
			{
				for(int j = 0; j < probs[0].length; j++) 
				{
					if( type[j] == 1)
						probs[i][j] = expScale( probs[i][j], bounds[0][j], 
								bounds[1][j], 10.0 );
					else
						probs[i][j] = scale( probs[i][j], bounds[0][j], 
								bounds[1][j] );
				}
			}
		return probs;
	}
	
	public double expScale(double in, double low, double high, double base)
	{
		return( low + ((Math.pow(base,in) - 1.0 ) / (base-1.0)) * (high-low) );
	}
	
	public double scale(double in, double low, double high)
	{
		return( low + ( in * (high-low) ) );
	}
	
}

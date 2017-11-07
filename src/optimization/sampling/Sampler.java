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
		LHC;
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

	public double[][] sample( double[][] bounds )
	{
		return scale( sample(), bounds);
	}
	
	public static Sampler getSampler(Sampler.SampleMethod method, int... pars)
	{
		int k, p, r;
		
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
		}
		return null;
	}
	
	public double[][] scale(double[][] probs, double[][] bounds)
	{
		double[] ones = Vector.onesDbl( this.size() );
		return Matrix.add(Vector.outerProduct(ones, bounds[0]),
				Matrix.elemTimes(Vector.outerProduct( ones, 
				Vector.minus(bounds[1], bounds[0]) ), probs) );
	}
}

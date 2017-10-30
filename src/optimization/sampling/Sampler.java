package optimization.sampling;

import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import sensitivityAnalysis.MorrisSampling;
import utility.Helper;

public class Sampler {
	
	public enum SampleMethod {
		MORRIS,
		LHC;
	}

	public int size() {
		return 0;
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

//			writeOutputs(r*(k+1), states);
		
		case LHC :
			return new LatinHyperCubeSampling(p, k);
//			writeOutputs(p ,samples);
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
	
	public double[][] sample()
	{
		return null;
	}

	public double[][] sample( double[][] bounds )
	{
		return scale( sample(), bounds);
	}
}

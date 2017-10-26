package optimization.sampling;

import java.util.ArrayList;
import java.util.List;

import utility.ExtraMath;

public class LatinHyperCubeSampling {

	public static double[][] sample(int stripes, int dimensions) {
		double[][] out = new double[stripes][dimensions];
		double[] temp = new double[stripes];
		
		/* split sampling space 0 to 1 by amount of stripes */
		double d = 1.0 / stripes;

		for (int i = 0; i < dimensions; i++) 
		{
			List<Double> rowShuffle = new ArrayList<Double>();
			for (int j = 0; j < stripes; j++) 
			{
				rowShuffle.add( ExtraMath.getUniRand(j * d, (j + 1) * d) );
			}			
			
			for ( int j = 0; j < stripes; j++ )
			{
				temp[j] = rowShuffle.remove( ExtraMath.getUniRandInt( 
						rowShuffle.size() ) );
			}

			for (int j = 0; j < stripes; j++) 
				out[j][i] = temp[j];
		}
		return out;
	}	
}
package optimization.objectiveFunction;

/**
 * \brief Returns the cumulative distribution based on z-value 
 * calculated using mean and standard error
 * 
 * @author Sankalp Arya (sarya@ncsu.edu), NCSU, Raleigh.
 *
 */
public class CumulativeDistribution implements ObjectiveFunction {
	
	private double[] _mean;
	private double[] _sd;
	
	public void setData( double[][] data )
	{
		this._mean = data[0];
		this._sd = data[1];
	}
	
	public double loss(double[] x)
	{
		double out = 0.0;
		double[] z = new double[x.length];
		int n = _mean.length;
		
		for (int i = 0; i < n; i++)
		{
			z[i] = (x[i] - _mean[i])/_sd[i];
			double temp = Math.exp((0-Math.pow(z[i],2))/2);
			out += (1-temp)/n;
		}
		
		return out;
	}
}

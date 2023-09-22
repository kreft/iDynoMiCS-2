package optimization.objectiveFunction;

/**
 * \brief Returns the cumulative distribution based on z-value 
 * calculated using mean and standard error
 * 
 * @author Sankalp Arya (sarya@ncsu.edu), NCSU, Raleigh.
 *
 */
public class CumulativeDistribution implements ObjectiveFunction {
	
	private double[] _data;
	
	public void setData( double[] data )
	{
		this._data = data;
	}
	
	public double loss(double[] x)
	{
		double out = 0.0;
		double[] z = {};
		double sd = SD(x);
		
		for (int i = 0; i < _data.length; i++)
		{
			z[i] = (x[i] - _data[i])/sd;
			double temp =(1/Math.sqrt(2*Math.PI))*Math.exp((0-Math.pow(z[i],2))/2);
			out += temp;
		}
		
		return out;
	}
	
	public double SD(double[] x)
	{
		double sum = 0.0;
		double standardDeviation = 0.0;
		double mn = mean(x);
		
		int n = x.length;
		
		for (int i = 0; i < n; i++)
			sum += Math.pow(x[i] - mn, 2);
		
		standardDeviation = Math.sqrt(sum/n);
		
		return standardDeviation;
	}
	
	public double mean(double[] x)
	{
		double sum = 0.0;
		double meanValue = 0.0;
		
		int n = x.length;
		
		for (int i = 0; i < n; i++)
			sum += x[i];
		
		meanValue = sum/n;
		return meanValue;
	}
}

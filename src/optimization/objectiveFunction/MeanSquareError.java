package optimization.objectiveFunction;

/**
 * \brief returns the mean square error of the provided data set and a given x 
 * set.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class MeanSquareError implements ObjectiveFunction {
	
	private double[] _data;
	
	public void setData( double[] data )
	{
		this._data = data;
	}
	
	public double loss(double[] x)
	{
		double out = 0;
		for (int i = 0; i < _data.length; i++)
			out += Math.pow(_data[i] - x[i] , 2);
		return out/x.length;
	}
}

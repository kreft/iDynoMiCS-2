package optimization.objectiveFunction;

/**
 * \brief the quadratic loss function returns the Sum of the quadratic error of
 * x and the dataset.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class QuadraticLossFunction implements ObjectiveFunction {
	
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
		return out;
	}

}

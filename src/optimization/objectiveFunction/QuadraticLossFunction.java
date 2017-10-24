package optimization.objectiveFunction;

public class QuadraticLossFunction implements ObjectiveFunction {
	
	private double[] data;
	
	public double loss(double[] x)
	{
		double out = 0;
		for (int i = 0; i < data.length; i++)
			out += Math.pow(data[i], x[i]);
		return out;
	}

}

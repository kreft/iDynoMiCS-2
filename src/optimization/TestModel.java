package optimization;

import utility.ExtraMath;

public class TestModel {
	
	double _a = 2.0;
	double _b = 0.1;
	
	public TestModel()
	{
		
	}
	
	public TestModel(double a, double b)
	{
		this._a = a;
		this._b = b;
	}
	
	public double getY(double x)
	{
		return  _a + _b * Math.pow(x, 2);
	}

	public double getMeasurment(double x, double noise)
	{
		return getY(x) + noise * ExtraMath.getNormRand();
	}
}

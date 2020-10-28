package surface;

import java.util.HashMap;

import expression.Expression;
import linearAlgebra.Vector;
import shape.Shape;

public class LinearSpring implements Spring {

	private double _restLength;
	private Point _a;
	private Point _b;
	private Expression _springFunction;
	HashMap<String, Double> springVars = new HashMap<String,Double>();
	
	public LinearSpring(double stiffness, Point[] points, 
			Expression springFunction, double restLength)
	{
		this._springFunction = springFunction;
		this._restLength = restLength;
		this._a = points[0];
		this._b = points[1];
		
		springVars.put("stiffness", stiffness);
	}
	
	public boolean ready()
	{
		if (this._springFunction == null)
			return false;
		return true;
	}
	
	public void setRestValue(double restLength)
	{
		this._restLength = restLength;
	}
	
	public void setSpringFunction(Expression function)
	{
		this._springFunction = function;
	}
	
	public void setStiffness(double stiffness)
	{
		springVars.put("stiffness", stiffness);
	}
	
	public void applyForces(Shape shape)
	{
		double[] diff = shape.getMinDifferenceVector( 
				_a.getPosition(), _b.getPosition() );
		double dn = Vector.normEuclid(diff);
		springVars.put("dh", dn-this._restLength);
		double[] fV	= Vector.times(diff, 
				this._springFunction.getValue(springVars) );
		
		/* apply forces */
		Vector.addEquals( this._b.getForce(), fV ) ;
		Vector.reverseEquals(fV);
		Vector.addEquals( this._a.getForce(), fV ) ;
	}
}

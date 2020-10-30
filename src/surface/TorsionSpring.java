package surface;

import java.util.HashMap;

import dataIO.Log;
import expression.Expression;
import linearAlgebra.Vector;
import shape.Shape;

public class TorsionSpring implements Spring {

	private double _restAngle;
	private Point _a;
	private Point _b;
	private Point _c;
	private Expression _springFunction;
	HashMap<String, Double> springVars = new HashMap<String,Double>();
	
	public TorsionSpring(double stiffness, Point[] points, 
			Expression springFunction, double restAngle)
	{
		this._springFunction = springFunction;
		this._restAngle = restAngle;
		this._a = points[0];
		this._b = points[1];
		this._c = points[2];
		
		springVars.put("stiffness", stiffness);
	}
	
	public TorsionSpring(double stiffness, Point a, Point b, Point c, 
			Expression springFunction, double restAngle)
	{
		this._springFunction = springFunction;
		this._restAngle = restAngle;
		this._a = a;
		this._b = b;
		this._c = c;
		
		springVars.put("stiffness", stiffness);
	}
	
	public boolean ready()
	{
		if (this._springFunction == null)
			return false;
		return true;
	}
	
	public void setPoint(int i, Point points)
	{
		if( i == 0 )
			this._a = points;
		if( i == 1 )
			this._b = points;
		if( i == 2 )
			this._c = points;
	}
	
	public void setRestValue(double restAngle)
	{
		this._restAngle = restAngle;
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
		double[] a = shape.getNearestShadowPoint(_a.getPosition(), 
				_b.getPosition() );
		double[] c = shape.getNearestShadowPoint(_c.getPosition(), 
				_b.getPosition() );
		
		Vector.minusEquals(a, this._b.getPosition());
		Vector.minusEquals(c, this._b.getPosition());
		Vector.spherifyTo(a, a);
		Vector.spherifyTo(c, c);
		
		double thetaAngle = Math.abs( a[1] - c[1] );
		
//		Log.out(a[2] + " " +  c[2] + " " + phiAngle);
			
		double outTheta = (_restAngle - thetaAngle) * 0.5;
		if( a[1] > c[1] )
		{
			a[1] += outTheta;
			c[1] -= outTheta;
		}
		else
		{
			a[1] -= outTheta;
			c[1] += outTheta;
		}
		
		double outPhi = 0;

		double ac = 0;
		double cc = 0;
		
		if( a.length > 2)
		{
			
			ac = a[2]-0.5*_restAngle;
			cc = c[2]-0.5*_restAngle;
			
			double phiAngle = ac + cc;
			outPhi = phiAngle*0.5;
			
				a[2] -= outPhi;
				c[2] -= outPhi;
		}
		
//		a = flippySpinny(a);
//		c = flippySpinny(c);
		
		Vector.unspherifyEquals(a);
		Vector.unspherifyEquals(c);
		Vector.addEquals(a, _b.getPosition());
		Vector.addEquals(c, _b.getPosition());

		double[] directionA = Vector.normaliseEuclid(
				shape.getMinDifferenceVector( a, _a.getPosition() ) );
		double[] directionC = Vector.normaliseEuclid(
				shape.getMinDifferenceVector( c, _c.getPosition() ) );
		double[] directionB = Vector.normaliseEuclid(
				Vector.times( Vector.add( directionA, directionC ), -1.0 ) );
		double z = Math.abs(ac) + Math.abs(cc);
		/* If agents are approaching alignment in the z-axis (z = Pi) the 
		 * weight of relative x/y angles drops.
		 * 
		 * This might need some sine/cosine?
		 */
		springVars.put("dif", Math.abs( outTheta * (Math.PI - z) + outPhi ) );
		
		double[] fV	= Vector.times(directionA, 
				this._springFunction.getValue(springVars) );

		Vector.addEquals( this._a.getForce(), fV ) ;

		fV	= Vector.times(directionC, 
				this._springFunction.getValue(springVars) );

		Vector.addEquals( this._c.getForce(), fV ) ;

		/* b receives force from both sides */
		fV	= Vector.times(Vector.times(directionB, 2.0), 
				this._springFunction.getValue(springVars) );

		Vector.addEquals( this._b.getForce(), fV ) ;
	}
	
	public double[] flippySpinny( double[] a)
	{
		if( a[2] < 0.0 )
		{
			a[1] += Math.PI;
			a[2] = Math.abs(a[2]);
		}
		if( a[2] > Math.PI )
		{
			a[1] += Math.PI;
			a[2] = 2 * Math.PI - a[2];
		}
		return a;
	}
}

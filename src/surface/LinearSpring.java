package surface;

import java.util.HashMap;

import org.w3c.dom.Element;

import expression.Expression;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Shape;

public class LinearSpring implements Spring {

	private double _restLength;
	private Point _a;
	private Point _b;
	private Expression _springFunction;
	HashMap<String, Double> springVars = new HashMap<String,Double>();
	
	public LinearSpring()
	{
	
	}
	
	public LinearSpring(double stiffness, Point[] points, 
			Expression springFunction, double restLength)
	{
		this._springFunction = springFunction;
		this._restLength = restLength;
		this._a = points[0];
		this._b = points[1];
		
		springVars.put("stiffness", stiffness);
	}
	
	public void setPoint(int i, Point points)
	{
		if( i == 0 )
			this._a = points;
		if( i == 1 )
			this._b = points;
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
		if ( Double.isNaN(fV[0]))
			System.out.println(fV[0]);
		
		/* apply forces */
		Vector.addEquals( this._b.getForce(), fV ) ;
		Vector.reverseEquals(fV);
		if ( Double.isNaN(fV[0]))
			System.out.println(fV[0]);
		Vector.addEquals( this._a.getForce(), fV ) ;
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Module getModule() {
		Module modelNode = new Module(XmlRef.spring, this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);
		modelNode.add( new Attribute(XmlRef.typeAttribute, 
				this.getClass().getSimpleName(), null, false));
		return modelNode;
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}
}

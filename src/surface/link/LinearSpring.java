package surface.link;

import java.util.HashMap;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.Expression;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Shape;
import surface.Point;

/**
 * \brief, linear springs between two mass points either trough spine or linked
 * 
 * @author Bastiaan
 *
 */
public class LinearSpring implements Spring {

	private double _restLength;
	public Point _a;
	public Point _b;
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
	
	public void setPoint(int i, Point points, boolean tempDuplicate)
	{
		if( i == 0 )
			this._a = points;
		if( i == 1 )
			this._b = points;
		/* duplicate check */
		if( Vector.equals( this._a.getPosition(),this._b.getPosition() ))
			Log.out("duplicate point");
	}
	
	/**
	 * returns true if the spring function was created previously
	 */
	public boolean ready()
	{
		if (this._springFunction == null)
			return false;
		return true;
	}
	
	/* used to avoid evaluating this spring twice */
	public boolean isLeadingPoint(Point p)
	{
		if( this._a.identifier() > this._b.identifier() )
			return this._a.identifier() == p.identifier();
		else
			return this._b.identifier() == p.identifier();
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
	
	/**
	 * Apply the forces resulting from this spring to the associated points.
	 */
	public void applyForces(Shape shape)
	{
		/* calculate the difference between the rest length and actual distance
		 * between the points */
		double[] diff = shape.getMinDifferenceVector( 
				_a.getPosition(), _b.getPosition() );
		double dn = Vector.normEuclid(diff);
		springVars.put("dh", dn-this._restLength);
		
		/* when debugging this can be used to check rather large deltas */
		if( Log.shouldWrite( Tier.DEBUG ) && 
				Math.abs(dn-this._restLength) > 0.1)
			Log.out( Tier.DEBUG, String.valueOf( dn-this._restLength ));
		
		double[] fV	= Vector.times(diff, 
				this._springFunction.getValue(springVars) );
		
		/* If errors occur this extra check will show up if either the spring
		 * function or the diff returns NaNs. */
		if ( Log.shouldWrite( Tier.DEBUG ) && Double.isNaN(fV[1]))
			Log.out( Tier.DEBUG, fV[1] + " Linear spring");
		
		/* apply forces */
		Vector.addEquals( this._b.getForce(), fV ) ;
		Vector.reverseEquals(fV);
		Vector.addEquals( this._a.getForce(), fV );
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

	/**
	 * currently not stored as separate entity but as link
	 */
	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString()
	{
		return "Linear "+_restLength+ " " + Vector.toString(_a.getPosition()) +
				" " + Vector.toString( _b.getPosition() );
	}
}

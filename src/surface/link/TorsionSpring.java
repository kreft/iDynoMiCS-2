package surface.link;

import java.util.HashMap;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.Expression;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Shape;
import surface.Point;

/**
 * \brief: torsion springs between three points, outer points a and c rotate
 * around b
 * 
 * @author Bastiaan
 *
 */
public class TorsionSpring implements Spring {

	private double _restAngle;
	private Point _a;
	private Point _b;
	private Point _c;
	private Expression _springFunction;
	HashMap<String, Double> springVars = new HashMap<String,Double>();
	
	public TorsionSpring()
	{
	
	}
	
	public TorsionSpring(double stiffness, Point[] points, 
			Expression springFunction, double restAngle)
	{
		this._springFunction = springFunction;
		this._restAngle = restAngle;
		this._a = points[0];
		this._b = points[1];
		this._c = points[2];
		
		/* check for duplicate positions */
		if( Vector.equals( this._a.getPosition(),this._b.getPosition()) || 
				Vector.equals( this._c.getPosition(), this._b.getPosition()) )
			Log.out("duplicate point");
		
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
		
		/* check for duplicate positions */
		if( Vector.equals( this._a.getPosition(),this._b.getPosition()) || 
				Vector.equals( this._c.getPosition(), this._b.getPosition()) )
			Log.out("duplicate point");
		
		springVars.put("stiffness", stiffness);
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
	
	public void setPoint(int i, Point points, boolean tempDuplicate)
	{
		if( i == 0 )
			this._a = points;
		if( i == 1 )
			this._b = points;
		if( i == 2 )
			this._c = points;

		/* check for duplicate positions */
		if( !tempDuplicate && (Vector.equals( this._a.getPosition(),
				this._b.getPosition()) || Vector.equals( this._c.getPosition(), 
				this._b.getPosition()) ) )
			Log.out("duplicate point");
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
	
	/**
	 * Apply the forces resulting from this spring to the associated points.
	 * The forces on a and c are reverse equals applied to b such that the net
	 * force on the entire construct equals out. 
	 *
	 * note: in order to find the rest position of the outer points the system
	 * is converted to spherical coordinates with center point b as reference 
	 * point.
	 */
	public void applyForces(Shape shape)
	{
		double[] a = shape.getNearestShadowPoint(_a.getPosition(), 
				_b.getPosition() );
		double[] c = shape.getNearestShadowPoint(_c.getPosition(), 
				_b.getPosition() );
				
		Vector.minusEquals(a, this._b.getPosition());
		Vector.minusEquals(c, this._b.getPosition());
		
		/* Neither a or c should have the same position as b */
		if( Log.shouldWrite(Tier.DEBUG) )
		{
			if( Vector.equals( this._a.getPosition(),
					this._b.getPosition()) || 
					Vector.equals( this._c.getPosition(), 
					this._b.getPosition()))
				Log.out(Tier.DEBUG, "duplicate point");
		}
		
		/* currently we only support torsion springs that relax to a linear
		 * alignment */
		double u = Math.PI - Vector.angle(a, c);
		if( Log.shouldWrite(Tier.DEBUG) )
		{
			if( Double.isNaN(u))
				Idynomics.simulator.interupt = true;
		}
		
		Vector.spherifyTo(a, a);
		Vector.spherifyTo(c, c);
		
		/* the angle in the x,y plane */
		double thetaAngle = Math.abs( a[1] - c[1] );		
		/* we only support torsion springs that relax to a linear alignment! */
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
		
		/* the angle in the z direction */
		double outPhi, ac, cc = 0;
		if( a.length > 2)
		{
			/* We take a vector aligned with the x,y plane to be zero. such that
			 * the sum of the two angles cancel out as long as any angle up from
			 * the x,y plane is balanced as long as the opposing vector is 
			 * pointing down with the same angle.
			 */
			ac = a[2]-0.5*_restAngle;
			cc = c[2]-0.5*_restAngle;
			
			double phiAngle = ac + cc;
			outPhi = phiAngle*0.5;
			a[2] -= outPhi;
			c[2] -= outPhi;
		}
		
		/* now a and c represent the closest points in polar space from the
		 * original points that make for a linear alignment between a, b and c
		 * and we can convert them back to the Cartesian system. */
		Vector.unspherifyEquals(a);
		Vector.unspherifyEquals(c);
		Vector.addEquals(a, _b.getPosition());
		Vector.addEquals(c, _b.getPosition());

		/* the direction a, b and c should be pushed for alignment */
		double[] directionA = Vector.normaliseEuclid(
				shape.getMinDifferenceVector( a, _a.getPosition() ) );
		double[] directionC = Vector.normaliseEuclid(
				shape.getMinDifferenceVector( c, _c.getPosition() ) );
		double[] directionB = Vector.normaliseEuclid(
				Vector.times( Vector.add( directionA, directionC ), -1.0 ) );

		/* the dif term is used in the force calculation to scale depending on
		 * the angle between the three point. eg: a weak bend should receive
		 * less force than a sharp bend. */
		springVars.put("dif", u );
		
		/* apply force to a */
		double[] fV	= Vector.times(directionA, 
				this._springFunction.getValue(springVars) );
		if( Log.shouldWrite(Tier.DEBUG) )
			if ( Double.isNaN(fV[1]))
				Log.out(Tier.DEBUG, fV[1]+" torsion" );
		Vector.addEquals( this._a.getForce(), fV ) ;

		/* apply force to c */
		fV	= Vector.times(directionC, 
				this._springFunction.getValue(springVars) );
		if( Log.shouldWrite(Tier.DEBUG) )
			if ( Double.isNaN(fV[1]))
				Log.out(Tier.DEBUG, fV[1]+" torsion");
		Vector.addEquals( this._c.getForce(), fV ) ;

		/* b receives force from both sides in opposing direction */
		fV	= Vector.times(Vector.times(directionB, 2.0), 
				this._springFunction.getValue(springVars) );
		if( Log.shouldWrite(Tier.DEBUG) )
			if ( Double.isNaN(fV[1]))
				Log.out(Tier.DEBUG, fV[1]+" torsion");
		Vector.addEquals( this._b.getForce(), fV ) ;
		
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
	
	
	public String toString()
	{
		return "Torsion "+_restAngle+ " "+Vector.toString(_a.getPosition())+
				" "+Vector.toString(_b.getPosition())+
				" "+Vector.toString(_c.getPosition());
	}
}

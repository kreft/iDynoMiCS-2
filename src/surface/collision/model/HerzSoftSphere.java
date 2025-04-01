package surface.collision.model;

import org.w3c.dom.Element;

import aspect.AspectInterface;
import dataIO.XmlHandler;
import idynomics.Global;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Settable;
import surface.collision.CollisionFunction;
import surface.collision.CollisionVariables;

/**
 * Herz soft sphere collision model
 */
public class HerzSoftSphere implements CollisionFunction
{
	private double _forceScalar = Global.collision_scalar;
	
	/**
	 * Implementation of the Instantiatable interface
	 * 
	 * Young's Moduli ranging from aprox. 0.04 MPa (S. Putrefaciens CN32, 
	 * PH10, Gaboriaud 2005) and 769 MPa (B. Casi, Kumar 2009) See also
	 * Tuson 2012: 10.1111/j.1365-2958.2012.08063.x
	 */
	public void instantiate(Element xmlElement, Settable parent)
	{
		/*
		 * For Herz we are looking for the effective Young's modulus (E_eff)
		 * In case of hugely different Young's moduli we may want to extend
		 * on this and calculate the effective modulus on the fly
		 */
		Double forceScalar = XmlHandler.gatherDouble( xmlElement, 
				XmlRef.forceScalar);
		if( forceScalar != null )
				this._forceScalar = forceScalar;
	}
	
	/**
	 * \brief return the currently set force scalar for this 
	 * CollisionFunction
	 * 
	 * @return double force scalar
	 */
	public double forceScalar()
	{
		return this._forceScalar;
	}
	
	/**
	 * \brief calculate a force between two objects based on the distance
	 * 
	 * @param distance
	 * @param var: functions as a scratch book to pass multiple in/output 
	 * variables
	 * between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var, 
			AspectInterface first, AspectInterface second)
	{
		/*
		 * If distance is negative, apply the repulsive force.
		 * Otherwise, return a zero vector. A small overlap is allowed to
		 * prevent objects bouncing in equilibrium 
		 */
		if ( var.getDistance() < -0.001 )
		{
			double kn;
			if( first != null && second != null &&
					first.isAspect( AspectRef.youngsModulus) &&
					first.isAspect( AspectRef.poissonRatio ) &&
					second.isAspect( AspectRef.youngsModulus) &&
					second.isAspect( AspectRef.poissonRatio ) ) {
				double a = first.getDouble(AspectRef.youngsModulus);
				double b = first.getDouble(AspectRef.youngsModulus);
				double eeff = 1 / (
						( (1-Math.pow( first.getDouble( AspectRef.poissonRatio ),2 ) ) / a) +
						( (1-Math.pow( second.getDouble( AspectRef.poissonRatio ),2 ) ) / b) );
				/* alternate form:
				Double eeff = a * b / ( -a * Math.pow(second.getDouble( AspectRef.poissonRatio ),2 )
						+ a -b* Math.pow(first.getDouble( AspectRef.poissonRatio ),2 ) + b );
				 */
				kn = 1.33333333 * Math.sqrt(var.radiusEffective) * eeff;
			}
			else {
				kn = 1.33333333 * Math.sqrt(var.radiusEffective) * forceScalar();
			}
			
			double c = kn * Math.pow(-var.getDistance(), 1.5 );
			/* dP is overwritten here. */
			Vector.normaliseEuclidEqualsUnchecked( var.interactionVector, c );
			return var;
		}
		/* dP is not overwritten here. */
		Vector.setAll(var.interactionVector, 0.0);
		return var;
	}
}
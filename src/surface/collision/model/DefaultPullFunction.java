package surface.collision.model;

import org.w3c.dom.Element;

import aspect.AspectInterface;
import dataIO.XmlHandler;
import idynomics.Global;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Settable;
import surface.collision.CollisionFunction;
import surface.collision.CollisionVariables;

/**
 * default pull CollisionFunction
 */
public class  DefaultPullFunction implements CollisionFunction
{
	private double _forceScalar = Global.pull_scalar;
	
	/**
	 * Implementation of the Instantiatable interface
	 * FIXME currently not settable from xml yet
	 */
	public void instantiate(Element xmlElement, Settable parent)
	{
		Double forceScalar = XmlHandler.gatherDouble(xmlElement, 
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
	 * variables between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var,
			AspectInterface first, AspectInterface second)
	{
		/*
		 * If distance is in the range, apply the pull force.
		 * Otherwise, return a zero vector. A small distance is allowed to
		 * prevent objects bouncing in equilibrium 
		 */
		if ( var.distance > 0.001 && var.distance < var.margin ) 
		{
			/* Linear. */
			double c = Math.abs(this._forceScalar * var.distance);
			/* dP is overwritten here. */
			Vector.normaliseEuclidEquals(var.interactionVector, c);
			return var;
		} 
		Vector.setAll(var.interactionVector, 0.0);
		return var;
	}
}
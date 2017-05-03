package surface;

import org.w3c.dom.Element;

import agent.Agent;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Settable;
import utility.Helper;

/**
 * \brief CollisionFunctions are used to shape and scale the physical
 * interactions between two objects
 */
public interface CollisionFunction extends Instantiable
{
	/**
	 * \brief return the currently set force scalar for this CollisionFunction
	 * 
	 * @return double force scalar
	 */
	public double forceScalar();
	
	/**
	 * \brief calculate a force between two objects based on the distance
	 * @param neighbour 
	 * @param agent 
	 * 
	 * @param distance
	 * @param var: functions as a scratch book to pass multiple in/output 
	 * variables between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var, Agent agent, Agent neighbour);

	/**
	 * default pull CollisionFunction
	 */
	public CollisionFunction DefaultPullFunction = new CollisionFunction()
	{
		private double _forceScalar;
		
		/**
		 * Implementation of the Instantiatable interface
		 */
		public void instantiate(Element xmlElement, Settable parent)
		{
			this._forceScalar = Double.valueOf((String) Helper.setIfNone(
					XmlHandler.gatherAttribute(xmlElement, XmlRef.forceScalar), 
					"-2.0" ) );
			Log.out(Tier.BULK, "initiating DefaultPullFunction");
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
				Agent agent, Agent neighbour)
		{
			if (neighbour == null) {
				/*
				 * If distance is in the range, apply the pull force.
				 * Otherwise, return a zero vector. A small distance is allowed
				 * to prevent objects bouncing in equilibrium 
				 */
				if ( var.distance > 0.0 && var.distance < var.pullRange ) 
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
			else
			{
				/*
				 * If distance is in the range, apply the pull force.
				 * Otherwise, return a zero vector. A small distance is allowed
				 * to prevent objects bouncing in equilibrium 
				 */
				if ( var.distance > 0.0 && var.distance < var.pullRange ) 
				{
					/* note converting from µm to si */
					agent.set("currentDistance", var.distance);
					agent.event("resolveForce", neighbour);
					/* note converting from si to kg * µm /s2 */
					double c = agent.getDouble("scaledForce");
					/* dP is overwritten here. */
					Vector.normaliseEuclidEquals(var.interactionVector, c);
					return var;
				} 
				Vector.setAll(var.interactionVector, 0.0);
				return var;
			}
		}
	};
	
	/**
	 * default push CollisionFunction
	 */
	public CollisionFunction DefaultPushFunction = new CollisionFunction()
	{
		private double _forceScalar;
		
		/**
		 * Implementation of the Instantiatable interface
		 */
		public void instantiate(Element xmlElement, Settable parent)
		{
			this._forceScalar = Double.valueOf((String) Helper.setIfNone(
					XmlHandler.gatherAttribute(xmlElement, XmlRef.forceScalar), 
					"1e20" ) );
			Log.out(Tier.BULK, "initiating DefaultPushFunction");
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
				Agent agent, Agent neighbour)
		{
			/*
			 * If distance is negative, apply the repulsive force.
			 * Otherwise, return a zero vector. A small overlap is allowed to
			 * prevent objects bouncing in equilibrium 
			 */
			if ( var.distance < -0.001 ) 
			{
				/* Linear. */
//				double c = Math.abs( this._forceScalar * var.distance );
				/* two component, better force scaling for low vs high overlap */
				double c = Math.abs( this._forceScalar * (var.distance + var.distance ) ); //* var.distance * 1e1 ) );
				/* dP is overwritten here. */
				Vector.normaliseEuclidEquals( var.interactionVector, c );

				return var;
			}
			/* dP is not overwritten here. */
			Vector.setAll(var.interactionVector, 0.0);
			return var;
		}

	};
}


package surface.collision;


import aspect.AspectInterface;
import instantiable.Instantiable;


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
	 * 
	 * @param distance
	 * @param var: functions as a scratch book to pass multiple in/output 
	 * variables between methods
	 * @return force vector
	 */
	public CollisionVariables interactionForce(CollisionVariables var,
			AspectInterface first, AspectInterface second);
}


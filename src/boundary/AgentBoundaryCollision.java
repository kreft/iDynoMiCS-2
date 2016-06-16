package boundary;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
// NOTE Rob [15June2016]: Not sure if we'll end up using this in the end, still
// just trying to work things out.
public enum AgentBoundaryCollision
{
	/**
	 * Push the agent back into the compartment.
	 */
	PUSH_BACK,
	/**
	 * Remove the agent from the compartment, putting it into the boundary's
	 * departure lounge.
	 */
	DEPART,
	/**
	 * Register the agent for removal from the compartment: for modelling,
	 * e.g. lysis.
	 */
	REGISTER_REMOVE;
}

package generalInterfaces;

import agent.Agent;

//TODO sort out the best way of handling copyable and duplicable objects
public interface Duplicable {

	/**
	 * return a deep copy (Exact duplicate) all fields in the resulting copy
	 * all fields must be dereferenced, new copy objects are created for any 
	 * referenced objects inside the parent object. With the exception of
	 * immutable objects. Yet it allows to set ownership to a new agent.
	 * @return
	 */
	public Object copy(Agent agent);
}

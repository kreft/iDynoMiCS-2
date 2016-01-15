package agent;

import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Node;

import dataIO.XmlLoad;
import agent.state.PrimaryState;
import agent.state.State;

public class Species implements StateObject 
{
	/**
	 * The states HashMap stores all primary and secondary states.
	 */
	protected HashMap<String, State> _states = new HashMap<String, State>();
	
	/**
	 * The speciesModules List contains all Species modules incorporated in this
	 * Species(module).
	 */
	protected LinkedList<Species> speciesModules = new LinkedList<Species>();
	
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Species() {
	}
	
	public Species(Node xmlNode) {
		XmlLoad.loadStates(this, xmlNode);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public boolean isLocalState(String name)
	{
		return _states.containsKey(name) ? true : false;
	}
	
	public boolean isGlobalState(String name)
	{
		if (_states.containsKey(name))
			return true;
		else
			for (Species m : speciesModules)
				if(m.isGlobalState(name) == true)
					return true;
		
		return false;
			
		// update this method if a higher order StateObject class is created
	}
	
	/**
	 * \brief general getter method for any Agent state stored in this species 
	 * module
	 * @param name
	 * 			name of the state (String)
	 * @return Object of the type specific to the state
	 */
	public State getState(String name)
	{
		if (isLocalState(name))
			return _states.get(name);
		else
			for (Species m : speciesModules)
				if(m.isGlobalState(name) == true)
					return m.getState(name);
		
		System.out.println("Warning: could not find state: " + name);
		return null;
	}
	
	
	/**
	 * \brief general setter method for any Agent state
	 * @param name
	 * 			name of the state (String)
	 * @param state
	 * 			Object that contains the value of the state.
	 */
	public void setState(String name, State state)
	{
		_states.put(name, state);
	}
	
	public void setPrimary(String name, Object state)
	{
		State aState = new PrimaryState();
		aState.set(state);
		_states.put(name, aState);
	}

	/**
	 * set should be able to handle any type of state you throw at it.
	 * @param name
	 * @param state
	 */
	public void set(String name, Object state)
	{
		if (state instanceof State)
			setState(name,(State) state);
		else 
			setPrimary(name, state);
	}
	
	public void addSpeciesModule(String name)
	{
		//FIXME: Bas [13.01.16] lets be sure we aren't adding a lot of void
		// species here.
		speciesModules.add(SpeciesLib.get(name));
	}

}

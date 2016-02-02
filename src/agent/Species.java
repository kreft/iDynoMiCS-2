package agent;

import java.util.LinkedList;

import org.w3c.dom.Node;

import dataIO.XmlLoad;
import agent.state.State;

public class Species extends AspectRegistry
{
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
	
	public boolean isGlobalState(String name)
	{
		if (_states.containsKey(name))
			return true;
		else
			for (Species m : speciesModules)
				if(m.isGlobalState(name) == true)
					return true;
		return false;
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
	
	public void addSpeciesModule(String name)
	{
		//FIXME: Bas [13.01.16] lets be sure we aren't adding a lot of void
		// species here.
		speciesModules.add(SpeciesLib.get(name));
	}

}

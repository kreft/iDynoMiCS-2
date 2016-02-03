package agent;

import java.util.LinkedList;

import org.w3c.dom.Node;

import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import dataIO.XmlLoad;
import agent.state.State;

/**
 * 
 * @author baco
 *
 */
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
	
	/**
	 * Check whether the state exists for this species or it's species modules. 
	 */
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
		
		Feedback.out(LogLevel.ALL, "Warning: could not find state: " + name);
		return null;
	}
	
	/**
	 * add a species module to be incorporated in this species
	 * FIXME: Bas [13.01.16] lets be sure we aren't adding a lot of void
	 * species here.
	 * @param name
	 */
	public void addSpeciesModule(String name)
	{
		speciesModules.add(SpeciesLib.get(name));
	}
}
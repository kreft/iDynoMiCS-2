package agent;

import java.util.HashMap;

import org.w3c.dom.Node;

import dataIO.XmlLoad;
import agent.activity.*;
import agent.body.Body;
import agent.state.*;
import agent.state.secondary.*;
import grid.CartesianGrid;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.Simulator;

public class Agent implements StateObject
{

	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
    private int uid = ++UNIQUE_ID;
	
	/**
	 * The states HashMap stores all primary and secondary states.
	 */
	protected HashMap<String, State> _states = new HashMap<String, State>();
	
    /**
	 * All activities owned by this Agent and whether they are currently enabled
	 * or disabled.
     */
    protected HashMap<String, Activity> _activities = new HashMap<String, Activity>();

    //FIXME: Bas - dilemma: so we prefer not giving everything all information
    // that is available, however.. agents need to perform their activities we
    // do not know what information is needed for those activities thus how do
    // we do we ensure that the agents can perform all activities without giving
    // them all available information? For now let them have all information 
    // until we have a definite answ     * Used to search neighbors and store newly created agents

    /**
     * Used to fetch species states.
     */
    Species species;
    
    
   // public interface StatePredicate<T> {boolean test(Object s);}
	
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{

	}
	
	public Agent(Node xmlNode)
	{
		XmlLoad.loadStates(this, xmlNode);
		species = SpeciesLib.get((String) get("species"));
	}
	
	public void init()
	{
				
	}


	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief general getter method for any primary Agent state
	 * @param name
	 * 			name of the state (String)
	 * @return Object of the type specific to the state
	 */
	public State getState(String name)
	{
		if (isLocalState(name))
			return _states.get(name);
		else
		{
			System.out.println("Warning: agent state " + name + " not defined.");
			return null;
		}
	}
	
	public boolean isLocalState(String name)
	{
		if (_states.containsKey(name))
			return true;
		else
			return false;
	}
	
	/*
	 * returns object stored in Agent state with name "name". If the state is
	 * not found it will look for the Species state with "name". If this state
	 * is also not found this method will return null.
	 */
	public Object get(String name)
	{
		if (this.isLocalState(name))
			return getState(name).get(this);
		else if (species.isLocalState(name))
			return species.getState(name).get(this);
		else
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
	
	public void setCalculated(String name, CalculatedState.stateExpression state)
	{
		State anonymous = new CalculatedState();
		anonymous.set((CalculatedState.stateExpression) state);
		_states.put(name, anonymous);
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
		else if (state instanceof CalculatedState.stateExpression)
			setCalculated(name,(CalculatedState.stateExpression) state);
		else
			setPrimary(name, state);
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * \brief do time dependent activity.
	 * @param activity
	 * @param timestep
	 */
	public void doActivity(String activity, Double timestep) 
	{
		try
		{
			_activities.get(activity).execute(new Agent[]{this},timestep);
		}
		catch (Exception e) // null pointer exception?
	{
			System.out.println(e.toString());
		}
	}
	
	/**
	 * \brief do time dependent multiple actor activity.
	 * @param activity
	 * @param timestep
	 */
	public void doActivity(String activity, Agent secondActor, Double timestep) 
	{
		try
		{
			_activities.get(activity).execute(new Agent[]{this,secondActor},timestep);
		}
		catch (Exception e) // null pointer exception?
		{
			System.out.println(e.toString());
		}
	}
	
	/*************************************************************************
	 * general methods
	 ************************************************************************/

	/**
	 * \brief: Registers the birth of a new agent with the agentContainer.
	 */
	public void registerBirth(Compartment compartment) {
		compartment.addAgent(this);
	}

	public int UID() {
		return uid;
	}

	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}

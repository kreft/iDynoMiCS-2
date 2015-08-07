package agent;


import idynomics.AgentContainer;

import java.util.HashMap;
import agent.activity.*;
import agent.body.*;
import grid.SpatialGrid;
import utility.Vector;

public class Agent
{
	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
    protected int uid = ++UNIQUE_ID;

	/**
	 * The states HashMap use used to store all non generic Agent variables.
	 */
	protected HashMap<String, Object> _states = new HashMap<String, Object>();
	
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
    // until we have a definite answer to this question.
	
	/**
     * Used to search neighbors and store newly created agents
	 */
    AgentContainer _agents;
	
	/**
     * Used for reaction speeds and growth
	 */
    SpatialGrid _solutes;
	
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{
		
	}
	
	public void init()
	{
				
	}
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief general getter method for any Agent state
	 * @param name
	 * 			name of the state (String)
	 * @return Object of the type specific to the state
	 */
	public Object getState(String name)
	{
		return _states.get(name);
	}

	/**
	 * \brief general setter method for any Agent state
	 * @param name
	 * 			name of the state (String)
	 * @param state
	 * 			Object that contains the value of the state.
	 */
	public void setState(String name, Object state)
	{
		_states.put(name,state);
	}

	/**
	 * @return the total mass of the agent (including all components)
	 */
	public Double getMass()
	{
		return Vector.sum((Double[]) getState("mass"));	
	}

	/**
	 * @return the total volume of the agent
	 */
	public Double getVolume()
	{
		return Vector.dot( (Double[]) getState("mass"), 
				(Double[]) getState("density"));
	}

	/**
	 * FIXME: this method may need some fine tuning in a later stage.
	 * @return true if the agent has a located body.
	 */
	public Boolean isLocated() {
		Body myBody = (Body) getState("Body");
		if (myBody.getMorphologyIndex() == 0)
			return false;
		else
			return true;
	}

	/**
	 * @return the lower corner of bounding box.
	 */
	public float[] getLower() 
	{
		Body myBody = (Body) getState("Body");
		return myBody.coord((Double) getState("radius"));
	}
	
	/**
	 * @return the lower corner of bounding box with added margin.
	 */
	public float[] getLower(double margin) 
	{
		Body myBody = (Body) getState("Body");
		return myBody.coord((Double) getState("radius"),margin);
	}

	/** 
	 * @return the rib length of bounding box.
	 */
	public float[] getDim() 
	{
		Body myBody = (Body) getState("Body");
		return myBody.dimensions((Double) getState("radius"));
	}

	/**
	 * @return the rib length of bounding box with added margin.
	 */
	public float[] getDim(double margin) 
	{
		Body myBody = (Body) getState("Body");
		return myBody.dimensions((Double) getState("radius"),margin);
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
	public void registerBirth() {
		_agents.registerBirth(this);
	}

	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}

package agent;


import idynomics.AgentContainer;

import java.util.HashMap;

import agent.activity.Activity;
import agent.body.Body;
import spatialgrid.SoluteGrid;
import utility.Vect;

public class Agent
{
    /*************************************************************************
	 * General properties/variables
	 ************************************************************************/
	
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
    
    /**
     * Used to search neighbors and store newly created agents
     */
    AgentContainer _agentContainer;
	
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
		return Vect.sum((Double[]) getState("mass"));	
	}

	/**
	 * @return the total volume of the agent
	 */
	public Double getVolume()
	{
		return Vect.sum(Vect.product( (Double[]) getState("mass"), 
				(Double[]) getState("density")));
	}
	

	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * \brief do the activity if this activity is owned by the agent. Note that
	 * the activity itself will check for the prerequisites. 
	 * @param activity
	 */
	public void doActivity(String activity) 
	{
		try
		{
			_activities.get(activity).execute(this);
		}
		catch (Exception e) // null pointer exception?
		{
			System.out.println(e.toString());
		}
	}
	
	public void doActivity(String activity, Agent secondActor) 
	{
		try
		{
			_activities.get(activity).execute(this,secondActor);
		}
		catch (Exception e) // null pointer exception?
		{
			System.out.println(e.toString());
		}
	}
	
	public void step(Double timeStepSize, SoluteGrid[] solutes)
	{
		
	}

	public void registerBirth() {
		_agentContainer.registerBirth(this);
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
	
	//////////// the bounding box of the agent ////////////
	/**
	 * 
	 * @return
	 */
	public float[] getLower() 
	{
		Body myBody = (Body) getState("Body");
		return myBody.coord((Double) getState("radius"));
	}
	
	public float[] getLower(double t) 
	{
		Body myBody = (Body) getState("Body");
		return myBody.coord((Double) getState("radius"),t);
	}
	
	public float[] getUpper() 
	{
		Body myBody = (Body) getState("Body");
		return myBody.upper((Double) getState("radius"));
	}
	
	public float[] getDim() 
	{
		Body myBody = (Body) getState("Body");
		return myBody.dimensions((Double) getState("radius"));
	}
	
	public float[] getDim(double t) 
	{
		Body myBody = (Body) getState("Body");
		return myBody.dimensions((Double) getState("radius"),t);
	}
	

	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}

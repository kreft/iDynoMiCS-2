package agent;

import java.util.HashMap;

import org.w3c.dom.Node;

import xmlpack.XmlLoad;
import agent.activity.*;
import agent.body.Body;
import agent.state.*;
import agent.state.secondary.*;
import grid.CartesianGrid;
import idynomics.AgentContainer;

public class Agent
{

	/**
	 * The uid is a unique identifier created when a new Agent is created via 
	 * the constructor.
	 */
	protected static int UNIQUE_ID = 0;
    protected int uid = ++UNIQUE_ID;
	
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
    // until we have a definite answer to this question.
	
	/**
     * Used to search neighbors and store newly created agents
	 */
    AgentContainer _agents;
	
	/**
     * Used for reaction speeds and growth
	 */
    CartesianGrid _solutes;
    
   // public interface StatePredicate<T> {boolean test(Object s);}
	
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Agent()
	{

	}
	
	public Agent(Node agentNode)
	{
		XmlLoad.loadAgentPrimaries(this, agentNode);
		loadAgentSecondaries();
	}
	
	public void init()
	{
				
	}
	
	public void loadAgentSecondaries()
	{
		if ((boolean) this.get("isLocated"))
		{
			if (((Body) this.get("body")).getMorphologyIndex() == 1)
			{
				this.set("joints",new JointsState());
				this.set("volume",new SimpleVolumeState());
				this.set("radius",new CoccoidRadius());
			}
			this.set("lowerBoundingBox", new LowerBoundingBox());
			this.set("dimensionsBoundingBox", new DimensionsBoundingBox());
		}
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
	public Object getState(String name)
	{
		if (_states.containsKey(name))
			return _states.get(name);
		else
			return null;
	}
	
	public Object get(String name)
	{
		if (_states.containsKey(name))
			return _states.get(name).get();
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
		aState.init(this, state);
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
		{
			State s = (State) state;
			s.setAgent(this); // needed since otherwise the next line can result in errors for secondary states.
			s.init(this, s.get());
			_states.put(name, s);
		}
		else if (state instanceof CalculatedState.stateExpression)
		{
			State anonymous = new CalculatedState();
			anonymous.init(this, (CalculatedState.stateExpression) state);
			_states.put(name, anonymous);
		} 
		else
		{	
		State aState = new PrimaryState();
		aState.init(this, state);
		_states.put(name, aState);
		}
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

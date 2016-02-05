package agent;

import java.util.HashMap;
import java.util.LinkedList;

import agent.event.Event;
import agent.state.Calculated;
import dataIO.Feedback;
import dataIO.Feedback.LogLevel;


/**
 * Work in progress, reworking aspectReg
 * @author baco
 *
 * @param <A>
 */
public class AspectReg<A> {
	
	/**
	 * The recognized aspect types
	 * @author baco
	 *
	 */
	public enum AspectType
	{
		PRIMARY,
		CALCULATED,
		EVENT
	}
	
	/**
	 * The aspect HashMap stores all aspects (primary, secondary states and 
	 * events).
	 */
	protected HashMap<String, Aspect> _aspects = new HashMap<String, Aspect>();
	
	/**
	 * Contains all (sub) modules
	 */
	protected LinkedList<AspectReg<A>> _modules = new LinkedList<AspectReg<A>>();
	
	/**
	 * add an aspect to this registry
	 */
	public void add(String key, A aspect)
	{
		Aspect a = new Aspect(aspect);
		_aspects.put(key, a);
	}
	
	/**
	 * remove aspect from this registry
	 */
	public void remove(String key)
	{
		_aspects.remove(key);
	}
	
	/**
	 * get value if the aspect is a primary or calculated state
	 */
	public Object getValue(AspectReg<A> rootRegistry, String key)
	{
		Aspect a = getAspect(key);
		
    	switch (a.type)
    	{
    	case PRIMARY: return a.aspect;
    	case CALCULATED: return ((Calculated) a.aspect).get(rootRegistry);
    	case EVENT: Feedback.out(LogLevel.CRITICAL, "Attempt to get event" +
    			key + "as Value!");
    	}
    	return null;
	}
	
	/**
	 * perform event
	 * @param initiator
	 * @param compliant
	 * @param timeStep
	 */
	public void doEvent(AspectReg<A> initiator, AspectReg<A>  compliant, 
			double timeStep, String key)
	{
		Aspect a = getAspect(key);

		if (a.type != AspectType.EVENT)
			Feedback.out(LogLevel.CRITICAL, "Attempt to initiate non event "
					+ "aspect" + key + "as event!");
		//((Event) a.aspect).start(initiator, compliant, timeStep);
	}
	
	/**
	 * get local or global aspect (for internal usage).
	 */
	private Aspect getAspect(String key)
	{
		return _aspects.get(key);
		//TODO global aspects
	}
	
	/**
	 * 
	 * @author baco
	 *
	 */
	private class Aspect
	{
		final A aspect;
		final AspectType type;

		/**
		 * Sets the aspect and declares type
		 * @param aspect
		 */
	    public Aspect(A aspect)
	    {
	      this.aspect = aspect;
	      if(aspect instanceof Calculated)
	    	  this.type = AspectType.CALCULATED;
	      else if(aspect instanceof Event)
	    	  this.type = AspectType.EVENT;
	      else
	    	  this.type = AspectType.PRIMARY;
	    }
	} 
}

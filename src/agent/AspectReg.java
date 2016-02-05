package agent;

import java.util.HashMap;
import java.util.LinkedList;

import agent.event.Event;
import agent.state.SecondaryState;
import dataIO.Feedback;
import dataIO.Feedback.LogLevel;
import generalInterfaces.AspectInterface;
import generalInterfaces.Duplicable;
import utility.Copier;


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
	protected LinkedList<AspectInterface> _modules = new LinkedList<AspectInterface>();
	
	/**
	 * returns true if the key is found in the aspect tree
	 */
	public boolean isGlobalAspect(String key)
	{
		if (_aspects.containsKey(key))
			return true;
		else
			for (AspectInterface m : _modules)
				if(m.registry().isGlobalAspect(key) == true)
					return true;
		return false;
	}
	
	/**
	 * add an aspect to this registry
	 */
	public void add(String key, A aspect)
	{
		if (aspect != null)
		{
			Aspect a = new Aspect(aspect);
			_aspects.put(key, a);
		}
	}
	
	/**
	 * same as add but intend is to overwrite
	 */
	public void set(String key, A aspect)
	{
		add(key,aspect);
	}
	
	/**
	 * remove aspect from this registry
	 */
	public void remove(String key)
	{
		_aspects.remove(key);
	}
	
	/**
	 * add a species module to be incorporated in this species
	 * FIXME: Bas [13.01.16] lets be sure we aren't adding a lot of void
	 * species here.
	 * @param name
	 */
	public void addSubModule(String name)
	{
		addSubModule(SpeciesLib.get(name));
	}
	
	public void addSubModule(AspectInterface module)
	{
		_modules.add(module);
	}
	
	/**
	 * get value if the aspect is a primary or calculated state
	 */
	public Object getValue(AspectInterface rootRegistry, String key)
	{
		Aspect a = getAspect(key);
		if (a == null)
			return null;
    	switch (a.type)
    	{
    	case PRIMARY: return a.aspect;
    	case CALCULATED: return ((SecondaryState) a.aspect).get(rootRegistry);
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
	 * TODO: some proper testing
	 */
	public void doEvent(AspectInterface initiator, AspectInterface compliant, 
			double timeStep, String key)
	{
		Aspect a = getAspect(key);

		if (a.type != AspectType.EVENT)
			Feedback.out(LogLevel.CRITICAL, "Attempt to initiate non event "
					+ "aspect" + key + "as event!");
		((Event) a.aspect).start((Agent) initiator, (Agent) compliant, timeStep);
	}
	
	/**
	 * get local or global aspect (for internal usage).
	 * NOTE if multiple aspect registry modules have an aspect with the same key
	 * the first encountered espect with that key will be returned.
	 */
	private Aspect getAspect(String key)
	{
		if (_aspects.containsKey(key))
			return _aspects.get(key);
		else
			for (AspectInterface m : _modules)
				if(m.registry().isGlobalAspect(key) == true)
					return (Aspect) m.registry().getAspect(key);
		
		Feedback.out(LogLevel.BULK, "Warning: could not find aspect: " + key);
		return null;
	}
	
	/**
	 * 
	 */
	public void duplicate(AspectInterface newObj)
	{
		AspectReg<A> duplicate = (AspectReg<A>) newObj.registry();
		duplicate.clear();
		for (String key : _aspects.keySet())
		{
			Aspect a = getAspect(key);
			if (a.aspect instanceof Duplicable)
				duplicate.add(key, (A) ((Duplicable) a.aspect).copy(newObj)); 
			else
				duplicate.add(key, (A) Copier.copy(a.aspect));
		}
		for (AspectInterface m : _modules)
		{
			duplicate.addSubModule(m);
		}
	}

	/**
	 * 
	 */
	public void clear()
	{
		this._aspects.clear();
		this._modules.clear();
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
	      if(aspect instanceof SecondaryState)
	    	  this.type = AspectType.CALCULATED;
	      else if(aspect instanceof Event)
	    	  this.type = AspectType.EVENT;
	      else
	    	  this.type = AspectType.PRIMARY;
	    }
	} 
}

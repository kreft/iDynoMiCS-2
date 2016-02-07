package aspect;

import java.util.HashMap;
import java.util.LinkedList;

import dataIO.Log;
import dataIO.Log.tier;
import generalInterfaces.Quizable;
import utility.Copier;


/**
 * Work in progress, reworking aspectReg
 * @author baco
 *
 * @param <A>
 */
public class AspectReg<A> {
	
	/**
	 * The aspect HashMap stores all aspects (primary, secondary states and 
	 * events).
	 */
	protected HashMap<String, Aspect<?>> _aspects = 
			new HashMap<String, Aspect<?>>();
	
	/**
	 * Contains all (sub) modules
	 */
	protected LinkedList<AspectInterface> _modules = 
			new LinkedList<AspectInterface>();
	
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
		if(_aspects.containsKey(key))
			Log.out(tier.DEBUG, "attempt to add aspect " + key + 
					" which already exists in this aspect registry");
		else
			set(key,aspect);
	}
	
	/**
	 * same as add but intend is to overwrite
	 */
	public void set(String key, A aspect)
	{
		_aspects.put(key, new Aspect<A>(aspect));
	}
	
	/**
	 * remove aspect from this registry
	 */
	public void remove(String key)
	{
		_aspects.remove(key);
	}
	
	/**
	 * Add subModule (implementing AspectInterface)
	 * @param module
	 */
	public void addSubModule(AspectInterface module)
	{
		_modules.add(module);
	}
	
	/**
	 * Add subModule from quizable Library
	 * @param name
	 */
	public void addSubModule(String name, Quizable library)
	{
		addSubModule((AspectInterface) library.get(name));
	}
	
	/**
	 * get value if the aspect is a primary or calculated state
	 */
	public Object getValue(AspectInterface rootRegistry, String key)
	{
		Aspect<?> a = getAspect(key);
		if (a == null)
			return null;
    	switch (a.type)
    	{
    	case PRIMARY: return a.aspect;
    	case CALCULATED: return a.calc.get(rootRegistry);
    	case EVENT: Log.out(tier.CRITICAL, "Attempt to get event" +
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
		Aspect<?> a = getAspect(key);
		if (a == null)
			Log.out(tier.CRITICAL, "Warning: aspepct registry does not"
					+ " contain event:" + key);
		
		else if (a.type != Aspect.aspectClass.EVENT)
			Log.out(tier.CRITICAL, "Attempt to initiate non event "
					+ "aspect" + key + "as event!");
		else
			a.event.start(initiator, compliant, timeStep);
	}
	
	/**
	 * get local or global aspect (for internal usage).
	 * NOTE if multiple aspect registry modules have an aspect with the same key
	 * the first encountered aspect with that key will be returned.
	 */
	private Aspect<?> getAspect(String key)
	{
		if (_aspects.containsKey(key))
			return _aspects.get(key);
		else
			for (AspectInterface m : _modules)
				if(m.registry().isGlobalAspect(key) == true)
					return (Aspect<?>) m.registry().getAspect(key);
		
		Log.out(tier.BULK, "Warning: could not find aspect: " + key);
		return null;
	}
	
	/**
	 * copies all aspects and submodule from donor into this aspect registry
	 */
	@SuppressWarnings("unchecked")
	public void duplicate(AspectInterface donor)
	{
		this.clear();
		AspectReg<?> donorReg = donor.registry();
		for (String key : donorReg._aspects.keySet())
			add(key, (A) Copier.copy(donorReg.getAspect(key).aspect));
		for (AspectInterface m : donorReg._modules)
			addSubModule(m);
	}

	/**
	 * clear all
	 */
	public void clear()
	{
		this._aspects.clear();
		this._modules.clear();
	}
}

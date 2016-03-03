package aspect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import dataIO.Log;
import dataIO.Log.Tier;
import generalInterfaces.Quizable;
import utility.Copier;


/**
 * Work in progress, reworking aspectReg
 * 
 * @param <A>
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AspectReg<A>
{
	/**
	 * The _aspects HashMap stores all aspects (primary, secondary states and 
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
		if ( this._aspects.containsKey(key) )
			return true;
		else
			for ( AspectInterface m : this._modules )
				if ( m.reg().isGlobalAspect(key) )
					return true;
		return false;
	}
	
	/**
	 * add an aspect to this registry
	 */
	public void add(String key, A aspect)
	{
		if ( this._aspects.containsKey(key) )
		{
			Log.out(Tier.DEBUG, "Attempt to add aspect " + key + 
					" which already exists in this aspect registry");
		}
		else
			this._aspects.put(key, new Aspect<A>(aspect));
	}
	
	/**
	 * same as add but intend is to overwrite
	 */
	@SuppressWarnings("unchecked")
	public void set(String key, A aspect)
	{
		Aspect<A> a;
		if ( this._aspects.containsKey(key) )
		{
			a = (Aspect<A>) this._aspects.get(key);
			a.updateAspect(aspect);
		}
		else
			this._aspects.put(key, new Aspect<A>(aspect));
	}
	
	/**
	 * Remove aspect from this registry.
	 */
	public void remove(String key)
	{
		this._aspects.remove(key);
	}
	
	/**
	 * Add subModule (implementing AspectInterface)
	 * 
	 * @param module
	 */
	public void addSubModule(AspectInterface module)
	{
		this._modules.add(module);
	}
	
	/**
	 * Add subModule from quizable Library
	 * 
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
		if ( a == null )
			return null;
		switch (a.type)
		{
		case PRIMARY: return a.aspect;
		case CALCULATED: return a.calc.get(rootRegistry);
		case EVENT: Log.out(Tier.CRITICAL, "Attempt to get event" +
				key + "as Value!");
		}
    	return null;
	}
	
	/**
	 * \brief Get an Aspect's value, assuming it is a primary state.
	 * 
	 * @param key
	 * @return
	 */
	public Object getPrimaryValue(String key)
	{
		Aspect<?> a = getAspect(key);
		if ( a == null )
			return null;
		switch (a.type)
		{
		case PRIMARY: 
			return a.aspect;
		case CALCULATED:
			Log.out(Tier.CRITICAL, "Attempt to get calculated" +
					key + "as primary value!");
			break;
		case EVENT:
			Log.out(Tier.CRITICAL, "Attempt to get event" +
					key + "as primary value!");
			break;
		}
    	return null;
	}
	
	/**
	 * \brief Get a description of the Aspect called by the given key.
	 * 
	 * @param key
	 * @return
	 */
	public String getDescription(String key)
	{
		Aspect<?> a = getAspect(key);
		if ( a == null )
			return null;
		return a.description;
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
		if ( a == null )
			Log.out(Tier.DEBUG, "Warning: aspepct registry does not"
					+ " contain event:" + key);
		
		else if ( a.type != Aspect.AspectClass.EVENT )
		{
			Log.out(Tier.CRITICAL, "Attempt to initiate non event "
					+ "aspect" + key + "as event!");
		}
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
		if ( this._aspects.containsKey(key) )
			return this._aspects.get(key);
		else
			for ( AspectInterface m : this._modules )
				if ( m.reg().isGlobalAspect(key) )
					return (Aspect<?>) m.reg().getAspect(key);
		Log.out(Tier.BULK, "Warning: could not find aspect \"" + key+"\"");
		return null;
	}
	
	/**
	 * copies all aspects and submodule from donor into this aspect registry
	 */
	@SuppressWarnings("unchecked")
	public void duplicate(AspectInterface donor)
	{
		this.clear();
		AspectReg<?> donorReg = donor.reg();
		for (String key : donorReg._aspects.keySet())
			add(key, (A) Copier.copy(donorReg.getAspect(key).aspect));
		for (AspectInterface m : donorReg._modules)
			addSubModule(m);
	}

	/**
	 * Clear all aspects and modules from this registry.
	 */
	public void clear()
	{
		this._aspects.clear();
		this._modules.clear();
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	/**
	 * \brief Compile a list of all aspect names in this registry.
	 * 
	 * @return
	 */
	public List<String> getAllAspectNames()
	{
		LinkedList<String> names = new LinkedList<String>();
		this.addAllAspectNames(names);
		return names;
	}
	
	/**
	 * \brief Helper method for {@link #getAllAspectNames()}.
	 * 
	 * @param names
	 */
	public void addAllAspectNames(List<String> names)
	{
		names.addAll(this._aspects.keySet());
		for ( AspectInterface ai : this._modules )
			ai.reg().addAllAspectNames(names);
	}
}

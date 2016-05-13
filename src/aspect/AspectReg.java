package aspect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.Log.Tier;
import dataIO.XmlLabel;
import generalInterfaces.Quizable;
import utility.Helper;


/**
 * Work in progress, reworking aspectReg
 * 
 * @param <A>
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AspectReg<A>
{
	
	/**
	 * quick fix for xmling
	 * 
	 */
	public String identity;
	
	/**
	 * \brief Recognised aspect types.
	 * 
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
	 */
	public enum AspectClass
	{
		/**
		 * Neither an instance of {@code aspect.Calculated}, nor of
		 * {@code aspect.Event}.
		 */
		PRIMARY,
		/**
		 * An instance of {@code aspect.Calculated}.
		 */
		CALCULATED,
		/**
		 * An instance of {@code aspect.Event}.
		 */
		EVENT
	}

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
	 * FIXME this can go cleaner
	 * @return
	 */
	public String getXml() {
		String out = "";
		for (AspectInterface a : _modules)
		{
			out = out + "<" + XmlLabel.speciesModule + " " + 
					XmlLabel.nameAttribute + "=\"" + a.reg().identity + "\" />\n";
		}
		for (String key : _aspects.keySet())
		{
			out = out + _aspects.get(key).getXml(key);
		}
		return out;
	} 
	
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
	public synchronized void set(String key, A aspect)
	{
		if(_aspects.containsKey(key))
			this.getAspect(key).set(aspect);
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
	public synchronized Object getValue(AspectInterface rootRegistry, String key)
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
	 * perform event
	 * @param initiator
	 * @param compliant
	 * @param timeStep
	 * TODO: some proper testing
	 */
	public synchronized void doEvent(AspectInterface initiator, AspectInterface compliant, 
			double timeStep, String key)
	{
		Aspect<?> a = getAspect(key);
		if ( a == null )
			Log.out(Tier.BULK, "Warning: aspepct registry does not"
					+ " contain event:" + key);
		
		else if ( a.type != AspectReg.AspectClass.EVENT )
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
	@SuppressWarnings("unchecked")
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
	 * Copies all aspects and submodule from donor into this aspect registry.
	 */
	@SuppressWarnings("unchecked")
	public void duplicate(AspectInterface donor)
	{
		this.clear();
		AspectReg<?> donorReg = donor.reg();
		for (String key : donorReg._aspects.keySet())
			add(key, (A) ObjectFactory.copy(donorReg.getAspect(key).aspect));
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
		this.appendAllAspectNamesTo(names);
		return names;
	}
	
	/**
	 * \brief Helper method for {@link #getAllAspectNames()}.
	 * 
	 * @param names
	 */
	public void appendAllAspectNamesTo(List<String> names)
	{
		names.addAll(this._aspects.keySet());
		for ( AspectInterface ai : this._modules )
			ai.reg().appendAllAspectNamesTo(names);
	}
	
	/**
	 * \brief Very general class that acts as a wrapper for other Objects.
	 * 
	 * @param <A> Class of the Aspect.
	 * 
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
	 */
	@SuppressWarnings("hiding")
	private class Aspect<A>
	{
		/**
		 * The object this Aspect wraps.
		 */
		protected A aspect;
		
		/**
		 * The type of object this Aspect wraps.
		 */
		protected AspectReg.AspectClass type;
		
		/**
		 * Direct access field for a {@code Calculated} aspect (to prevent
		 * excessive casting).
		 */
		protected Calculated calc;
		
		/**
		 * Direct access field for an {@code Even} aspect (to prevent excessive
		 * casting).
		 */
		protected Event event;
		
		/**
		 * \brief Construct and Aspect by setting the aspect and declares type
		 * 
		 * @param <A>
		 * @param aspect
		 */
	    public Aspect(A aspect)
	    {
	    	set(aspect);
	    }
	    
	    /**
	     * Set passed object as aspect for existing aspect object
	     * @param aspect
	     */
	    @SuppressWarnings("unchecked")
		public void set(Object aspect)
	    {
	    	this.aspect = (A) aspect;
			if ( this.aspect instanceof Calculated )
			{
				  this.type = AspectReg.AspectClass.CALCULATED;
				  this.calc = (Calculated) this.aspect;
			}
			else if ( this.aspect instanceof Event )
			{
				  this.type = AspectReg.AspectClass.EVENT;
				  this.event = (Event) this.aspect;
			}
			else
			{
				  this.type = AspectReg.AspectClass.PRIMARY;
			}
	    }
	    
	    /**
	     * return the full aspect xml specification of the aspect.
	     * @param key
	     * @return
	     */
	    public String getXml(String key) 
	    {
	    	String out = "";
	    	String simpleName = this.aspect.getClass().getSimpleName();
	    	switch (this.type)
	    	{
	    	case CALCULATED:
	    		out = out + " " + XmlLabel.typeAttribute + "=\"" + "CALCULATED" 
	    				+ "\" " + XmlLabel.classAttribute + "=\"" + simpleName +
	    				"\" " + XmlLabel.inputAttribute + "=\"" + 
	    				Helper.StringAToString(this.calc.input) + "\" />\n";
	    		break;
	    	case EVENT:
	    		out = out + " " + XmlLabel.typeAttribute + "=\"" + "EVENT" 
	    				+ "\" " + XmlLabel.classAttribute + "=\"" + simpleName +
	    				"\" " + XmlLabel.inputAttribute + "=\"" + 
	    				Helper.StringAToString(this.event.input) + "\" />\n";
	    		break;
	    	default:
	    		out = out + ObjectFactory.nodeFactory(this.aspect, 
	    				XmlLabel.aspect, key);
	    	}
			return out;
		}
	}
}

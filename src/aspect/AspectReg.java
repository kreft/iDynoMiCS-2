package aspect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlLabel;
import generalInterfaces.Quizable;
import generalInterfaces.XMLable;
import linearAlgebra.Vector;
import utility.Copier;
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
	public synchronized boolean isGlobalAspect(String key)
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
	public synchronized void add(String key, A aspect)
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
	public synchronized void remove(String key)
	{
		this._aspects.remove(key);
	}
	
	/**
	 * Add subModule (implementing AspectInterface)
	 * 
	 * @param module
	 */
	public synchronized void addSubModule(AspectInterface module)
	{
		this._modules.add(module);
	}
	
	/**
	 * Add subModule from quizable Library
	 * 
	 * @param name
	 */
	public synchronized void addSubModule(String name, Quizable library)
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
			Log.out(Tier.DEBUG, "Warning: aspepct registry does not"
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
	private synchronized Aspect<?> getAspect(String key)
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
	public synchronized void duplicate(AspectInterface donor)
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
	public synchronized void clear()
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
	 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
		
		private Predicate<A> _restriction;
		
		private String _restrictionExplanation;
		
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
	     * TODO work in progress
	     * return partial xml specification of the input object, XMLables are
	     * included as child node, simple objects are include in the value
	     * attribute.
	     * @param obj
	     * @param typeLabel
	     * @param valLabel
	     * @return
	     */
	    public String specString(Object obj, String typeLabel, String valLabel)
	    {
	    	String simpleName = obj.getClass().getSimpleName();
	    	String out = "";
	    	if (obj instanceof XMLable)
    		{
    			XMLable x = (XMLable) obj;
    			out = out + " " + typeLabel + "=\"" + simpleName + "\">\n" + 
    			x.getXml();
    		}
    		else
    		{
		    	switch (simpleName)
	    		{
	    		case "String[]":
	    			out = out + " " + typeLabel + "=\"" + simpleName+ "\" " + valLabel
	    					+ "=\"" + Helper.StringAToString((String[]) obj) 
	    					+ "\"";
	    			break;
	    		default:
	    			out = out + " " + typeLabel + "=\"" + simpleName+ "\" " + valLabel
	    					+ "=\"" + obj.toString() + "\"";
	    		}
    		}
	    	return out;
	    }
	    
	    /**
	     * Return the partial aspect xml specification for a given primary 
	     * aspect object
	     * @param obj
	     * @param prePend
	     * @return
	     */
	    public String primaryFactory(Object obj)
	    {
	    	String out = "";	    	
	    	String simpleName = obj.getClass().getSimpleName();
	    	switch (simpleName)
    		{
    		case "HashMap":
    			@SuppressWarnings("unchecked")
				HashMap<Object,Object> h = (HashMap<Object,Object>) obj;
    			out = out + " type=\"" + simpleName + "\">\n";
    			for(Object hKey : h.keySet())
    			{
    				out = out + "<" + XmlLabel.item + " " +
    						specString(hKey, XmlLabel.keyTypeAttribute,  XmlLabel.keyAttribute) +
    						specString(h, XmlLabel.typeAttribute, XmlLabel.valueAttribute)
    						+ (h instanceof XMLable ? "</" + XmlLabel.item + ">\n" : "/>\n");

    			}
    			out = out + "</" + XmlLabel.aspect + ">\n";
    			break;
    		case "LinkedList":
    			@SuppressWarnings("unchecked")
				LinkedList<Object> l = (LinkedList<Object>) obj;
    			out = out + " type=\"" + simpleName + "\">\n";
    			for(Object o : l)
    			{
    				out = out + "<" + XmlLabel.item + " " +
    						specString(o, XmlLabel.typeAttribute,
    						XmlLabel.valueAttribute) + (o instanceof XMLable ? 
    						"</" + XmlLabel.item + ">\n" : "/>\n");

    			}
    			out = out + "</" + XmlLabel.aspect + ">\n";
    			break;
    		default:
    			out = out + specString(obj, 
    					XmlLabel.typeAttribute, XmlLabel.valueAttribute)
    					+ (obj instanceof XMLable ? "</" + XmlLabel.item + ">\n"
    					: "/>\n");
    		}
    		
	    	return out;
	    }
	    
	    /**
	     * return the full aspect xml specification of the aspect.
	     * @param key
	     * @return
	     */
	    public String getXml(String key) 
	    {
	    	String out = "<" + XmlLabel.aspect + " name=\"" + key + "\"";
	    	String simpleName = this.aspect.getClass().getSimpleName();
	    	switch (this.type)
	    	{
	    	case CALCULATED:
	    		out = out + " type=\"" + "CALCULATED" + "\" class=\"" + 
	    				simpleName + "\" input=\"" + 
	    				Helper.StringAToString(this.calc.input) + "\" />\n";
	    		break;
	    	case EVENT:
	    		out = out+  " type=\"" + "EVENT" + "\" class=\"" + 
	    				simpleName + "\" input=\"" + 
	    				Helper.StringAToString(this.event.input) + "\" />\n";
	    		break;
	    	default:
	    		out = out + primaryFactory(this.aspect);
	    	}

			return out;
		}

		/**
	     * \brief TODO
	     * 
	     * @param newAspect
	     */
	    protected void updateAspect(A newAspect)
	    {
	    	if ( newAspect.getClass() != this.aspect.getClass() )
	    	{
	    		// TODO safety
	    	}
	    	/* Check that the restriction is satisfied, if there is one. */
	    	if ( this.isRestrictionBroken() )
	    	{
	    		if ( this._restrictionExplanation == null )
	    			Log.out(Tier.CRITICAL, "Aspect restriction broken!");
	    		else
	    			Log.out(Tier.CRITICAL, this._restrictionExplanation);
	    	}
	    	this.aspect = newAspect;
	    	/* Update the direct access fields, if appropriate. */
	    	if ( this.type == AspectReg.AspectClass.CALCULATED )
	    		this.calc = (Calculated) this.aspect;
	    	if ( this.type == AspectReg.AspectClass.EVENT )
	    		this.event = (Event) this.aspect;
	    }
	    
	    public void setRestriction(Predicate<A> restriction)
	    {
	    	this._restriction = restriction;
	    }
	    
	    public void setRestiction(Predicate<A> restriction, String explanation)
	    {
	    	this.setRestriction(restriction);
	    	this._restrictionExplanation = explanation;
	    }
	    
	    public boolean isRestrictionBroken()
	    {
	    	if ( this._restriction == null )
	    		return false;
	    	return ( ! this._restriction.test(this.aspect) );
	    }
	    
	    public String getRestrictionExplanation()
	    {
	    	return this._restrictionExplanation;
	    }
	}

}

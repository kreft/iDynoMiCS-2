package aspect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dataIO.Log;
import dataIO.ObjectFactory;
import idynomics.Idynomics;
import dataIO.Log.Tier;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;
import nodeFactory.NodeConstructor;
import nodeFactory.primarySetters.Pile;


/**
 * Work in progress, reworking aspectReg
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AspectReg
{
	
	/**
	 * quick fix for xmling
	 * 
	 */
	protected String _identity;
	
	/**
	 * The _aspects HashMap stores all aspects (primary, secondary states and 
	 * events).
	 */
	protected HashMap<String, Aspect> _aspects = 
											new HashMap<String, Aspect>();
	
	/**
	 * all (sub) modules
	 */
	protected Pile<String> _subModules = new Pile<String>(String.class,
			XmlRef.nameAttribute, XmlRef.modules, XmlRef.speciesModule );
	
	/**
	 * get the identity of this aspectReg
	 * @return
	 */
	public String getIdentity() {
		return _identity;
	}

	/**
	 * set the identity of this apspectReg
	 * @param _identity
	 */
	public void setIdentity(String _identity) {
		this._identity = _identity;
	}
		
	/**
	 * returns true if the key is found in the aspect tree
	 */
	public boolean isGlobalAspect(String key)
	{
		if ( this._aspects.containsKey(key) )
			return true;
		else
			for ( AspectInterface m : this.getSubModules() )
				if ( m.reg().isGlobalAspect(key) )
					return true;
		return false;
	}
	
	/**
	 * add an aspect to this registry
	 */
	public void add(String key, Object aspect)
	{
		if ( aspect == null || key == null)
			Log.out(Tier.NORMAL, "Received null input, skipping aspect.");
		else
		{
			if ( this._aspects.containsKey(key) )
			{
				Log.out(Tier.DEBUG, "Attempt to add aspect " + key + 
						" which already exists in this aspect registry");
			}
			else
				this._aspects.put(key, new Aspect(aspect, key, this) );
		}
	}
	
	public void addInstatiatedAspect(String key, Aspect aspect)
	{
		if ( aspect == null || key == null)
			Log.out(Tier.NORMAL, "Received null input, skipping aspect.");
		else
		{
			if ( this._aspects.containsKey(key) )
			{
				Log.out(Tier.DEBUG, "Attempt to add aspect " + key + 
						" which already exists in this aspect registry");
			}
			else
				this._aspects.put(key, aspect );
		}
	}
	
	/**
	 * same as add but intend is to overwrite
	 */
	public void set(String key, Object aspect)
	{
		if ( this._aspects.containsKey(key) )
			this.getAspect(key).set(aspect, key);
		else
			this._aspects.put(key, new Aspect(aspect, key, this) );
	}
	
	/**
	 * Remove aspect from this registry.
	 * 
	 * @param key Name of the aspect to remove.
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
		this._subModules.add(module.reg().getIdentity());
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param module
	 */
	public void removeSubmodule(String module) 
	{
		this._subModules.remove(module);
	}
	
	/**
	 * \brief Add a subModule from an AspectInterface Library
	 * 
	 * @param name
	 * @param library
	 */
	public void addSubModule(String name, AspectInterface library)
	{
		addSubModule( (AspectInterface) library.getValue(name) );
	}
	
	public LinkedList<AspectInterface> getSubModules()
	{
		LinkedList<AspectInterface> modules = new LinkedList<AspectInterface>();
		for (String s : this.getSubModuleNames() )
			modules.add( Idynomics.simulator.speciesLibrary.get(s) );
		return modules;
	}
	
	public Pile<String> getSubModuleNames()
	{
		return this._subModules;
	}
	
	/**
	 * get value if the aspect is a primary or calculated state
	 */
	public Object getValue( AspectInterface rootRegistry, 
			String key )
	{
		Aspect a = getAspect(key);
		if ( a == null )
			return null;
		switch (a.type)
		{
		case PRIMARY: return a.aspect;
		case CALCULATED: return a.calc.get(rootRegistry);
		case EVENT: Log.out(Tier.CRITICAL, "Attempt to get event " +
				key + " as Value!");
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
	public void doEvent(AspectInterface initiator, 
			AspectInterface compliant, double timeStep, String key)
	{
		Tier level = Tier.BULK;
		Aspect a = getAspect(key);
		if ( a == null )
		{
			if ( Log.shouldWrite(level) )
			{
				Log.out(Tier.BULK, "Warning: aspect registry does not"
						+ " contain event:" + key);
			}
		}
		else if ( a.type != Aspect.AspectClass.EVENT )
		{
			Log.out(Tier.CRITICAL, "Attempt to initiate non event "
					+ " aspect " + key + " as event!");
		}
		else
			a.event.start(initiator, compliant, timeStep);
	}
	
	/**
	 * get local or global aspect (for internal usage).
	 * NOTE if multiple aspect registry modules have an aspect with the same key
	 * the first encountered aspect with that key will be returned.
	 */
	private Aspect getAspect(String key)
	{
		Tier level = Tier.BULK;
		if ( this._aspects.containsKey(key) )
			return this._aspects.get(key);
		else
			for ( AspectInterface m : this.getSubModules() )
				if ( m.reg().isGlobalAspect(key) )
					return (Aspect) m.reg().getAspect(key);
		if ( Log.shouldWrite(level) )
			Log.out(Tier.BULK, "Warning: could not find aspect \"" + key+"\"");
		return null;
	}
	
	/**
	 * Copies all aspects and submodule from donor into this aspect registry.
	 */
	public void duplicate(AspectInterface donor)
	{
		this.clear();
		AspectReg donorReg = donor.reg();
		for ( String key : donorReg._aspects.keySet() )
			add( key, (Object) ObjectFactory.copy(
					donorReg.getAspect(key).aspect ) );
		for (AspectInterface m : donorReg.getSubModules() )
			addSubModule(m);
	}

	/**
	 * Clear all aspects and modules from this registry.
	 */
	public void clear()
	{
		this._aspects.clear();
		this._subModules.clear();
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
		names.addAll(this._aspects.keySet() );
		for ( AspectInterface ai : this.getSubModules() )
			ai.reg().appendAllAspectNamesTo(names);
	}
	
	/**
	 * TODO
	 * @return
	 */
	public Set<String> getLocalAspectNames()
	{
		return this._aspects.keySet();
	}
	
	/**
	 * TODO
	 * @param key
	 * @return
	 */
	public ModelNode getAspectNode(String key)
	{
		return this._aspects.get(key).getNode();
	}
	
	/**
	 * TODO
	 * @param constructor
	 * @return
	 */
	public ModelNode getModuleNode(NodeConstructor constructor) {
		ModelNode modelNode = new ModelNode(XmlRef.speciesModule,constructor);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this.getIdentity());
		modelNode.add(new ModelAttribute(XmlRef.nameAttribute, 
				this.getIdentity(), null, true ) );
		
		return modelNode;
	}

	/**
	 * TODO
	 * @param key
	 * @param newKey
	 */
	public void rename(String key, String newKey )
	{
		Object a = (Object) this.getAspect(key);
		this.remove(key);
		this.add(newKey, a);
	}

}

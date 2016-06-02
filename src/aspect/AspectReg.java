package aspect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import agent.Body;
import aspect.calculated.StateExpression;
import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.ObjectRef;
import dataIO.Log.Tier;
import dataIO.XmlRef;
import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import nodeFactory.primarySetters.LinkedListSetter;
import surface.Point;
import utility.Helper;


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
	public String _identity;
	
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
	protected HashMap<String, Aspect> _aspects = 
											new HashMap<String, Aspect>();
	
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
	public void add(String key, Object aspect)
	{
		if ( this._aspects.containsKey(key) )
		{
			Log.out(Tier.DEBUG, "Attempt to add aspect " + key + 
					" which already exists in this aspect registry");
		}
		else
			this._aspects.put(key, new Aspect(aspect, key, this) );
	}
	
	/**
	 * same as add but intend is to overwrite
	 */
	public void set(String key, Object aspect)
	{
		if(_aspects.containsKey(key) )
			this.getAspect(key).set(aspect, key);
		else
			this._aspects.put(key, new Aspect(aspect, key, this) );
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
	public void addSubModule(String name, AspectInterface library)
	{
		addSubModule( (AspectInterface) library.getValue(name) );
	}
	
	public LinkedList<AspectInterface> getSubModules()
	{
		return _modules;
	}
	
	/**
	 * get value if the aspect is a primary or calculated state
	 */
	public synchronized Object getValue( AspectInterface rootRegistry, 
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
	public synchronized void doEvent(AspectInterface initiator, 
			AspectInterface compliant, double timeStep, String key)
	{
		Aspect a = getAspect(key);
		if ( a == null )
			Log.out(Tier.BULK, "Warning: aspepct registry does not"
					+ " contain event:" + key);
		
		else if ( a.type != AspectReg.AspectClass.EVENT )
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
		if ( this._aspects.containsKey(key) )
			return this._aspects.get(key);
		else
			for ( AspectInterface m : this._modules )
				if ( m.reg().isGlobalAspect(key) )
					return (Aspect) m.reg().getAspect(key);
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
		names.addAll(this._aspects.keySet() );
		for ( AspectInterface ai : this._modules )
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
		modelNode.requirement = Requirements.ZERO_TO_MANY;
		
		modelNode.add(new ModelAttribute(XmlRef.nameAttribute, 
				this._identity, null, true ) );
		
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
	
	/**
	 * \brief Very general class that acts as a wrapper for other Objects.
	 * 
	 * 
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
	 */
	public class Aspect implements NodeConstructor
	{
		/**
		 * The object this Aspect wraps.
		 */
		protected Object aspect;
		
		/**
		 * 
		 */
		protected String key;
		
		/**
		 * 
		 */
		protected AspectReg registry;
		
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
		 * @param aspect
		 * @param key
		 * @param registry
		 */
	    public Aspect(Object aspect, String key, AspectReg registry)
	    {
	    	this.registry = registry;
	    	set(aspect, key);
	    }
	    
	    public Aspect(AspectReg registry)
	    {
	    	this.registry = registry;
	    }
	    
	    /**
	     * Set passed object as aspect for existing aspect object
	     * @param aspect
	     */
		public void set(Object aspect, String key)
	    {
	    	this.aspect = (Object) aspect;
	    	this.key = key;
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
			else if ( this.aspect == null )
			{
				Log.out(Tier.NORMAL, "attempt to load null object " + key +
						" as aspect, abort");
			}
			else
			{
				  this.type = AspectReg.AspectClass.PRIMARY;
			}
	    }

		/**
		 * Get the ModelNode object for this Aspect object
		 * @return ModelNode
		 */
		@SuppressWarnings("unchecked")
		@Override
		public ModelNode getNode() 
		{
			ModelNode modelNode = new ModelNode(XmlRef.aspect, this);
			modelNode.requirement = Requirements.ZERO_TO_FEW;
			modelNode.title = this.key;
			
			modelNode.add(new ModelAttribute(XmlRef.nameAttribute, 
					this.key, null, true ) );
			
			String simpleName = this.aspect.getClass().getSimpleName();
			
			/* Primaries */
			if(this.type.equals(AspectReg.AspectClass.PRIMARY) )
			{
				modelNode.add(new ModelAttribute(XmlRef.typeAttribute, 
						this.type.toString(), null, false ) );
				
				modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
						simpleName, null, false ) );
				
				
		    	switch (simpleName)
				{
				case "HashMap":
					HashMap<Object,Object> h = (HashMap<Object,Object>) aspect;
//					for (Object k : h.keySet() )
//						modelNode.add(HashMapNode(k) );
					break;
				case "LinkedList":
//					modelNode.add(ObjectFactory.nodeFactoryInner(aspect) );
					// TODO work in progress
					LinkedList<Object> linkedList = (LinkedList<Object>) aspect;
					for (Object o : linkedList)
						modelNode.add(new LinkedListSetter(o).getNode() );
					break;
				case "Body":
					Body myBody = (Body) aspect;
					for (Point p : myBody.getPoints() )
						modelNode.add(p.getNode() );
					break;
				default:
					if (aspect instanceof NodeConstructor)
					{
						NodeConstructor x = (NodeConstructor) aspect;
						modelNode.add(x.getNode() ); 
					}
					else
					{
						modelNode.add(new ModelAttribute(XmlRef.valueAttribute, 
								ObjectFactory.stringRepresentation(aspect), 
								null, true ) );
					}
				}
			}
			/* events and calculated */
			else
			{
				modelNode.add(new ModelAttribute(XmlRef.typeAttribute, 
						this.type.toString(), null, false ) );

				modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
						simpleName, null , false ) );
				
				if (simpleName.equals( StateExpression.class.getSimpleName() ) )
				{
					modelNode.add(new ModelAttribute(XmlRef.inputAttribute, 
							( (Calculated) this.aspect ).getInput()[0], 
							null, false ) );
				}
			}

			return modelNode;
		}
		
		/**
		 * Get the ModelNode object for a Hashmap TODO to be replaced
		 * @return ModelNode
		 */
		@SuppressWarnings("unchecked")
		public ModelNode HashMapNode(Object key) 
		{
			HashMap<Object,Object> h = (HashMap<Object,Object>) aspect;
			ModelNode modelNode = new ModelNode("item", this);
			modelNode.requirement = Requirements.ZERO_TO_MANY;
			
			modelNode.add(new ModelAttribute(XmlRef.classAttribute, 
					h.get(key).getClass().getSimpleName(), null, false ) );
			
			return modelNode;
		}

		/**
		 * Load and interpret the values of the given ModelNode to this 
		 * NodeConstructor object
		 * @param node
		 */
		@Override
		public void setNode(ModelNode node) 
		{
			if(node.getAttribute(XmlRef.valueAttribute) != null)
			{
				this.set(ObjectFactory.loadObject(
						node.getAttribute(XmlRef.valueAttribute).value, 
						node.getAttribute(XmlRef.typeAttribute).value,
						node.getAttribute(XmlRef.classAttribute).value), key);
			}
			
			for(ModelNode n : node.childNodes)
				n.constructor.setNode(n);
		}

		// TODO build up from general.classLib rather than hard code
		/**
		 * Create a new minimal object of this class and return it, used by the gui
		 * to add new
		 * @return NodeConstructor
		 */
		@Override
		public NodeConstructor newBlank() {
			String name = "";
			name = Helper.obtainInput(name, "aspect name");
			String type = Helper.obtainInput( Helper.enumToString(
					AspectReg.AspectClass.class).split(" "), 
					"aspect type", false);
			String pack = "";
			String classType = "";
			switch (type)
	    	{
	    	case "CALCULATED":
	    		pack = "aspect.calculated.";
	    		classType = Helper.obtainInput( Helper.ListToArray(
	    				Idynomics.xmlPackageLibrary.getAll(pack) ), 
	    				"aspect class", false);
				
	    		break;
	    	case "EVENT": 
	    		pack = "aspect.event.";
	    		classType = Helper.obtainInput( Helper.ListToArray(
	    				Idynomics.xmlPackageLibrary.getAll(pack) ), 
	    				"aspect class", false);
				
	    		break;
			default:
				classType = Helper.obtainInput( ObjectRef.getAllOptions(), 
						"Primary type", false);
				break;
			}
			registry.add( name, ObjectFactory.loadObject( Helper.obtainInput(
					"", "Primary value"), type, classType) );
			return registry.getAspect(name);
		}

		/**
		 * return the default XMLtag for the XML node of this object
		 * @return String xmlTag
		 */
		@Override
		public String defaultXmlTag() {
			// TODO Auto-generated method stub
			return XmlRef.aspect;
		}
	}
}

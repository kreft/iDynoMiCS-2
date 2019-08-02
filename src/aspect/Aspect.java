package aspect;

import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Element;
import agent.Body;
import aspect.calculated.StateExpression;
import dataIO.Log;
import dataIO.ObjectFactory;
import idynomics.Idynomics;
import dataIO.Log.Tier;
import instantiable.Instance;
import instantiable.Instantiable;
import referenceLibrary.ClassRef;
import referenceLibrary.ObjectRef;
import referenceLibrary.PackageRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import settable.primarySetters.HashMapSetter;
import settable.primarySetters.LinkedListSetter;
import surface.Point;
import utility.Helper;

/**
 * \brief Very general class that acts as a wrapper for other Objects.
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Aspect implements Instantiable, Settable
{
	/**
	 * \brief Recognized aspect types.
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
	protected AspectClass type;
	
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

	private Settable _parentNode;
	
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
    
    public Aspect()
    {
    	
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
			  this.type = Aspect.AspectClass.CALCULATED;
			  this.calc = (Calculated) this.aspect;
		}
		else if ( this.aspect instanceof Event )
		{
			  this.type = Aspect.AspectClass.EVENT;
			  this.event = (Event) this.aspect;
		}
		else if ( this.aspect == null )
		{
			Log.out(Tier.NORMAL, "attempt to load null object " + key +
					" as aspect, abort");
		}
		else
		{
			  this.type = Aspect.AspectClass.PRIMARY;
		}
    }
	
	
	public String getKey() 
	{
		return this.key;
	}


	/**
	 * Get the ModelNode object for this Aspect object
	 * @return ModelNode
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Module getModule() 
	{
		Module modelNode = new Module(XmlRef.aspect, this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this.key);
		
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
				this.key, null, true ) );
		
		String simpleName = this.aspect.getClass().getSimpleName();
		
		/* Primaries */
		if(this.type.equals(Aspect.AspectClass.PRIMARY) )
		{
			/* class name */
			modelNode.add(new Attribute(XmlRef.classAttribute, 
					simpleName, null, false ) );
			
			/* handle special cases TODO: do we want to maintain support for 
			 * these special cases? */
			if ( simpleName.equals(ClassRef.simplify(ClassRef.hashMap)))
			{
				HashMap<Object,Object> h = (HashMap<Object,Object>) aspect;
				for (Object k : h.keySet() )
					modelNode.add(new HashMapSetter<Object,Object>(
							h.get(k), k, h).getModule() );
			}
			else if ( simpleName.equals(ClassRef.simplify(ClassRef.linkedList)))
			{
				LinkedList<Object> linkedList = (LinkedList<Object>) aspect;
				for (Object o : linkedList)
					modelNode.add(new LinkedListSetter<Object>(
							o, linkedList ).getModule() );
			}
			else
			{
				if (aspect instanceof Settable)
				{
					Settable x = (Settable) aspect;
					modelNode.add(x.getModule() ); 
				}
				else
				{
					modelNode.add(new Attribute(XmlRef.valueAttribute, 
							ObjectFactory.stringRepresentation(aspect), 
							null, true ) );
				}
			}
		}
		/* events and calculated */
		else
		{
			if ( Idynomics.xmlPackageLibrary.has(simpleName))
				modelNode.add(new Attribute(XmlRef.classAttribute, 
						simpleName, null , false ) );
			else
				modelNode.add(new Attribute(XmlRef.classAttribute, 
						this.aspect.getClass().getName(), null , false ) );
				
			if (simpleName.equals( StateExpression.class.getSimpleName() ) )
			{
				modelNode.add(new Attribute(XmlRef.inputAttribute, 
						( (Calculated) this.aspect ).getInput(), 
						null, false ) );
			}
		}

		return modelNode;
	}

	/**
	 * Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object
	 * @param node
	 */
	@Override
	public void setModule(Module node) 
	{
		if ( node.getAttribute(XmlRef.valueAttribute) != null )
		{
			switch ( this.type )
	    	{
	    	case CALCULATED:
	    		this.set( Calculated.instanceFromString(
	    				node.getAttribute(XmlRef.classAttribute).getValue(), 
	    				node.getAttribute(XmlRef.inputAttribute).getValue()), key);
	    	case EVENT: 
	    		this.set( Instance.getNew(null, null, 
	    				node.getAttribute(XmlRef.classAttribute).getValue()), key);
	    	case PRIMARY:
			default:
				this.set( ObjectFactory.loadObject(
						node.getAttribute(XmlRef.valueAttribute).getValue(), 
						node.getAttribute(XmlRef.classAttribute).getValue()), key);
			}
		}
		Settable.super.setModule(node);
	}

	// TODO build up from general.classLib rather than hard code
	/**
	 * Create a new minimal object of this class and return it, used by the gui
	 * to add new
	 * @return NodeConstructor
	 */
	@Override
	public void instantiate(Element xmlElem, Settable parent) {
		String name = "";
		name = Helper.obtainInput(name, "aspect name");
		/* if name is canceled */
//			if ( name == null )
//				return null;
		String type = Helper.obtainInput( Helper.enumToString(
				Aspect.AspectClass.class).split(" "), 
				"aspect type", false);
		/* if type is canceled */
//			if ( type == null )
//				return null;
		String objectClass = "";
		switch (AspectClass.valueOf(type))
    	{
    	case CALCULATED:
    		objectClass = Helper.obtainInput( Helper.listToArray(
    				ClassRef.getAllOptions( PackageRef.calculatedPackage ) ), 
    				"aspect class", false);
    		this.set(  Instance.getNew(xmlElem, null, objectClass) , name );
    		break;
    	case EVENT: 
    		objectClass = Helper.obtainInput( Helper.listToArray(
    				ClassRef.getAllOptions( PackageRef.eventPackage) ), 
    				"aspect class", false);
    		this.set(  Instance.getNew(xmlElem, null, objectClass) , name );
    		break;
    	case PRIMARY:
		default:
			objectClass = Helper.obtainInput( ObjectRef.getAllOptions(), 
					"Primary type", false);
			this.set( ObjectFactory.loadObject( null, objectClass), name);
			break;
		}
		this.registry = ((AspectInterface) parent).reg();
		registry.addInstatiatedAspect( name, this );
	}

	@Override
	public void removeModule(String specifier) 
	{
		this.registry.remove(this.key);
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

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}
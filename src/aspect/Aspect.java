package aspect;

import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.calculated.StateExpression;
import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.ObjectRef;
import dataIO.XmlRef;
import dataIO.Log.Tier;
import generalInterfaces.Instantiatable;
import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.primarySetters.HashMapSetter;
import nodeFactory.primarySetters.LinkedListSetter;
import surface.Point;
import utility.Helper;

/**
	 * \brief Very general class that acts as a wrapper for other Objects.
	 * 
	 * 
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
	 */
	public class Aspect implements Instantiatable, NodeConstructor
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
			modelNode.setRequirements(Requirements.ZERO_TO_MANY);
			modelNode.setTitle(this.key);
			
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
				
				//FIXME this needs to be finished
		    	switch (simpleName)
				{
				case "HashMap": //FIXME ClassRef
					HashMap<Object,Object> h = (HashMap<Object,Object>) aspect;
					for (Object k : h.keySet() )
						modelNode.add(new HashMapSetter<Object,Object>(
								h.get(k), k, h).getNode() );
					break;
				case "LinkedList": //FIXME ClassRef
					LinkedList<Object> linkedList = (LinkedList<Object>) aspect;
					for (Object o : linkedList)
						modelNode.add(new LinkedListSetter<Object>(
								o, linkedList ).getNode() );
					break;
				case "Body": //FIXME ClassRef
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
							( (Calculated) this.aspect ).getInput(), 
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
			ModelNode modelNode = new ModelNode(XmlRef.item, this);
			modelNode.setRequirements(Requirements.ZERO_TO_MANY);
			
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
			if ( node.getAttribute(XmlRef.valueAttribute) != null )
			{
				this.set(ObjectFactory.loadObject(
						node.getAttribute(XmlRef.valueAttribute).value, 
						node.getAttribute(XmlRef.typeAttribute).value,
						node.getAttribute(XmlRef.classAttribute).value), key);
			}
			NodeConstructor.super.setNode(node);
		}

		// TODO build up from general.classLib rather than hard code
		/**
		 * Create a new minimal object of this class and return it, used by the gui
		 * to add new
		 * @return NodeConstructor
		 */
		@Override
		public void init(Element xmlElem, NodeConstructor parent) {
			String name = "";
			name = Helper.obtainInput(name, "aspect name");
			/* if name is canceled */
//			if ( name == null )
//				return null;
			String type = Helper.obtainInput( Helper.enumToString(
					AspectReg.AspectClass.class).split(" "), 
					"aspect type", false);
			/* if type is canceled */
//			if ( type == null )
//				return null;
			String pack = "";
			String classType = "";
			switch (type)
	    	{
	    	case "CALCULATED":
	    		pack = "aspect.calculated.";
	    		classType = Helper.obtainInput( Helper.listToArray(
	    				Idynomics.xmlPackageLibrary.getAll(pack) ), 
	    				"aspect class", false);
	    		if ( classType.equals("StateExpression") ) //FIXME ClassRef
	    			this.set( ObjectFactory.loadObject( 
	    					Helper.obtainInput( "", "expression" ), 
	    					type, classType)  , name );
	    		else
	    			this.set(  ObjectFactory.loadObject( "", 
	    					type, classType) , name );
	    		((AspectInterface) parent).reg().addInstatiatedAspect( name, this );
	    		break;
	    	case "EVENT": 
	    		pack = "aspect.event.";
	    		classType = Helper.obtainInput( Helper.listToArray(
	    				Idynomics.xmlPackageLibrary.getAll(pack) ), 
	    				"aspect class", false);
	    		this.set( ObjectFactory.loadObject( "", 
    					type, classType) , name);

    			((AspectInterface) parent).reg().addInstatiatedAspect( name, this );
	    		break;
	    	case "PRIMARY":
			default:
				classType = Helper.obtainInput( ObjectRef.getAllOptions(), 
						"Primary type", false);
				this.set( ObjectFactory.loadObject( 
    					Helper.obtainInput( "", "Primary value" ), 
    					type, classType), name);
    			((AspectInterface) parent).reg().addInstatiatedAspect( name, this );
				break;
			}
		}
		

		@Override
		public void removeNode(String specifier) 
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
	}
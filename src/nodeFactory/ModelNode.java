package nodeFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import generalInterfaces.Instantiatable;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ModelNode
{
	/**
	 * \brief Clear way of specifying exactly how many sub-model instances may
	 * be made.
	 * 
	 * <p>Note that the use of Integer.MAX_VALUE is due to the lack of an
	 * "infinity" in Integers (as exists in Double). However, at a value of 
	 * (2^31 - 1) > 2 billion, Integer.MAX_VALUE is should survive most
	 * usages.</p>
	 */
	public static enum Requirements
	{
		/**
		 * Exactly one instance of this sub-model may be made: no more, no fewer.
		 */
		EXACTLY_ONE(1, 1),
		/**
		 * This submodel is optional, but if it is made then only one instance
		 * is permitted.
		 */
		ZERO_OR_ONE(0, 1),
		/**
		 * There must be at least one instance of this sub-model made, but there
		 * is no upper limit.
		 */
		ONE_TO_MANY(1, Integer.MAX_VALUE),
		/**
		 * Any number of instances of this sub-model are permitted, even none at
		 * all.
		 */
		ZERO_TO_MANY(0, Integer.MAX_VALUE),

		/* temporary for GUI esthetics (same as zero to many) */
		ZERO_TO_FEW(0, Integer.MAX_VALUE),
		/**
		 * FIXME, clean-up and restructure, a way of identifying Nodes that 
		 * may not change in any way
		 */
		IMMUTABLE(1, 1);

		public final int min, max;

		Requirements(int min, int max)
		{
			this.min = min;
			this.max = max;
		}
	}

	/* ***********************************************************************
	 * VARIABLES
	 * **********************************************************************/

	/**
	 * Associated XML tag.
	 */
	protected String _tag;

	/**
	 * Title (if applicable).
	 */
	protected String _title = "";

	/**
	 * A measure of how many of this node is required (one, many, etc).
	 */
	protected Requirements _requirement;

	/**
	 * Object associated with node.
	 */
	public NodeConstructor constructor;

	/**
	 * Allowed child nodes
	 */
	protected Map<NodeConstructor,Requirements> _childConstructors = 
			new HashMap<NodeConstructor,Requirements>();
	
	/**
	 * Contsructables
	 */
	protected List<Constructable> _constructables = 
			new LinkedList<Constructable>();

	/**
	 * Existing child nodes
	 */
	protected List<ModelNode> _childNodes;

	/**
	 * Attributes
	 */
	protected List<ModelAttribute> _attributes;
	
	/**
	 * boolean is true if the object has been removed via the gui
	 */
	protected boolean isRemoved = false;

	/* ***********************************************************************
	 * INSTANCE CONSTRUCTOR
	 * **********************************************************************/

	/**
	 * General constructor
	 * @param tag
	 * @param constructor
	 */
	public ModelNode(String tag, NodeConstructor constructor)
	{
		this._tag = tag;
		this.constructor = constructor;
		this._childNodes = new LinkedList<ModelNode>();
		this._attributes = new LinkedList<ModelAttribute>();
	}

	/* ***********************************************************************
	 * TAG METHODS
	 * **********************************************************************/

	/**
	 * @return The XML tag for this node.
	 */
	public String getTag()
	{
		return this._tag;
	}

	/**
	 * \brief Check if the given tag is the same as this model node's tag.
	 * 
	 * @param tag String tag to check.
	 * @return {@code true} if they are equal, {@code false} if they are
	 * different.
	 */
	public boolean isTag(String tag)
	{
		return this._tag.equals(tag);
	}

	/**
	 * \brief Check if any of the given tags are the same as this model node's
	 * tag.
	 * 
	 * @param tags List of String tags to check.
	 * @return {@code true} if any are equal to this node's tag, {@code false}
	 * if they are all different.
	 */
	public boolean isTagIn(String[] tags)
	{
		for ( String tag : tags )
			if ( this.isTag(tag) )
				return true;
		return false;
	}
	
	/* ***********************************************************************
	 * TITLE METHODS
	 * **********************************************************************/
	
	/**
	 * @return The title for this node.
	 */
	public String getTitle()
	{
		return this._title;
	}
	
	/**
	 * \brief Set the title for this node.
	 * 
	 * @param title String title.
	 */
	public void setTitle(String title)
	{
		this._title = title;
	}
	
	/* ***********************************************************************
	 * REQUIREMENT METHODS
	 * **********************************************************************/
	
	/**
	 * \brief Set the construction requirements for this node.
	 * 
	 * @param req A measure of how many of this node is required (one, many,
	 * etc).
	 */
	public void setRequirements(Requirements req)
	{
		this._requirement = req;
	}
	
	/**
	 * @return {@code true} if no more than one of this model node may be made.
	 */
	public boolean requireMaxOne()
	{
		return this._requirement.max == 1;
	}
	
	/**
	 * \brief Check if the given node requirements are equal to those set for
	 * this model node.
	 * 
	 * @param req A measure of how many of a node may be required 
	 * (one, many, etc).
	 * @return True if this is the same as the requirements set for this node.
	 */
	public boolean areRequirements(Requirements req)
	{
		return this._requirement == req;
	}
	
	public Requirements getRequirment()
	{
		return this._requirement;
	}
	
	/* ***********************************************************************
	 * THIS CONSTRUCTORS
	 * **********************************************************************/

	/**
	 * Adding a child object, action performed on clicking add button in gui
	 * @param childObject
	 */
	public void add(NodeConstructor childObject)
	{
		this.constructor.addChildObject(childObject);
	}

	/* ***********************************************************************
	 * CHILD NODES
	 * **********************************************************************/

	/**
	 * \brief Add the given child node to this ModelNode object.
	 * 
	 * @param childNode Child model node to add.
	 */
	public void add(ModelNode childNode)
	{
		this._childNodes.add(childNode);
	}

	/**
	 * \brief Get all child nodes which have the given tag.
	 * 
	 * @param tag Node tag to look for.
	 * @return All child nodes belonging to this node that have the same tag.
	 */
	public List<ModelNode> getChildNodes(String tag)
	{
		List<ModelNode> out = new LinkedList<ModelNode>();
		for ( ModelNode c : this._childNodes )
			if ( c.isTag(tag) )
				out.add(c);
		return out;
	}
	
	/**
	 * @return All child nodes.
	 */
	public List<ModelNode> getAllChildNodes()
	{
		return this._childNodes;
	}
	
	/* ***********************************************************************
	 * CHILD NODE CONSTRUCTORS
	 * **********************************************************************/

	
	public void addConstructable(String classRef, Requirements requirement)
	{
		this._constructables.add(new Constructable(classRef, requirement));
	}
	
	public void addConstructable(String classRef, String[] classRefs, Requirements requirement)
	{
		this._constructables.add(new Constructable(classRef, classRefs, requirement));
	}
	
	public NodeConstructor getConstruct(String constructable)
	{
		Constructable c = this.getConstructable(constructable);
		if (c.options() == null)
			return (NodeConstructor) Instantiatable.
					getNewInstance(	c.classRef(), null, this.constructor );
		else
			return (NodeConstructor) Instantiatable.getNewInstance(	
					Helper.obtainInput(c.options(), "select class", false), 
					null, this.constructor );
	}
	
	public Requirements getConRequirement(String classRef)
	{
		for (Constructable c : this._constructables)
			if( c.classRef() == classRef )
				return c.requirement();
		return null;
	}
	
	public Constructable getConstructable(String classRef)
	{
		for (Constructable c : this._constructables)
			if( c.classRef() == classRef )
				return c;
		return null;
	}
	
	public String[] getConstructables()
	{
		int i = 0;
		String[] out = new String[this._constructables.size()];
		for (Constructable c : this._constructables)
			out[i++] = c.classRef();
		return out;
	}
	/**
	 * \brief Add a child node constructor, together with requirements on how
	 * many times it may be constructed.
	 * 
	 * @param cnstr Child node constructor for this ModelNode.
	 * @param req A measure of how many of this child constructor node is
	 * required (one, many, etc).
	 */
	public void addChildConstructor(NodeConstructor cnstr, Requirements req)
	{
		this._childConstructors.put(cnstr, req);
	}
	
	/**
	 * @return All child node constructors.
	 */
	public Set<NodeConstructor> getAllChildConstructors()
	{
		return this._childConstructors.keySet();
	}
	
	/**
	 * \brief Check if the given child node constructor should be made exactly
	 * once.
	 * 
	 * @param cnstr Child node constructor belonging to this ModelNode.
	 * @return {@code true} if it must be made exactly once, {@code false} if
	 * it may be omitted or made more than once.
	 */
	public boolean requireExactlyOneChildConstructor(NodeConstructor cnstr)
	{
		return this._childConstructors.get(cnstr) == Requirements.EXACTLY_ONE;
	}
	
	/* ***********************************************************************
	 * NODE ATTRIBUTES
	 * **********************************************************************/

	/**
	 * \brief Add attribute to this ModelNode object
	 * 
	 * @param attribute ModelAttribute to add.
	 */
	public void add(ModelAttribute attribute)
	{
		this._attributes.add(attribute);
	}

	/**
	 * \brief Get a single attribute identified by String.
	 * 
	 * @param attribute Tag for the attribute required.
	 * @return The ModelAttribute with this tag, or null if it cannot be found.
	 */
	// TODO what if there is more that one attribute with the same name?
	public ModelAttribute getAttribute(String attribute)
	{
		for ( ModelAttribute a : this._attributes )
			if ( a.tag.equals(attribute) )
				return a;
		return null;
	}
	
	/**
	 * @return List of all this ModelNode's attributes.
	 */
	public List<ModelAttribute> getAttributes()
	{
		return this._attributes;
	}

	/* ***********************************************************************
	 * WRITING XML
	 * **********************************************************************/

	/**
	 * \brief Write a description of this ModelNode in XML format, off-setting
	 * every line by the required number of tabs.
	 * 
	 * @param tabs Number of tabs to offset by.
	 * @return String description of this ModelNode in XML format.
	 */
	public String getXML(int tabs)
	{
		String out = "";
		out += appendTabs(tabs) + "<" + this._tag;

		/* 
		 * Attributes
		 */
		for ( ModelAttribute a : this._attributes )
			out += a.getXML();
		/*
		 * Child nodes
		 */
		if ( this._childNodes.isEmpty() )
			out += " />\n ";
		else
		{
			out += " >\n";
			for ( ModelNode n : this._childNodes )
				out += n.getXML(tabs+1);
			out += appendTabs(tabs) + "</" + this._tag + ">\n";
		}
		return out;
	}

	/**
	 * \brief Write a description of this ModelNode in XML format, off-setting
	 * every line by an appropriate number of tabs.
	 * 
	 * @return String description of this ModelNode in XML format.
	 */
	public String getXML()
	{
		return this.getXML(0);
	}

	/**
	 * \brief Helper method for creating line offsets.
	 * 
	 * @param tabs Number of tabs.
	 * @return String for that many tabs.
	 */
	private static String appendTabs(int tabs)
	{
		String out = "";
		for( int i = 1; i < tabs; i++ )
			out += "\t";
		return out;
	}

	public void delete(String specifier) {
		this.isRemoved = true;
		constructor.removeNode(specifier);		
	}
	
	public boolean isRemoved()
	{
		return this.isRemoved;
	}

	public boolean hasChildNodes(String tag) 
	{
		for ( ModelNode m : _childNodes)
		{
			if ( m.constructor.defaultXmlTag() == tag );
				return true;
		}
		return false;
	}
}

package settable;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import instantiable.Instance;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Module
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
	protected Settable settableObject;

	/**
	 * List of child module specifications. Add the specification to this list
	 * if the object should be constructable as child of this module in the gui
	 */
	protected List<ModuleSpec> _constructables = 
			new LinkedList<ModuleSpec>();

	/**
	 * Existing child modules
	 */
	protected List<Module> _childModules;

	/**
	 * Attributes of this module
	 */
	protected List<Attribute> _attributes;

	/**
	 * General constructor
	 * @param tag
	 * @param settable
	 */
	public Module(String tag, Settable settable)
	{
		this._tag = tag;
		this.settableObject = settable;
		this._childModules = new LinkedList<Module>();
		this._attributes = new LinkedList<Attribute>();
	}
	
	/**
	 * \brief returns the Object associated with this module
	 * @return
	 */
	public Object getAssociatedObject()
	{
		return settableObject;
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

	/* FIXME the following method seem a bit silly to put here, rather convert
		generic helper method, does String match any of String[] (Helper class) */
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
			if ( this._tag.equals(tag) )
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
	 * TODO migrate to ModuleSpec?
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
	
	/**
	 * \brief return the requirement specification for the object associated to
	 * this module.
	 * @return
	 */
	public Requirements getRequirment()
	{
		return this._requirement;
	}
	
	/* ***********************************************************************
	 * ADD and GET Children
	 * **********************************************************************/

	/**
	 * \brief Add the given child node to this ModelNode object.
	 * 
	 * @param childNode Child model node to add.
	 */
	public void add(Module childModule)
	{
		this._childModules.add(childModule);
	}

	/**
	 * \brief Get all child nodes which have the given tag.
	 * 
	 * @param tag Node tag to look for.
	 * @return All child nodes belonging to this node that have the same tag.
	 */
	public List<Module> getChildModules(String tag)
	{
		List<Module> out = new LinkedList<Module>();
		for ( Module c : this._childModules )
			if ( tag.equals(c.getTag()) )
				out.add(c);
		return out;
	}
	
	/**
	 * @return All child nodes.
	 */
	public List<Module> getAllChildModules()
	{
		return this._childModules;
	}
	
	/* ***********************************************************************
	 * Child node specifications (specifies what object could be added as child)
	 * **********************************************************************/

	
	public void addChildSpec(String classRef, Requirements requirement)
	{
		this._constructables.add(new ModuleSpec(classRef, requirement));
	}
	
	public void addChildSpec(String classRef, Requirements requirement, String label)
	{
		this._constructables.add(new ModuleSpec(classRef, requirement, label));
	}
	
	public void addChildSpec(String classRef, String[] classRefs, Requirements requirement)
	{
		this._constructables.add(new ModuleSpec(classRef, classRefs, requirement));
	}
	
	public Module constructChild(String childReference)
	{
		ModuleSpec c = this.getChildSpec(childReference);
		Settable con;
		if (c.options() == null)
		{
			con = (Settable) Instance.getNew( null, this.settableObject, 
					c.classRef() );
		}
		else
		{
			con =  (Settable) Instance.getNew( null, this.settableObject, 
					c.options() );
		}
		Module node = con.getModule();
		this.add(node);
		return node;
	}
	
	public Requirements getChildRequirement(String classRef)
	{
		for (ModuleSpec c : this._constructables)
			if( c.classRef() == classRef )
				return c.requirement();
		return null;
	}
	
	public ModuleSpec getChildSpec(String classRef)
	{
		for (ModuleSpec c : this._constructables)
			if( c.classRef() == classRef )
				return c;
		return null;
	}
	
	public String[] getAllChildSpec()
	{
		int i = 0;
		String[] out = new String[this._constructables.size()];
		for (ModuleSpec c : this._constructables)
			out[i++] = c.classRef();
		return out;
	}
	
	/* ***********************************************************************
	 * MODULE ATTRIBUTES
	 * **********************************************************************/

	/**
	 * \brief Add attribute to this ModelNode object
	 * 
	 * @param attribute ModelAttribute to add.
	 */
	public void add(Attribute attribute)
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
	public Attribute getAttribute(String attribute)
	{
		for ( Attribute a : this._attributes )
			if ( a.tag.equals(attribute) )
				return a;
		return null;
	}
	
	/**
	 * @return List of all this ModelNode's attributes.
	 */
	public List<Attribute> getAttributes()
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
	public StringWriter getXML(int tabs, StringWriter writer)
	{
		String out = "";
		appendTabs(tabs, writer);
		writer.append('<').append(this._tag);
		/* 
		 * Attributes
		 */
		for ( Attribute a : this._attributes )
			a.getXML(writer);
		/*
		 * Child nodes
		 */
		if ( this._childModules.isEmpty() )
			writer.append(" />\n ");
		else
		{
			writer.append(" >\n");
			for ( Module n : this._childModules )
				n.getXML(tabs+1, writer);
			appendTabs(tabs, writer);
			writer.append("</").append(this._tag).append(">\n");
		}
		return writer;
	}
	
	public String getHeader()
	{
		String out = "";

		/* 
		 * Attributes
		 */
		int i = 0;
		for ( Attribute a : this._attributes )
		{
			out += a.getTag();
			i++;
			if (i < this._attributes.size())
				out += ";";
		}

		return out;
	}
	
	public String getCSV()
	{
		String out = "";
		/* 
		 * Attributes
		 */
		int i = 0;
		for ( Attribute a : this._attributes )
		{
			out += a.getValue();
			i++;
			if (i < this._attributes.size())
				out += ";";
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
		StringWriter outputWriter = new StringWriter();
		outputWriter = this.getXML(0, outputWriter);
		return outputWriter.toString();
	}

	/**
	 * \brief Helper method for creating line offsets.
	 * 
	 * @param tabs Number of tabs.
	 * @return String for that many tabs.
	 */
	private static StringWriter appendTabs(int tabs, StringWriter writer)
	{
		for( int i = 1; i < tabs; i++ )
			writer.append('\t');
		return writer;
	}

	public void delete(String specifier) 
	{
		settableObject.removeModule(specifier);		
	}

	public boolean hasChildNodes(String tag) 
	{
		for ( Module m : _childModules)
		{
			if ( m.settableObject.defaultXmlTag() == tag );
				return true;
		}
		return false;
	}
}

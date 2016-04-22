package nodeFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class ModelNode {
	
	/**
	 * \brief Clear way of specifying exactly how many sub-model instances may
	 * be made.
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
		
		/* temporary for gui esthetics (same as zero to many) */
		ZERO_TO_FEW(0, Integer.MAX_VALUE);
		
		
		/*
		 * Note that the use of Integer.MAX_VALUE is due to the lack of an
		 * "infinity" in Integers (as exists in Double). However, at a value of 
		 * (2^31 - 1) > 2 billion, Integer.MAX_VALUE is should survive most
		 * usages.
		 */
		
		private final int _min, _max;
		
		Requirements(int min, int max)
		{
			_min = min;
			_max = max;
		}
		
		public boolean maxOne()
		{
			return (_max == 1);
		}
	}
	
	/**
	 * Associated xml tag
	 */
	public String tag;
	
	/**
	 * Requirement of node one, many etc
	 */
	public Requirements requirement;
	
	/**
	 * Object associated with node
	 */
	public NodeConstructor constructor;
	
	/**
	 * Allowed childnodes
	 */
	public HashMap<NodeConstructor,Requirements> childConstructors = 
			new HashMap<NodeConstructor,Requirements>();
	
	/**
	 * Existing childnodes
	 */
	public List<ModelNode> childNodes;
	
	/**
	 * Attributes
	 */
	public List<ModelAttribute> attributes;
	
	/**
	 * General constructor
	 * @param tag
	 * @param constructor
	 */
	public ModelNode(String tag, NodeConstructor constructor)
	{
		this.tag = tag;
		this.constructor = constructor;
		this.childNodes = new LinkedList<ModelNode>();
		this.attributes = new LinkedList<ModelAttribute>();
	}
	
	/**
	 * Add attribute to ModelNode object
	 * @param attribute
	 */
	public void add(ModelAttribute attribute)
	{
		this.attributes.add(attribute);
	}
	
	/**
	 * Add childnode to ModelNode object
	 * @param childNode
	 */
	public void add(ModelNode childNode)
	{
		this.childNodes.add(childNode);
	}
	
	/**
	 * Add Object to ModelNode's 'Allowed ChildObjects list' 
	 * @param childObject
	 */
	public void add(NodeConstructor childObject)
	{
		constructor.addChildObject(childObject);
	}
	
	/**
	 * Get attribute identified by String
	 * @param attribute
	 * @return
	 */
	public ModelAttribute getAttribute(String attribute)
	{
		for( ModelAttribute a : attributes )
			if (a.tag.equals(attribute))
				return a;
		return null;
	}

	/**
	 * Get ChildNodes identified by String tag
	 * @param tag
	 * @return
	 */
	public List<ModelNode> getChildNodes(String tag)
	{
		List<ModelNode> out = new LinkedList<ModelNode>();
		for( ModelNode c : childNodes )
			if (c.tag.equals(tag))
				out.add(c);
		return out;
	}
	
	/**
	 * returns xml String from this ModelNode
	 * @return
	 */
	public String getXML()
	{
		String out = "";
		out += "<" + tag;
		
		/* attributes */
		for ( ModelAttribute a : attributes )
		{
			out += a.getXML();
		}
		
		/* child nodes */
		if ( childNodes.isEmpty() )
		{
			out += " />\n";
		}
		else
		{
			out += " >\n";
			for( ModelNode n : childNodes )
			{
				out += n.getXML();
			}
			out += "</" + tag + ">\n";
		}
		
		return out;
	}

}

package nodeFactory;

import java.util.LinkedList;
import java.util.List;

public class ModelNode {
	
	public String tag;
	public boolean unique;
	public NodeConstructor constructor;
	public List<ModelNode> childNodes;
	public List<ModelAttribute> attributes;
	
	public ModelNode(String tag, NodeConstructor constructor)
	{
		this.tag = tag;
		this.constructor = constructor;
		this.childNodes = new LinkedList<ModelNode>();
		this.attributes = new LinkedList<ModelAttribute>();
	}
	
	public void add(ModelAttribute attribute)
	{
		this.attributes.add(attribute);
	}
	
	public void add(ModelNode childNode)
	{
		this.childNodes.add(childNode);
	}
	
	public ModelAttribute getAttribute(String attribute)
	{
		for( ModelAttribute a : attributes )
			if (a.tag.equals(attribute))
				return a;
		return null;
	}

	public ModelNode getChild(String child) {
		for( ModelNode c : childNodes )
			if (c.tag.equals(child))
				return c;
		return null;
	}
	
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

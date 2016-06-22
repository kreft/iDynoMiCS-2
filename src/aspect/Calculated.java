package aspect;

import generalInterfaces.Copyable;
import generalInterfaces.Instantiatable;
import generalInterfaces.Redirectable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import dataIO.XmlHandler;
import dataIO.XmlRef;

/**
 * Calculated/Secondary states contain a description of how secondary states 
 * can be calculated from primary states. Secondary states return the value of 
 * this calculated states when queried and are intended to prevent errors due to
 * state values that have not been updated, and they reduce memory capacity 
 * since the can be set on species level rather than agent level.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Calculated implements Copyable, Instantiatable, Redirectable
{
	/**
	 * input string
	 */
	protected String _input;

	/**
	 * StateExpressions require an input string to set the expression
	 * @param input
	 */
	public void setInput(String input)
	{
		this._input = input;
	}
	
	/**
	 * returns the input String array of this state
	 * @return
	 */
	public String getInput()
	{
		return _input;
	}
	
	/**
	 * return a copy of this state.
	 */
	public Calculated copy()
	{
		return this;
	}
	
	/**
	 * General constructor from xmlNodes, returns a new instance directly from
	 * an xml node. Overwrite this method in implementing class if the class
	 * needs constructor arguments (they should be stored within the Node).
	 */
	public static Object getNewInstance(Node xmlNode)
	{
		Calculated obj = (Calculated) Instantiatable.getNewInstance(xmlNode);
		obj.init((Element) xmlNode);
		return obj;
	}
	

	public static Object getNewInstance(String input) {
		Calculated obj = (Calculated) Instantiatable.getNewInstance(input);
		obj.init(input);
		return obj;
	}

	public void init(Element xmlElem)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, XmlRef.inputAttribute);
		if (input != "")
			this.setInput(input);
		
		String fields = XmlHandler.gatherAttribute(xmlElem, XmlRef.fields);
		if (fields != null)
			this.redirect(fields);
	}
	
	private void init(String input) {
		this.setInput(input);
	}
	
	/**
	 * return the current (up-to-date) value of the secondary state.
	 */
	public abstract Object get(AspectInterface registry);

}
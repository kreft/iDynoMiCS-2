package aspect;

import generalInterfaces.Copyable;
import generalInterfaces.XMLable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import dataIO.XmlHandler;
import dataIO.XmlLabel;

/**
 * Calculated/Secondary states contain a description of how secondary states 
 * can be calculated from primary states. Secondary states return the value of 
 * this calculated states when queried and are intended to prevent errors due to
 * state values that have not been updated, and they reduce memory capacity 
 * since the can be set on species level rather than agent level.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Calculated implements Copyable, XMLable
{
	/**
	 * input states
	 */
	protected String[] _input;

	/**
	 * method that sets the input from a comma separated String.
	 * @param input
	 */
	public void setInput(String input)
	{
		input.replaceAll("\\s+","");
		this._input = input.split(",");
	}
	
	public void setField(String field, String value)
	{
		try {
			this.getClass().getField(field).set(this, value);
		} catch (IllegalArgumentException | IllegalAccessException | 
				NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * returns the input String array of this state
	 * @return
	 */
	public String[] getInput()
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
		Calculated obj = (Calculated) XMLable.getNewInstance(xmlNode);
		obj.init((Element) xmlNode);
		return obj;
	}
	

	public static Object getNewInstance(String input) {
		Calculated obj = (Calculated) XMLable.getNewInstance(input);
		obj.init(input);
		return obj;
	}

	public void init(Element xmlElem)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, "input");
		if (input != "")
			this.setInput(input);
		
		String fields = XmlHandler.gatherAttribute(xmlElem, XmlLabel.fields);
		String[] f = null;
		if (fields != "")
		{
			f = fields.split(",");
			for (String field : f)
			{
				String[] value;
				value = field.split("=");
				this.setField(value[0], value[1]);
			}
		}
	}
	
	private void init(String input) {
		this.setInput(input);
	}
	
	/**
	 * return the current (up-to-date) value of the secondary state.
	 */
	public abstract Object get(AspectInterface registry);

}
package aspect;

import org.w3c.dom.Element;

import dataIO.XmlHandler;
import generalInterfaces.Copyable;
import generalInterfaces.Redirectable;
import instantiable.Instance;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Settable;

/**
 * Calculated/Secondary states contain a description of how secondary states 
 * can be calculated from primary states. Secondary states return the value of 
 * this calculated states when queried and are intended to prevent errors due to
 * state values that have not been updated, and they reduce memory capacity 
 * since the can be set on species level rather than agent level.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class Calculated implements Copyable, Instantiable, Redirectable
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
	
	public static Object instanceFromString(String objecClass, String input) {
		Calculated obj = (Calculated) Instance.getNew(null, null, 
				objecClass );
		obj.init(input);
		return obj;
	}

	public void instantiate(Element xmlElem, Settable parent)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, XmlRef.inputAttribute);
		if (input != "")
			this.setInput(input);
		
		this.redirect(xmlElem);		
	}
	
	private void init(String input) {
		this.setInput(input);
	}
	
	/**
	 * return the current (up-to-date) value of the secondary state.
	 */
	public abstract Object get(AspectInterface registry);

}
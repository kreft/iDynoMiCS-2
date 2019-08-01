package aspect.calculated;

import org.w3c.dom.Element;

import aspect.AspectInterface;
import aspect.Calculated;
import dataIO.XmlHandler;
import expression.Expression;
import referenceLibrary.XmlRef;
import settable.Settable;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class ComponentExpression extends Calculated {
		
	private Expression expression;

	@Override
	public void setInput(String input)
	{
		this.expression = new Expression( input );
	}
	
	@Override
	public void instantiate(Element xmlElem, Settable parent)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, XmlRef.inputAttribute);
		if (input != "")
			this.setInput(input);
		else
			this.setInput(Helper.obtainInput( "", "expression" ));
		
		this.redirect(xmlElem);		
	}

	
	/**
	 * input[0] expression
	 */
	public Object get(AspectInterface aspectOwner)
	{
		return expression;
	}

}
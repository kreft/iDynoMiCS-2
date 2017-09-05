package aspect.calculated;

import java.util.HashMap;

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
public class StateExpression extends Calculated {
		
	private Expression expression;
	private HashMap<String, Double> variables = new HashMap<String, Double>();

	@Override
	public void setInput(String input)
	{
		this._input = input;
		this.expression = new Expression( input.replaceAll("\\s+","") );
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
		variables.clear();
		for( String var : expression.getAllVariablesNames() )
			variables.put( var, aspectOwner.getDouble(var) );
		return expression.getValue( variables );
	}

}
package aspect.calculated;

import java.util.HashMap;

import org.w3c.dom.Element;

import aspect.AspectInterface;
import aspect.Calculated;
import dataIO.XmlHandler;
import expression.ExpressionB;
import referenceLibrary.XmlRef;
import settable.Settable;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class StateExpression extends Calculated {
		
	private ExpressionB expression;
	private HashMap<String, Double> variables = new HashMap<String, Double>();

	@Override
	public void setInput(String input)
	{
		this._input = input;
		this.expression = new ExpressionB( input.replaceAll("\\s+","") );
	}
	
	@Override
	public void instantiate(Element xmlElem, Settable parent)
	{
		String input = XmlHandler.gatherAttribute(xmlElem, XmlRef.inputAttribute);
		if (input != "")
			this.setInput(input);
		else
			this.setInput(Helper.obtainInput( "", "expression" ));
		
		String fields = XmlHandler.gatherAttribute(xmlElem, XmlRef.fields);
		if (fields != null)
			this.redirect(fields);
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
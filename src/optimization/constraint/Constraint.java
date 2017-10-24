package optimization.constraint;

import java.util.HashMap;
import java.util.function.Predicate;

import expression.Expression;

public class Constraint {
	
	private Expression _expression;
	private Predicate<Object> _predicate;

	public void Constrataint(Expression expression, Predicate<Object> predicate)
	{
		this._expression = expression;
		this._predicate = predicate;
	}
	
	public boolean test( double[] data )
	{
		HashMap<String,Double> elements = new HashMap<String,Double>();
		for( int i = 1; i <= data.length; i++ )
			elements.put("X" + i , data[i-1] );
		return this._predicate.test( this._expression.getValue( elements ));
	}
	
	public String toString()
	{
		return this._expression.toString() + " " + this._predicate.toString();
	}
}

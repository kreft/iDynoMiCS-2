package optimization.constraint;

import java.util.HashMap;

import expression.Expression;

public class Condition implements Constraint {
	
	private Expression _expression;
	private double _bound;
	private boolean _sign;

	public void Constrataint(Expression expression, double bound, 
			boolean isUpperBound)
	{
		this._expression = expression;
		this._bound = bound;
		this._sign = isUpperBound;
	}
	
	@Override
	public boolean test( double[] values )
	{
		HashMap<String,Double> elements = new HashMap<String,Double>();
		for( int i = 1; i <= values.length; i++ )
			elements.put("X" + i , values[i-1] );
		double out = this._expression.getValue( elements );
		return (_sign ? out <= _bound : out >= _bound );
	}
	
	public boolean isUpperBound()
	{
		return this._sign;
	}
	
	public String toString()
	{
		return this._expression.toString() + (_sign ? " <= " + _bound : " >= " + 
				_bound );
	}


}

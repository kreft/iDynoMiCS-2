/**
 * 
 */
package expression;

import java.util.HashMap;

/**
 * @author cleggrj
 *
 */
public class Multiplication extends ComponentComposite
{
	/**
	 * <b>a</b> * <b>b</b>
	 */
	public Multiplication(Component a, Component b)
	{
		super(a, b);
		this._expr = "*";
	}

	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		return this._a.getValue(variables) * this._b.getValue(variables);
	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		Component da = this._a.differentiate(withRespectTo);
		Component db = this._b.differentiate(withRespectTo);
		if ( Constant.isConstantWithValue(da, 0.0) )
			return new Multiplication(this._a, db);
		if ( Constant.isConstantWithValue(db, 0.0) )
			return new Multiplication(this._b, da);
		return new Addition(new Multiplication(this._a, db),
								new Multiplication(this._b, da));
	}
}
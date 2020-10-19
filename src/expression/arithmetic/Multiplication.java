/**
 * 
 */
package expression.arithmetic;

import java.util.Map;

import expression.Component;
import expression.ComponentDouble;

/**
 * \brief A component of a mathematical expression composed of the
 * multiplication of two or more sub-components.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Multiplication extends ComponentDouble
{
	/**
	 * \brief Construct a multiplication component of a mathematical expression
	 * from two sub-components.
	 * 
	 * @param a One of the two sub-components.
	 * @param b The other sub-component.
	 */
	public Multiplication(Component a, Component b)
	{
		super(a, b);
		this._expr = "*";
	}
	
	@Override
	public double calculateValue(Map<String, Double> variables)
	{
		return this._a.getValue(variables) * this._b.getValue(variables);

	}
	
	@Override
	public Component differentiate(String withRespectTo)
	{
		/* TODO verify results */
		Multiplication aDb = Arithmetic.multiply(this._a, 
				this._b.differentiate(withRespectTo));
		Multiplication bDa = Arithmetic.multiply(this._b,
						this._a.differentiate(withRespectTo));
		if ( this._a instanceof Constant )
		return aDb;
		if ( this._b instanceof Constant )
		return bDa;
		return Arithmetic.add(aDb, bDa);
	}
}
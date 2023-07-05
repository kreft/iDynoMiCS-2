/**
 * 
 */
package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentSingle;

/**
 * \brief Signum function return 1 for positive value, -1 for negative and 0 for
 * a zero value
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Sign extends ComponentSingle
{
	/**
	 * \brief Construct a Sign component.
	 * 
	 * @param a Component to perform signum function on
	 */
	public Sign(Component a)
	{
		super(a);
		this._expr = "SIGN";
	}
	
	@Override
	public double calculateValue(Map<String, Double> variables)
	{
		return Math.signum(this._a.getValue(variables));
	}
	
	@Override
	public Object evaluate(AspectInterface subject) 
	{
		return Math.signum((double) this._a.evaluate(subject));
	}
	
	/**
	 * The derivative of the Signum function should always be zero except for
	 * at x = 0 where it should be 2 * Dirac delta function (0) = 2 * infinity.
	 * FIXME: [Bas] verify this please
	 */
	@Override
	protected Component getDifferential(String withRespectTo) {
		if (this.calculateValue(null) == 0.0 )
			return new Constant("Infinity" , Double.MAX_VALUE);
		return Arithmetic.zero();
	}
}
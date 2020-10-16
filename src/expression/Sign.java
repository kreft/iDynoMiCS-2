/**
 * 
 */
package expression;

import java.util.Map;

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
	public Sign(ComponentNumerical a)
	{
		super(a);
		this._expr = "SIGN";
	}
	
	@Override
	public double calculateValue(Map<String, Double> variables)
	{
		return Math.signum(this._a.getValue(variables));
	}
	
	/**
	 * The derivative of the Signum function should always be zero except for
	 * at x = 0 where it should be 2 * Dirac delta function (0) = 2 * infinity.
	 * FIXME: [Bas] verify this please
	 */
	@Override
	protected ComponentNumerical getDifferential(String withRespectTo) {
		if (this.calculateValue(null) == 0.0 )
			return new Constant("Infinity" , Double.MAX_VALUE);
		return Arithmetic.zero();
	}
}
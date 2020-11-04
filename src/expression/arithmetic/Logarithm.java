package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import expression.Component;
import expression.ComponentDouble;

/**
 * \brief A component of a mathematical expression composed of the logarithm
 * of one component to the base of another.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Logarithm extends ComponentDouble
{
	/**
	 * \brief Construct a logarithm component of a mathematical expression
	 * from two sub-components.
	 * 
	 * <p>log<sub><b>b</b></sub>(<b>a</b>)</p>
	 * 
	 * @param a The sub-component inside the brackets.
	 * @param b The base of the logarithm.
	 */
	public Logarithm(Component a, Component b)
	{
		super(a, b);
		double B = this._b.getValue(null);
		if ( this._b instanceof Constant )
		{
			if ( B == 1.0 || B == 0.0 )
				Log.out(Tier.CRITICAL,"WARNING! Infinite value: log base "+B);
		}
	}
	
	@Override
	public String getName()
	{
		String out = "log_{" + this._b.getName() + "}("+this._a.getName()+")";
		return ( isNegative() ) ? "-"+out : out;
	}
	
	@Override
	public String reportEvaluation(Map<String, Double> variables)
	{
		String out = "log_{" + this._b.reportEvaluation(variables) + "}("+
										this._a.reportEvaluation(variables)+")";
		return ( isNegative() ) ? "-"+out : out; 
	}
	
	@Override
	protected double calculateValue(Map<String, Double> variables)
	{
		double a = this._a.getValue(variables);
		double b = this._b.getValue(variables);
		if ( b == 1.0 || b <= 0.0 )
			this.infiniteValueWarning();
		return Math.log(a)/Math.log(b);
	}
	
	@Override
	public Object evaluate(AspectInterface subject) 
	{
		double a = (double) this._a.evaluate(subject);
		double b = (double) this._b.evaluate(subject);
		if ( b == 1.0 || b <= 0.0 )
			this.infiniteValueWarning();
		return Math.log(a)/Math.log(b);
	}
	
	/**
	 * TODO this is no longer correct!
	 */
	@Override
	public Component differentiate(String withRespectTo)
	{
		Component out;
		if ( this._b instanceof Constant )
		{
			double b = this._b.getValue(null);
			if ( b == Math.E )
			{
				out = new Division(
							this._a.differentiate(withRespectTo), this._a);
			}
			else
			{
				out = new Constant("ln("+this._b.getName()+")", Math.log(b));
				out = Arithmetic.multiply(this._a, out);
				out = new Division(this._a.differentiate(withRespectTo),out);
			}
		}
		else
		{
			out = new LogNatural(this._b);
			Component da = Arithmetic.multiply(this._a, out);
			da = new Division(this._a.differentiate(withRespectTo), da);
			Component db = new LogNatural(this._a);
			db = Arithmetic.multiply(db, this._b.differentiate(withRespectTo));
			db = new Division(db, Arithmetic.multiply(this._b, 
											new Power(out, Arithmetic.two())));
			out = new Subtraction(da, db);
		}
		return out;
	}
}
package expression;

import java.util.HashMap;

import dataIO.LogFile;

public class Logarithm extends ComponentDouble
{
	/**
	 * log<sub><b>b</b></sub>(<b>a</b>)
	 */
	public Logarithm(Component a, Component b)
	{
		super(a, b);
		double B = this._b.getValue(null);
		if ( this._b instanceof Constant )
		{
			if ( B == 1.0 || B == 0.0 )
				LogFile.writeLog("WARNING! Infinite value: log base "+B);
		}
	}
	
	@Override
	public String getName()
	{
		String out = "log_{" + this._b.getName() + "}("+this._a.getName()+")";
		return ( isNegative() ) ? "-"+out : out;
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		String out = "log_{" + this._b.reportValue(variables) + "}("+
										this._a.reportValue(variables)+")";
		return ( isNegative() ) ? "-"+out : out; 
	}
	
	@Override
	public double getValue(HashMap<String, Double> variables)
	{
		double a = this._a.getValue(variables);
		double b = this._b.getValue(variables);
		if ( b == 1.0 || b == 0.0 )
			this.infiniteValueWarning(variables);
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
				out = Expression.multiply(this._a, out);
				out = new Division(this._a.differentiate(withRespectTo),out);
			}
		}
		else
		{
			out = new LogNatural(this._b);
			Component da = Expression.multiply(this._a, out);
			da = new Division(this._a.differentiate(withRespectTo), da);
			Component db = new LogNatural(this._a);
			db = Expression.multiply(db, this._b.differentiate(withRespectTo));
			db = new Division(db, Expression.multiply(this._b, 
											new Power(out, Expression.two())));
			out = new Subtraction(da, db);
		}
		return out;
	}
}
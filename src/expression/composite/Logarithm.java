package expression.composite;

import java.util.HashMap;

import utility.LogFile;
import expression.Component;
import expression.CompositeComponent;
import expression.simple.Constant;

public class Logarithm extends CompositeComponent
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
		return "log_{" + this._b.getName() + "}("+this._a.getName()+")";
	}
	
	@Override
	public String reportValue(HashMap<String, Double> variables)
	{
		return "log_{" + this._b.reportValue(variables) + "}("+
										this._a.reportValue(variables)+")";
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
	
	@Override
	public Component getDifferential(String withRespectTo)
	{
		Component out;
		if ( this._b instanceof Constant )
		{
			double b = this._b.getValue(null);
			if ( b == Math.E )
			{
				out = new Division(
							this._a.getDifferential(withRespectTo), this._a);
			}
			else
			{
				out = new Constant("ln("+this._b.getName()+")", Math.log(b));
				out = new Multiplication(this._a, out);
				out = new Division(this._a.getDifferential(withRespectTo),out);
			}
		}
		else
		{
			out = new Logarithm(this._b, Constant.euler());
			Component da = new Multiplication(this._a, out);
			da = new Division(this._a.getDifferential(withRespectTo), da);
			Component db = new Logarithm(this._a, Constant.euler());
			db = new Multiplication(db, this._b.getDifferential(withRespectTo));
			db = new Division(db, new Multiplication(this._b, 
											new Power(out, Constant.two())));
			out = new Subtraction(da, db);
		}
		return out;
	}
}
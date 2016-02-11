package reaction;

import java.util.HashMap;
import java.util.TreeMap;

import expression.Component;
import expression.Constant;
import expression.Variable;


public class RateExpression {
	
	public String expression;
	
	public String[] operators = new String[]{"^", "SQRT", "*", "/", "+", "-"};
	
	public TreeMap<Integer, RateExpression> _subExpressions = new TreeMap<Integer, RateExpression>();
	
	public HashMap<String, Double> _terms;
	
	public TreeMap<Integer, String> _eval =  new TreeMap<Integer, String>();
		
	public RateExpression(String expression, HashMap<String, Double> terms)
	{
		expression = expression.replaceAll("\\s+","");
		this.expression = expression;
		if (terms == null)
			terms = new HashMap<String, Double>();
		_terms = terms;
			
		TreeMap<Integer,Integer> brackets = new TreeMap<Integer,Integer>();
		
		int c = -1;
		while (true)
		{
			int temp = expression.indexOf("(", c+1);
			if (temp == -1)
				break;
			brackets.put(temp, 1);
			c = temp;
		}
		
		c = -1;
		while (true)
		{
			int temp = expression.indexOf(")", c+1);
			if (temp == -1)
				break;
			brackets.put(temp, -1);
			c = temp;
		}
		
		brackets.put(expression.length(), -1);
		
		c = 0;
		int o = 0;
		for(Integer key : brackets.keySet())
		{
			if (c == 0)
			{
				setEq(String.valueOf(expression.subSequence(o, key)));
			}
			if(brackets.get(key) != null)
			{
				c += brackets.get(key);
				if(c == 1)
					o = key;
				if(c == 0)
				{
					setSub(o,key+1);
					o = key+1;
				}
			}
		}
	}
	
	public void setEq(String equation)
	{
		
		TreeMap<Integer,String> operLoc = new TreeMap<Integer,String>();
		for(String s : operators)
		{
			operLoc.putAll(identifyStrLoc(equation,s));
		}
		
		int o = 0;
		for(Integer key : operLoc.keySet())
		{
			if(key != 0)
				_eval.put(o,equation.substring(o,key));
			o = key+1;
		}
		if(o != 0)
			_eval.put(o,equation.substring(o,equation.length()));
		_eval.putAll(operLoc);
	}
	
	public TreeMap<Integer,String> identifyStrLoc(String sequence, String str)
	{
		TreeMap<Integer,String> seqMap = new TreeMap<Integer,String>();
		int c = -1;
		while (true)
		{
			int temp = sequence.indexOf(str, c+1);
			if (temp == -1)
				break;
			seqMap.put(temp, str);
			c = temp;
		}
		return seqMap;
	}
	
	public void setSub(int start, int end)
	{
		_subExpressions.put(start, new RateExpression( 
				expression.substring(start+1, end-1), this._terms));
		_eval.put(start, String.valueOf("$" + start));
	}
	
	public void addTerm(String key, double value)
	{
		this._terms.put(key, value);
	}
	
	public void printEval()
	{
		System.out.println(stringEval());
	}
	
	public String stringEval()
	{
		String str = "";
		String t;
		for(Integer e :_eval.keySet())
		{
			t = _eval.get(e);
			if( t.contains("$"))
			{
				str = str + "( " + _subExpressions.get( Integer.valueOf(
						t.replaceAll("\\$", ""))).stringEval() + ") ";
			}
			else
				str = str + _eval.get(e) + " ";
		}
		return str;
	}
	
	public Component build()
	{
		TreeMap<Integer, Component> _calc = new TreeMap<Integer, Component>();
		String t;
		for(Integer i : _eval.keySet())
		{
			t = _eval.get(i);
			// braces
			if(t.contains("$"))
				_calc.put(i, _subExpressions.get( Integer.valueOf(
						t.replaceAll("\\$", ""))).build());
			
	
			
			// constants
			if(t.contains("."))
				_calc.put(i, new Constant(t, Double.parseDouble(t)));
			
			// variables
			boolean isOperator = false;
			for(String op : operators)
			{
				if(t.contains(op))
					isOperator = true;
			}
			
			if(! isOperator)
				_calc.put(i, new Variable(t));
		}
		
		// Do the operator stuff here
		for(int j = 0; j < operators.length; j++)
		{
			for(Integer i : _eval.keySet())
			{
				t = _eval.get(i);
				if(t.contains(operators[j]))
					constructComponent(operators[j],i-1,i+1);
			}
		}
		
		// WORK IN PROGRESS when all operations are implemented
		return new Constant("",5.0);
	}
	
	public Component constructComponent(String operator, int prev, int next)
	{
		return new Constant("",5.0);
	}
	
}
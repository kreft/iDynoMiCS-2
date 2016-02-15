package expression;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * 
 * @author baco
 *
 */
public class ExpressionBuilder {
	
	/**
	 * Input expression
	 */
	public final String expression;
	
	/**
	 * Recognized operators (will prob come frome the expression pack somewhere)
	 */
	public String[] operators = new String[]{"^", "SQRT", "*", "/", "+", "-"};
	
	/**
	 * Subexpression (braces)
	 */
	private TreeMap<Integer, ExpressionBuilder> _subExpressions = 
			new TreeMap<Integer, ExpressionBuilder>();
	
	/**
	 * Todo: constants with name?
	 */
	public HashMap<String, Double> _constants;
	
	/**
	 * 
	 */
	public List<String> _variables = new LinkedList<String>();
	
	/**
	 * Eval tree
	 */
	private TreeMap<Integer, String> _eval =  new TreeMap<Integer, String>();
	
	/**
	 * calc TreeMap
	 */
	private TreeMap<Integer, Component> _calc = 
			new TreeMap<Integer, Component>();
	
	/**
	 * The component object
	 */
	public final Component component;
	
	/**
	 * 
	 * @param expression
	 * @param terms
	 */
	public ExpressionBuilder(String expression, HashMap<String, Double> terms)
	{
		/**
		 * initial construction
		 */
		expression = expression.replaceAll("\\s+","");
		this.expression = expression;
		if (terms == null)
			terms = new HashMap<String, Double>();
		_constants = terms;
			
		/**
		 * obtain brace location and count depth
		 */
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
			/**
			 * what is handled at this level
			 */
			if (c == 0)
			{
				if(key > 0 )
					setEq(o,String.valueOf(expression.subSequence( o, key )));
			}
			
			/**
			 * what is handled at deeper level (braces)
			 */
			if(brackets.get(key) != null)
			{
				c += brackets.get(key);
				/**
				 * make sure not to reset o on a descent!
				 */
				if(c == 1 && brackets.get(key) != -1)
					o = key;
				if(c == 0)
				{
					setSub(o,key+1);
					o = key+1;
				}
			}
		}
		
		this.component = build();
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param expression
	 */
	public ExpressionBuilder(String expression)
	{
		this(expression, new HashMap<String, Double>());
	}
	
	/**
	 * load brace free sub sequence into eval tree, start represents starts 
	 * location of substring in overall expressiong
	 * @param equation
	 */
	public void setEq(int start, String equation)
	{
		if(! equation.isEmpty())
		{
			/**
			 * locate operators
			 */
			TreeMap<Integer,String> operLoc = new TreeMap<Integer,String>();
			for(String s : operators)
			{
				operLoc.putAll( identifyStrLoc( equation, s, start ));
			}
			
			/**
			 * Load non-operator entries into eval tree
			 */
			int o = 0;
			for(Integer key : operLoc.keySet())
			{
				//NOTE subtract start for correct identification in substring
				if(key-start != 0)
					addVar( o+start,equation.substring( o, key-start ));
				o = key - start + operLoc.get(key).length();
			}
			
			/**
			 * also add the last one (this means we can't end with an operator
			 * build in a check if we would need to do that)
			 */
			if(o != 0)
				addVar( o+start ,equation.substring( o, equation.length() ));
			
			_eval.putAll(operLoc);
		}
	}
	
	private void addVar(int loc, String value)
	{
		_eval.put(loc,value);
		_variables.add(value);
	}
	
	/**
	 * helper method that returns TreeMap that identifies all occurrences of str
	 * in sequence
	 * @param sequence
	 * @param str
	 * @param start
	 * @return
	 */
	public TreeMap<Integer,String> identifyStrLoc(String sequence, String str, 
			int start)
	{
		TreeMap<Integer,String> seqMap = new TreeMap<Integer,String>();
		int c = -1;
		while (true)
		{
			int temp = sequence.indexOf(str, c+1 );
			if (temp == -1)
				break;
			seqMap.put(start+temp, str);
			c = temp;
		}
		return seqMap;
	}
	
	/**
	 * Store substring (braces) in _subExpressions map, also add it to eval,
	 * prepend $ for later identification
	 * @param start
	 * @param end
	 */
	public void setSub(int start, int end)
	{
		_subExpressions.put(start, new ExpressionBuilder( 
				expression.substring(start+1, end-1), this._constants));
		_eval.put(start, String.valueOf("$" + start));
	}
	
	public void addTerm(String key, double value)
	{
		this._constants.put(key, value);
	}
	
	/**
	 * Write full equation from tree on screen
	 */
	public void printEval()
	{
		System.out.println(stringEval());
	}
	
	/**
	 * Return full equation from tree as string (we could do a similar thing for
	 * tex
	 */
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
	
	/**
	 * build a root expression Component
	 * TODO: work in progress
	 * @return
	 */
	public Component build()
	{
		
		String t;
		for(Integer i : _eval.keySet())
		{
			t = _eval.get(i);
			
			boolean isOperator = false;
			for(String op : operators)
			{
				if(t.contains(op))
					isOperator = true;
			}
			
			// braces
			if(t.contains("$"))
			{
				_calc.put(i, _subExpressions.get( Integer.valueOf( t.replaceAll(
						"\\$", "") ) ).component);
			}
			
			/**
			 * constants TODO: we could build them up from "_terms" as well
			 * yet any directly written doubles should also be interpreted as
			 * constant
			 */
			else if(t.contains("."))
			{
				_calc.put(i, new Constant(t, Double.parseDouble(t)));
			}

			/**
			 * variables
			 */
			else if(! (isOperator || t.isEmpty()))
				_calc.put(i, new Variable(t));
		}
		
		// Do the operator stuff here
		for(int j = 0; j < operators.length; j++)
		{
			for(Integer i : _eval.keySet())
			{
				t = _eval.get(i);
				if(t.contains(operators[j]))
				{
					int min = (_calc.floorKey( i-1 ) != null ? 
							_calc.floorKey( i-1 ) : -1);
					int plu = (_calc.ceilingKey( i+1 ) != null ? 
							_calc.ceilingKey( i+1 ) : -1);
					_calc.put(i, constructComponent( operators[j], min, plu ));
					postOperatorTruncate(operators[j], min, plu);
				}
			}
		}

		if (_calc.keySet().isEmpty() || _calc.size() > 1)
		{
			System.err.println("ERROR: unfinished or empty expression root "
					+ "element!!!");
			return new Constant("ERROR!!!!!",1.0);
		} else
			return _calc.get(_calc.firstKey());
	}
	
	/**
	 * Return Component based on operator
	 * @param operator
	 * @param prev
	 * @param next
	 * @return
	 */
	public Component constructComponent(String operator, int prev, int next)
	{
		switch (operator)
		{
		case ("+"): return new Addition(_calc.get(prev),_calc.get(next));
		case ("*"): return new Multiplication(_calc.get(prev),_calc.get(next));
		case ("/"): return new Division(_calc.get(prev),_calc.get(next));
		case ("-"): return (prev >= 0 ? new Subtraction( _calc.get(prev),
				_calc.get(next)) : new Multiplication( new Constant("-1",-1),
				_calc.get(next)));
		case ("^"): return new Power(_calc.get(prev), _calc.get(next));
		case ("SQRT"): return new Power(_calc.get(next), new Constant("0.5",0.5));
		}
		System.err.println("ERROR: could not construnct component!");
		return new Constant("ERROR!!!!!",1.0);
	}
	
	/**
	 * Truncate calc tree after the operation has completed.
	 */
	public void postOperatorTruncate(String operator, int prev, int next)
	{
		switch (operator)
		{
		case ("+"): 
		case ("*"): 
		case ("/"): 
		case ("-"): 
		case ("^"):
		if(_calc.containsKey( prev ))
			_calc.remove( prev );
		if(_calc.containsKey( next ))
			_calc.remove( next );
		break;
		case("SQRT"):
			if(_calc.containsKey( next ))
				_calc.remove( next );
		}
	}
	
}
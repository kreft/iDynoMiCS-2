package expression;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.XmlHandler;

/**
 * 
 * @author baco
 *
 */
public class ExpressionB extends Component {
	
	/**
	 * expression string
	 */
	final String expression;
	
	/**
	 * Recognized operators, in order of evaluation TODO: currently hashtagging
	 * stuff that is likely to be in a variable or constant, discuss consider
	 * other indicator
	 */
	public static String[] operators = new 
			String[]{"#e", "#PI", "EXP", "^", "SQRT", "*", "/", "+", "-"};
	
	/**
	 * constants in this expression
	 */
	public HashMap<String, Double> _constants;
	
	/**
	 * variables in this expression
	 */
	public List<String> _variables = new LinkedList<String>();
	
	/**
	 * The component object
	 */
	final Component _a;
	
	/**
	 * expression constructor
	 * @param expression
	 * @param constants
	 */
	public ExpressionB(String expression, HashMap<String, Double> constants)
	{
		/**
		 * initial construction
		 */
		this.expression = expression.replaceAll("\\s+","");
		if (constants == null)
			constants = new HashMap<String, Double>();
		_constants = constants;
		this._a = build(expression, constants);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param expression
	 */
	public ExpressionB(String expression)
	{
		this(expression, null);
	}
	
	/**
	 * TODO
	 * @param xmlNode
	 */
	public ExpressionB(Node xmlNode)
	{
		Element elem = (Element) xmlNode;
		
		HashMap<String,Double> constantsMap = new HashMap<String,Double>();
		NodeList constants = XmlHandler.getAll(elem, "constant");
		for ( int i = 0; i < constants.getLength(); i++ )
		{
			constantsMap.put(XmlHandler.gatherAttribute(constants.item(i), "name"),
					Double.valueOf(XmlHandler.gatherAttribute(constants.item(i), "value")));
		}
				
		this.expression = XmlHandler.obtainAttribute(elem, "value").replaceAll("\\s+","");
		this._constants = constantsMap;
		this._a = build(expression, constantsMap);
	}
	
	/**
	 * 
	 * @param expression
	 * @param constants
	 * @return 
	 */
	public Component build(String expression, HashMap<String, Double> constants)
	{	
		/**
		 * constant value's
		 */
		this._constants = constants;
		/**
		 * evaluation tree (strings)
		 */
		TreeMap<Integer, String> _eval =  new TreeMap<Integer, String>();
		
		/**
		 * construction tree (components)
		 */
		TreeMap<Integer, Component> _calc = new TreeMap<Integer, Component>();
		
		/**
		 * Subexpressions (braces) embedded in this expression
		 */
		TreeMap<Integer, ExpressionB> _subs = new TreeMap<Integer, ExpressionB>();

		/**
		 * construct treeMaps for correct Component construction
		 */
		constructTreeMaps(_eval, _calc, _subs);

		/**
		 * some final error checking before setting the root Component
		 */
		if (_calc.keySet().isEmpty() || _calc.size() > 1)
		{
			System.err.println("ERROR: unfinished or empty expression root "
					+ "element!!!");
			return new Constant("ERROR!!!!!",1.0);
		} else
			return _calc.get(_calc.firstKey());
	}
	
	public void constructTreeMaps(TreeMap<Integer, String> _eval, 
			TreeMap<Integer, Component> _calc, 
			TreeMap<Integer, ExpressionB> _subs)
	{
			
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
					setEq(o,String.valueOf(expression.subSequence( o, key )), 
							_eval);
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
					setSub(o,key+1, _eval, _subs);
					o = key+1;
				}
			}
		}
		
		/**
		 * build a root expression Component (from tree)
		 */
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
			
			/**
			 * handle sub expressions (braces)
			 */
			if(t.contains("$"))
			{
				_calc.put(i, _subs.get( Integer.valueOf( t.replaceAll(
						"\\$", "") ) )._a);
			}
			
			/**
			 * add . defined constants
			 */
			else if(t.contains("."))
			{
				_calc.put(i, new Constant(t, Double.parseDouble(t)));
			}
	
			/**
			 * variables, hashmap defined constants
			 */
			else if(! (isOperator || t.isEmpty()))
			{
				boolean isConstant = false;
				/**
				 * string defined constants
				 */
				for(String key : _constants.keySet())
				{
					if(key.equals(t))
					{
						_calc.put(i, new Constant(t, _constants.get(key)));
						isConstant = true;
					}
				}
				
				/**
				 * variables
				 */
				if (! isConstant)
					_calc.put(i, new Variable(t));
			}
				
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
					_calc.put(i, constructComponent( operators[j], min, plu, _calc ));
					postOperatorTruncate(operators[j], min, plu, _calc);
				}
			}
		}
	}
		
	/**
	 * load brace free sub sequence into eval tree, start represents starts 
	 * location of substring in overall expressiong
	 * @param equation
	 */
	public void setEq(int start, String equation, TreeMap<Integer,String> _eval)
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
					addVar( o+start,equation.substring( o, key-start ), _eval);
				o = key - start + operLoc.get(key).length();
			}
			
			/**
			 * also add the last one (this means we can't end with an operator
			 * build in a check if we would need to do that)
			 */
			if(o != 0)
				addVar( o+start ,equation.substring( o, equation.length() ), 
						_eval);
			_eval.putAll(operLoc);
		}
	}
	
	/**
	 * adding variable encountered in expression string
	 * @param loc
	 * @param value
	 * @param _eval
	 */
	private void addVar(int loc, String value, TreeMap<Integer,String> _eval)
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
	public void setSub(int start, int end, TreeMap<Integer,String> _eval, 
			TreeMap<Integer,ExpressionB> _subs)
	{
		_subs.put(start, new ExpressionB( 
				expression.substring(start+1, end-1), this._constants));
		_eval.put(start, String.valueOf("$" + start));
	}
	
	/**
	 * adds a constant (with value), removes it from being a variable.
	 * @param key
	 * @param value
	 */
	public void defineConstant(String key, double value)
	{
		this._constants.put(key, value);
		this._variables.remove(this._variables.indexOf(key));
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
		/**
		 * evaluation tree (strings)
		 */
		TreeMap<Integer, String> _eval =  new TreeMap<Integer, String>();
		
		/**
		 * construction tree (components)
		 */
		TreeMap<Integer, Component> _calc = new TreeMap<Integer, Component>();
		
		/**
		 * Subexpressions (braces) embedded in this expression
		 */
		TreeMap<Integer, ExpressionB> _subs = new TreeMap<Integer, ExpressionB>();

		/**
		 * construct treeMaps for correct Component construction
		 */
		constructTreeMaps(_eval, _calc, _subs);
		
		/**
		 * write expression from treeMaps linear in correct order
		 */
		String str = "";
		String t;
		for(Integer e :_eval.keySet())
		{
			t = _eval.get(e);
			if( t.contains("$"))
			{
				str = str + "( " + _subs.get( Integer.valueOf(
						t.replaceAll("\\$", ""))).stringEval() + ") ";
			}
			else
				str = str + _eval.get(e) + " ";
		}
		return str;
	}
	
	/**
	 * Return Component based on operator
	 * @param operator
	 * @param prev
	 * @param next
	 * @return
	 */
	public Component constructComponent(String operator, int prev, int next, 
			TreeMap<Integer,Component> _calc)
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
		case ("#e"): return new Constant("e", Math.E);
		case ("#PI"): return new Constant("PI", Math.PI);
		case ("EXP"): return new Multiplication(_calc.get(prev), 
				new Power(new Constant("10", 10.0), _calc.get(next)));
		}
		System.err.println("ERROR: could not construnct component!");
		return new Constant("ERROR!!!!!",1.0);
	}
	
	/**
	 * Truncate calc tree after the operation has completed.
	 */
	public void postOperatorTruncate(String operator, int prev, int next,
			TreeMap<Integer,Component> _calc)
	{
		switch (operator)
		{
		case ("+"): 
		case ("*"): 
		case ("/"): 
		case ("-"): 
		case ("^"):
		case ("EXP"):
		if(_calc.containsKey( prev ))
			_calc.remove( prev );
		if(_calc.containsKey( next ))
			_calc.remove( next );
		break;
		case("SQRT"):
			if(_calc.containsKey( next ))
				_calc.remove( next );
		case("#e"):
		case("#PI"):
			break;
		}
	}

	@Override
	public String getName() {
		return _a.getName();
	}

	@Override
	public String reportValue(HashMap<String, Double> variables) {
		return _a.reportValue(variables);
	}

	@Override
	public double getValue(HashMap<String, Double> variables) {
		return _a.getValue(variables);
	}

	@Override
	public Component differentiate(String withRespectTo) {
		return _a.differentiate(withRespectTo);
	}
	
}
package expression;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.XmlHandler;
import dataIO.XmlLabel;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class ExpressionB extends Component implements NodeConstructor
{
	
	/**
	 * Expression string.
	 */
	protected String expression;
	
	/**
	 * Recognized operators, in order of evaluation TODO: currently hashtagging
	 * stuff that is likely to be in a variable or constant, discuss consider
	 * other indicator
	 */
	public static String[] operators = new 
			String[]{"#e", "#PI", "EXP", "^", "SQRT", "*", "/", "+", "-"};
	
	/**
	 * Names and values of constants in this expression.
	 */
	public Map<String, Double> _constants;
	
	/**
	 * Names of variables in this expression.
	 */
	public List<String> _variables = new LinkedList<String>();
	
	/**
	 * The component object.
	 */
	protected Component _a;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * expression constructor
	 * @param expression
	 * @param constants
	 */
	public ExpressionB(String expression, Map<String, Double> constants)
	{
		/* Remove all whitespace. */
		this.expression = expression.replaceAll("\\s+","");
		/* Create the constants map if it was not given. */
		if ( constants == null )
			constants = new HashMap<String, Double>();
		this._constants = constants;
		/* Build the component. */
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
		NodeList constants = XmlHandler.getAll(elem, XmlLabel.constant);
		for ( int i = 0; i < constants.getLength(); i++ )
		{
			constantsMap.put(XmlHandler.gatherAttribute(constants.item(i), 
					XmlLabel.nameAttribute),
					Double.valueOf(XmlHandler.gatherAttribute(constants.item(i),
					XmlLabel.valueAttribute)));
		}
				
		this.expression = XmlHandler.obtainAttribute(elem, 
				XmlLabel.valueAttribute).replaceAll("\\s+","");
		this._constants = constantsMap;
		this._a = build(expression, constantsMap);
	}
	
	/*************************************************************************
	 * BUILD METHODS
	 ************************************************************************/
	
	/**
	 * 
	 * @param expression
	 * @param constants
	 * @return 
	 */
	public Component build(String expression, Map<String, Double> constants)
	{	
		/* Constant values */
		this._constants = constants;
		/* Evaluation tree (strings) */
		TreeMap<Integer, String> eval =  new TreeMap<Integer, String>();
		/* Construction tree (components) */
		TreeMap<Integer, Component> calc = new TreeMap<Integer, Component>();
		/* Subexpressions (braces) embedded in this expression. */
		TreeMap<Integer, ExpressionB> subs = new TreeMap<Integer, ExpressionB>();
		/* 
		 * Construct treeMaps for correct Component construction.
		 */
		constructTreeMaps(eval, calc, subs);
		/* Some final error checking before setting the root Component */
		if ( calc.keySet().isEmpty() || calc.size() > 1 )
		{
			System.err.println("ERROR: unfinished or empty expression root "
					+ "element!!!");
			return new Constant("ERROR!!!!!",1.0);
		} else
			return calc.get(calc.firstKey());
	}
	
	public void constructTreeMaps(TreeMap<Integer, String> eval, 
			TreeMap<Integer, Component> calc, 
			TreeMap<Integer, ExpressionB> subs)
	{
		/*
		 * Obtain brace location and count depth.
		 */
		TreeMap<Integer,Integer> brackets = new TreeMap<Integer,Integer>();
		/* Find the open brackets. */
		int c = -1;
		while (true)
		{
			int index = expression.indexOf("(", c+1);
			if (index == -1)
				break;
			brackets.put(index, 1);
			c = index;
		}
		/* Find the close brackets. */
		c = -1;
		while (true)
		{
			int index = expression.indexOf(")", c+1);
			if ( index == -1 )
				break;
			brackets.put(index, -1);
			c = index;
		}
		/* TODO why?. */
		brackets.put(expression.length(), -1);
		
		c = 0;
		int o = 0;
		for ( Integer key : brackets.keySet() )
		{
			/*
			 * what is handled at this level
			 */
			if ( c == 0 && key > 0 )
				setEq(o, String.valueOf(expression.subSequence(o, key)), eval);
			
			/*
			 * what is handled at deeper level (braces)
			 */
			if ( brackets.get(key) != null )
			{
				c += brackets.get(key);
				/*
				 * make sure not to reset o on a descent!
				 */
				if ( c == 1 && brackets.get(key) != -1 )
					o = key;
				if ( c == 0 )
				{
					setSub(o,key+1, eval, subs);
					o = key+1;
				}
			}
		}
		/*
		 * Build a root expression Component (from tree)
		 */
		String t;
		for ( Integer i : eval.keySet() )
		{
			t = eval.get(i);
			
			boolean isOperator = false;
			for ( String op : operators )
			{
				if ( t.contains(op) )
					isOperator = true;
			}
			/*
			 * Handle sub expressions (braces)
			 */
			if ( t.contains("$") )
			{
				int temp = Integer.valueOf( t.replaceAll("\\$", "") );
				calc.put(i, subs.get( temp )._a);
			}
			/*
			 * Add "."-defined constants.
			 */
			else if ( t.contains(".") )
				calc.put(i, new Constant(t, Double.parseDouble(t)));
			/*
			 * Variables, hashmap-defined constants.
			 */
			else if ( ! (isOperator || t.isEmpty()) )
			{
				boolean isConstant = false;
				/* String-defined constants. */
				for ( String key : this._constants.keySet() )
					if ( key.equals(t) )
					{
						calc.put(i, new Constant(t, this._constants.get(key)));
						isConstant = true;
						break;
					}
				/* Variables. */
				if ( ! isConstant )
					calc.put(i, new Variable(t));
			}
		}
		/* Do the operator stuff here. */
		for ( int j = 0; j < operators.length; j++ )
		{
			for ( Integer i : eval.keySet() )
			{
				t = eval.get(i);
				if ( t.contains(operators[j]) )
				{
					int min = (calc.floorKey( i-1 ) != null ? 
							calc.floorKey( i-1 ) : -1);
					int plu = (calc.ceilingKey( i+1 ) != null ? 
							calc.ceilingKey( i+1 ) : -1);
					calc.put(i, constructComponent( operators[j], min, plu, calc ));
					postOperatorTruncate(operators[j], min, plu, calc);
				}
			}
		}
	}
		
	/**
	 * load brace free sub sequence into eval tree, start represents starts 
	 * location of substring in overall expression
	 * @param equation
	 */
	public void setEq(int start, String equation, TreeMap<Integer,String> eval)
	{
		if ( equation.isEmpty() )
			return;
		/* 
		 * Locate operators.
		 */
		TreeMap<Integer,String> operLoc = new TreeMap<Integer,String>();
		for ( String oper : operators )
			operLoc.putAll( identifyStrLoc( equation, oper, start ) );
		/* 
		 * Load non-operator entries into eval tree.
		 */
		int o = 0;
		for(Integer key : operLoc.keySet())
		{
			//NOTE subtract start for correct identification in substring
			if ( key-start != 0 )
				addVar( o+start,equation.substring( o, key-start ), eval);
			o = key - start + operLoc.get(key).length();
		}
		/*
		 * Also add the last one (this means we can't end with an operator
		 * build in a check if we would need to do that)
		 */
		if ( o != 0 )
			addVar( o+start ,equation.substring( o, equation.length() ), eval);
		eval.putAll(operLoc);
	}
	
	/**
	 * adding variable encountered in expression string
	 * @param loc
	 * @param value
	 * @param eval
	 */
	private void addVar(int loc, String value, TreeMap<Integer,String> eval)
	{
		eval.put(loc,value);
		this._variables.add(value);
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
			int index = sequence.indexOf(str, c+1 );
			if (index == -1)
				break;
			seqMap.put(start+index, str);
			c = index;
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
		/* Evaluation tree (strings). */
		TreeMap<Integer, String> eval =  new TreeMap<Integer, String>();
		/* Construction tree (components). */
		TreeMap<Integer, Component> calc = new TreeMap<Integer, Component>();
		/* Subexpressions (braces) embedded in this expression. */
		TreeMap<Integer, ExpressionB> subs = new TreeMap<Integer, ExpressionB>();
		/*
		 * Construct treeMaps for correct Component construction
		 */
		constructTreeMaps(eval, calc, subs);
		/*
		 * Write expression from treeMaps linear in correct order
		 */
		String str = "";
		String t;
		for ( Integer e :eval.keySet() )
		{
			t = eval.get(e);
			if ( t.contains("$") )
			{
				int temp = Integer.valueOf(t.replaceAll("\\$", ""));
				str = str + "( " + subs.get(temp).stringEval() + ") ";
			}
			else
				str = str + eval.get(e) + " ";
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
			TreeMap<Integer,Component> calc)
	{
		switch (operator)
		{
		case ("+"): return Expression.add(calc.get(prev),calc.get(next));
		case ("*"): return Expression.multiply(calc.get(prev),calc.get(next));
		case ("/"): return Expression.divide(calc.get(prev),calc.get(next));
		case ("-"): return (prev >= 0 ? new Subtraction( calc.get(prev),
				calc.get(next)) : new Multiplication( new Constant("-1",-1),
				calc.get(next)));
		case ("^"): return new Power(calc.get(prev), calc.get(next));
		case ("SQRT"): return new Power(calc.get(next), new Constant("0.5",0.5));
		case ("#e"): return Expression.euler();
		case ("#PI"): return Expression.pi();
		case ("EXP"): return new Multiplication(calc.get(prev), 
				new Power(Expression.ten(), calc.get(next)));
		}
		System.err.println("ERROR: could not construnct component!");
		return new Constant("ERROR!!!!!",1.0);
	}
	
	/**
	 * Truncate calc tree after the operation has completed.
	 */
	public void postOperatorTruncate(String operator, int prev, int next,
			TreeMap<Integer,Component> calc)
	{
		switch (operator)
		{
		case ("+"): 
		case ("*"): 
		case ("/"): 
		case ("-"): 
		case ("^"):
		case ("EXP"):
			if(calc.containsKey( prev ))
				calc.remove( prev );
			if(calc.containsKey( next ))
				calc.remove( next );
			break;
		case("SQRT"):
			if(calc.containsKey( next ))
				calc.remove( next );
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
	public String reportEvaluation(Map<String, Double> variables) {
		return _a.reportEvaluation(variables);
	}

	@Override
	public double getValue(Map<String, Double> variables) {
		return _a.getValue(variables);
	}

	@Override
	public Component differentiate(String withRespectTo) {
		return _a.differentiate(withRespectTo);
	}
	
	public void appendVariablesNames(List<String> names)
	{
		this._a.appendVariablesNames(names);
	}

	@Override
	public ModelNode getNode() {
		ModelNode modelNode = new ModelNode(XmlLabel.expression, 
				this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		modelNode.add(new ModelAttribute(XmlLabel.expression, this.expression, null, true));
		return modelNode;
	}

	@Override
	public void setNode(ModelNode node) 
	{
		this.expression = node.getAttribute(XmlLabel.expression).value;
		this._a = build(expression, _constants);
	}

	@Override
	public NodeConstructor newBlank() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChildObject(NodeConstructor childObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return XmlLabel.expression;
	}
}
package expression;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.Log.Tier;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlRef;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class ExpressionB extends Component implements NodeConstructor
{
	private enum Bracket
	{
		OPEN("(", 1),
		CLOSE(")", -1),
		END_OF_EXPRESSION(";", -1);
		
		private String _string;
		
		private int _value;

		Bracket(String str, int val)
		{
			this._string = str;
			this._value = val;
		}
		
		public String getStr()
		{
			return this._string;
		}
		
		public int getVal()
		{
			return this._value;
		}
	}
	
	/**
	 * Expression string.
	 */
	protected String _expression;
	
	/**
	 * Recognized operators, in order of evaluation TODO: currently hashtagging
	 * stuff that is likely to be in a variable or constant, discuss consider
	 * other indicator
	 */
	public static final String[] OPERATORS = new 
			String[]{"#e", "#PI", "EXP", "^", "SQRT", "*", "/", "+", "-"};
	
	/**
	 * Names and values of constants in this expression.
	 */
	private Map<String, Double> _constants;
	
	/**
	 * Names of variables in this expression.
	 */
	private List<String> _variables = new LinkedList<String>();
	
	/**
	 * The component object.
	 */
	protected Component _a;
	/**
	 * 
	 */
	private Tier LOG_LEVEL = Tier.DEBUG;
	
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
		Log.out(LOG_LEVEL, "Making an expression from \""+expression+"\"");
		/* Remove all whitespace. */
		this._expression = expression.replaceAll("\\s+","");
		/* Create the constants map if it was not given. */
		if ( constants == null )
			constants = new HashMap<String, Double>();
		Log.out(LOG_LEVEL, "  Constants defined:");
		for ( String key : constants.keySet() )
			Log.out(LOG_LEVEL, "  -> "+key+" = "+constants.get(key));
		this._constants = constants;
		/* Build the component. */
		this.build();
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
		NodeList constants = XmlHandler.getAll(elem, XmlRef.constant);
		for ( int i = 0; i < constants.getLength(); i++ )
		{
			constantsMap.put(XmlHandler.gatherAttribute(constants.item(i), 
					XmlRef.nameAttribute),
					Double.valueOf(XmlHandler.gatherAttribute(constants.item(i),
					XmlRef.valueAttribute)));
		}
				
		this._expression = XmlHandler.obtainAttribute(elem, 
				XmlRef.valueAttribute).replaceAll("\\s+","");
		this._constants = constantsMap;
		this.build();
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
	public void build()
	{	
		/* Evaluation tree (strings) */
		TreeMap<Integer, String> eval =  new TreeMap<Integer, String>();
		/* Construction tree (components) */
		TreeMap<Integer, Component> calc = new TreeMap<Integer, Component>();
		/* Subexpressions (braces) embedded in this expression. */
		TreeMap<Integer, ExpressionB> subs = new TreeMap<Integer, ExpressionB>();
		/* 
		 * Construct treeMaps for correct Component construction.
		 */
		this.constructTreeMaps(eval, calc, subs);
		/* Some final error checking before setting the root Component */
		if ( calc.keySet().isEmpty() )
		{
			System.err.println("ERROR: empty expression root element!");
		}
		else if ( calc.size() > 1 )
		{
			System.err.println("ERROR: unfinished expression root element!");
		}
		else
			this._a = calc.get(calc.firstKey());
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param eval
	 * @param calc
	 * @param subs
	 */
	public void constructTreeMaps(TreeMap<Integer, String> eval, 
			TreeMap<Integer, Component> calc, 
			TreeMap<Integer, ExpressionB> subs)
	{
		Log.out(LOG_LEVEL, "CONSTRUCT TREE MAPS");
		Log.out(LOG_LEVEL, " eval has size "+eval.size());
		Log.out(LOG_LEVEL, " calc has size "+calc.size());
		Log.out(LOG_LEVEL, " subs has size "+subs.size());
		/*
		 * Obtain brace location and count depth.
		 */
		TreeMap<Integer,Bracket> brackets = new TreeMap<Integer,Bracket>();
		/* Find the open brackets. */
		int c = -1;
		while (true)
		{
			int index = this._expression.indexOf("(", c+1);
			if (index == -1)
				break;
			brackets.put(index, Bracket.OPEN);
			c = index;
		}
		/* Find the close brackets. */
		c = -1;
		while (true)
		{
			int index = this._expression.indexOf(")", c+1);
			if ( index == -1 )
				break;
			brackets.put(index, Bracket.CLOSE);
			c = index;
		}
		brackets.put(this._expression.length(), Bracket.END_OF_EXPRESSION);
		/* Debugging message. */
		Log.out(LOG_LEVEL, this._expression);
		String msg = "";
		for ( int i = 0; i <= this._expression.length(); i++ )
		{
			if ( brackets.containsKey(i) )
				msg += brackets.get(i).getStr();
			else
				msg += " ";
		}
		Log.out(LOG_LEVEL, msg);
		/* */
		int depth = 0;
		int start = 0;
		for ( Integer key : brackets.keySet() )
		{
			Log.out(LOG_LEVEL, "   "+key+" : "+brackets.get(key));
			/*
			 * What is handled at this level.
			 */
			if ( depth == 0 && key > 0 )
			{
				CharSequence cS = this._expression.subSequence(start, key);
				this.setEq(start, String.valueOf(cS), eval);
			}
			/*
			 * What is handled at deeper level (braces).
			 */
			depth += brackets.get(key).getVal();
			/* Make sure not to reset depth on a descent! */
			if ( (depth == 1) && (brackets.get(key) == Bracket.OPEN) )
				start = key;
			if ( depth == 0 )
			{
				Log.out(LOG_LEVEL, "    setting sub "+start+", "+(key+1));
				this.setSub(start, key + 1, eval, subs);
				start = key + 1;
			}
		}
		/*
		 * Build a root expression Component (from tree)
		 */
		Log.out(LOG_LEVEL, "   Build a root expression Component (from tree)");
		Log.out(LOG_LEVEL, "    eval has size "+eval.size());
		Log.out(LOG_LEVEL, "    calc has size "+calc.size());
		Log.out(LOG_LEVEL, "    subs has size "+subs.size());
		String t;
		for ( Integer i : eval.keySet() )
		{
			Log.out(LOG_LEVEL, "    i "+i+" : "+eval.get(i));
			t = eval.get(i);
			boolean isOperator = false;
			for ( String op : OPERATORS )
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
			{
				Log.out(LOG_LEVEL, "      Found a new constant: "+t);
				calc.put(i, new Constant(t, Double.parseDouble(t)));
			}
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
		for ( int j = 0; j < OPERATORS.length; j++ )
		{
			for ( Integer i : eval.keySet() )
			{
				t = eval.get(i);
				if ( t.contains(OPERATORS[j]) )
				{
					int min = (calc.floorKey( i-1 ) != null ? 
							calc.floorKey( i-1 ) : -1);
					int plu = (calc.ceilingKey( i+1 ) != null ? 
							calc.ceilingKey( i+1 ) : -1);
					calc.put(i, constructComponent( OPERATORS[j], min, plu, calc ));
					this.postOperatorTruncate(OPERATORS[j], min, plu, calc);
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
		Log.out(LOG_LEVEL,
				"   Setting equation \""+equation+"\", start at "+start);
		/* 
		 * Locate operators.
		 */
		TreeMap<Integer,String> operLoc = new TreeMap<Integer,String>();
		for ( String oper : OPERATORS )
		{
			TreeMap<Integer,String> locations = 
					this.identifyStrLoc( equation, oper, start );
			//Log.out(LOG_LEVEL, "    found "+locations.size()+" of "+oper);
			operLoc.putAll( locations );
		}
		/*
		 * If there are no operators in this expression, then it is a single
		 * constant or variable so just put this in and return.
		 */
		if ( operLoc.isEmpty() )
		{
			eval.put(0, equation);
			return;
		}
		/* 
		 * Load non-operator entries into eval tree.
		 */
		int o = 0;
		for ( Integer key : operLoc.keySet() )
		{
			Log.out(LOG_LEVEL, "    o "+0+", key "+key+", start "+start);
			//NOTE subtract start for correct identification in substring
			if ( key-start != 0 )
				this.addVar( o+start, equation.substring(o, key-start), eval);
			o = key - start + operLoc.get(key).length();
		}
		/*
		 * Also add the last one (this means we can't end with an operator
		 * build in a check if we would need to do that)
		 */
		if ( o != 0 )
			addVar( o+start ,equation.substring( o, equation.length() ), eval);
		eval.putAll(operLoc);
		Log.out(LOG_LEVEL, "    eq set, eval now has size "+eval.size());
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
	public void setSub(int start, int end, TreeMap<Integer,String> eval, 
			TreeMap<Integer,ExpressionB> subs)
	{
		subs.put(start, new ExpressionB( 
				this._expression.substring(start+1, end-1), this._constants));
		eval.put(start, String.valueOf("$" + start));
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
		Log.out(LOG_LEVEL, "STRING EVAL");
		/* Evaluation tree (strings). */
		TreeMap<Integer, String> eval =  new TreeMap<Integer, String>();
		/* Construction tree (components). */
		TreeMap<Integer, Component> calc = new TreeMap<Integer, Component>();
		/* Subexpressions (braces) embedded in this expression. */
		TreeMap<Integer, ExpressionB> subs = new TreeMap<Integer, ExpressionB>();
		/*
		 * Construct treeMaps for correct Component construction
		 */
		this.constructTreeMaps(eval, calc, subs);
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
		ModelNode modelNode = new ModelNode(XmlRef.expression, 
				this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		modelNode.add(new ModelAttribute(XmlRef.valueAttribute, this._expression, null, true));
		
		for (String con : this._constants.keySet() )
			modelNode.add(getConstantNode(con));
		return modelNode;
	}
	
	public ModelNode getConstantNode(String constant)
	{
		ModelNode modelNode = new ModelNode(XmlRef.constant, 
				this);
		modelNode.requirement = Requirements.ZERO_TO_FEW;
		
		modelNode.add(new ModelAttribute(XmlRef.nameAttribute, constant, null, true));
		modelNode.add(new ModelAttribute(XmlRef.valueAttribute, String.valueOf(this._constants.get(constant)), null, true));
		return modelNode;
	}

	@Override
	public void setNode(ModelNode node) 
	{
		this._expression = node.getAttribute(XmlRef.valueAttribute).value;
		this.build();
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
		return XmlRef.expression;
	}
}
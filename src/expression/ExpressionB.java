package expression;

import java.util.Collection;
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
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
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
	 * Unit
	 */
	protected Unit _unit;
	
	/**
	 * Recognized operators, in order of evaluation TODO: currently hashtagging
	 * stuff that is likely to be in a variable or constant, discuss consider
	 * other indicator
	 * 
	 * NOTE: Order of evaluation!
	 */
	public static final String[] OPERATORS = new 
			String[]{
					"#e",	// euler
					"#PI", 	// pi
					"EXP-", // ten power minus
					"EXP", 	// ten power
					"^-", 	// power minus
					"^", 	// power
					"SQRT-", // square root minus
					"SQRT", // square root
					"*-", 	// multiplication minus
					"*", 	// multiplication
					"/-", 	// division minus
					"/", 	// division
					"+", 	// addition
					"-",	// negative or subtraction TODO make sure this does not cause any problems
					};	
	/**
	 * TODO Work out what this does.
	 */
	private static final String INTERNAL_TAG = "$";
	
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
	private static Tier LOG_LEVEL = Tier.BULK;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Construct from a {@code String} representation of a mathematical
	 * <b>expression</b> and a dictionary of <b>constants</b>.
	 * 
	 * @param expression String expression using variable names, real numbers,
	 * <b>constants</b>, and recognised operators (+, -, *, /, etc).
	 * @param constants Dictionary of constant names to their real number
	 * values.
	 */
	public ExpressionB(String expression, Map<String, Double> constants)
	{
		init(expression, constants);
	}
	
	/**
	 * \brief Construct from a {@code String} representation of a mathematical
	 * <b>expression</b>.
	 * 
	 * @param expression String expression using variable names, real numbers,
	 * and recognised operators (+, -, *, /, etc).
	 */
	public ExpressionB(String expression)
	{
		init(expression, null);
	}
	
	/**
	 * \brief Construct from the XML node of a protocol file.
	 * 
	 * @param xmlNode XML node.
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
				
		init( XmlHandler.obtainAttribute(elem, XmlRef.valueAttribute, this.defaultXmlTag()), 
				constantsMap);
	}
	
	public void init(String expression, Map<String, Double> constants)
	{
		Log.out(LOG_LEVEL, "Making an expression from \""+expression+"\"");
		
		/* check for units */
		String[] split = expression.replace("[", "TEMPSPLITMARKER").split("TEMPSPLITMARKER");
		if ( split.length > 1 )
		{
			this._unit = new Unit(split[1]);
			expression = split[0];
		}
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

		
		if (this._unit != null)
		{
			this._a = new Multiplication(
					new Constant( "unit modifier", this._unit.modifier() ), 
					_a);
		}
	}
	
	public Unit getUnit()
	{
		return this._unit;
	}
	
	/*************************************************************************
	 * BUILD METHODS
	 ************************************************************************/
	
	/**
	 * \brief Builds a {@code Component}, {@link #_a}, from
	 * {@link #_expression} and {@link #_constants}.
	 */
	private void build()
	{	
		/* Evaluation tree (strings). */
		TreeMap<Integer, String> eval =  new TreeMap<Integer, String>();
		/* Construction tree (components). */
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
	 * @param eval Evaluation tree map (strings).
	 * @param calc Construction tree map (components).
	 * @param subs Subexpressions (braces) embedded in {@link #_expression}.
	 */
	private void constructTreeMaps(TreeMap<Integer, String> eval, 
			TreeMap<Integer, Component> calc, 
			TreeMap<Integer, ExpressionB> subs)
	{
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
		if ( Log.shouldWrite(LOG_LEVEL) )
		{
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
		}
		/* */
		int depth = 0;
		int start = 0;
		for ( Integer key : brackets.keySet() )
		{
			if ( Log.shouldWrite(LOG_LEVEL) )
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
				if ( Log.shouldWrite(LOG_LEVEL) )
					Log.out(LOG_LEVEL, "    setting sub "+start+", "+(key+1));
				this.setSub(start, key + 1, eval, subs);
				start = key + 1;
			}
		}
		/*
		 * Build a root expression Component (from tree)
		 */
		String term;
		for ( Integer i : eval.keySet() )
		{
			term = eval.get(i);
			if ( isOperator(term) )
				continue;
			/*
			 * handle units
			 */
			else if ( term.contains("]"))
			{
				Unit unit = new Unit(term);
				calc.put(i, new Constant(unit.unit(), unit.modifier()));
			}
			/*
			 * Handle sub expressions (braces)
			 */
			else if ( term.contains(INTERNAL_TAG) )
			{
				String temp = term.replaceAll("\\"+INTERNAL_TAG, "");
				calc.put(i, subs.get(Integer.valueOf(temp))._a);
			}
			/*
			 * Add "."-defined constants.
			 */
			// NOTE This means that any constants defined as integers (e.g. 0,
			// 1, 2, 10, etc) will not be recognised.
			else if ( term.contains(".") )
			{
				if ( Log.shouldWrite(LOG_LEVEL) )
					Log.out(LOG_LEVEL, "      Found a new constant: "+term);
				calc.put(i, new Constant(term, Double.parseDouble(term)));
			}
			/*
			 * Variables, hashmap-defined constants.
			 */
			else if ( this._constants.containsKey(term) )
				calc.put(i, new Constant(term, this._constants.get(term)));
			else if ( ! term.isEmpty() )
				calc.put(i, new Variable(term));
		}
		/* 
		 * Do the operator stuff here, in the same order that they appear in
		 * OPERATORS.
		 */
		for ( String oper : OPERATORS )
			for ( Integer i : eval.keySet() )
			{
				term = eval.get(i);
				if ( term == oper )
				{
					calc.put(i, constructComponent( term, i, calc ));
					postOperatorTruncate( term, i, calc);
				}
			}
	}
	
	/**
	 * 
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isOperator(String str)
	{
		for ( String oper : OPERATORS )
			if ( oper == str )
				return true;
		return false;
	}
	
	/**
	 * \brief Load a brace-free sub-sequence into the given <b>eval</b> tree.
	 * 
	 * @param start Index of first character in this sub-sequence within the
	 * overall expression.
	 * @param equation 
	 * @param eval 
	 */
	public void setEq(int start, String equation, TreeMap<Integer,String> eval)
	{
		if ( equation.isEmpty() )
			return;
		if ( Log.shouldWrite(LOG_LEVEL) )
		{
			Log.out(LOG_LEVEL,
					"   Setting equation \""+equation+"\", start at "+start);
		}
		/* 
		 * Locate operators.
		 */
		TreeMap<Integer,String> operLoc = new TreeMap<Integer,String>();
		TreeMap<Integer,String> locations;
		String absents = "";
		for ( String oper : OPERATORS )
		{
			locations = identifyStrLoc( equation, oper, start );
			if ( locations.isEmpty() )
				absents += oper + ", ";
			else if ( Log.shouldWrite(LOG_LEVEL) )
				Log.out(LOG_LEVEL, "    found "+locations.size()+" of "+oper);
			operLoc.putAll( locations );
			for (int l : locations.keySet())
				equation = cutString(equation, l-start, oper.length());
		}
		if ( (! absents.isEmpty()) && Log.shouldWrite(LOG_LEVEL) )
		{
			Log.out(LOG_LEVEL, "    found 0 of "+absents);
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
			if ( Log.shouldWrite(LOG_LEVEL) )
				Log.out(LOG_LEVEL, "    o "+0+", key "+key+", start "+start);
			if ( key != start )
				this.addVar( o+start, equation.substring(o, key-start), eval);
			o = key - start + operLoc.get(key).length();
		}
		/*
		 * Also add the last one (this means we can't end with an operator
		 * build in a check if we would need to do that)
		 */
		if ( o != 0 )
			this.addVar(o+start, equation.substring(o,equation.length()), eval);
		eval.putAll(operLoc);
		if ( Log.shouldWrite(LOG_LEVEL) )
			Log.out(LOG_LEVEL, "    eq set, eval now has size "+eval.size());
	}
	
	private String cutString(String string, int start , int numberOfCharacters )
	{
		String out = string.substring(0,start);
		for (int l = 0; l < numberOfCharacters; l++)
			out += " ";
		out += string.substring(start+numberOfCharacters);
		return out;
	}
	
	/**
	 * \brief Register the name of a variable encountered in the 
	 * {@link #_expression} string.
	 * 
	 * @param loc Character index at which this variable name starts.
	 * @param name Name of the variable.
	 * @param eval Evaluation map to add this <b>name</b> to.
	 */
	private void addVar(int loc, String name, TreeMap<Integer,String> eval)
	{
		eval.put(loc, name);
		this._variables.add(name);
	}
	
	/**
	 * \brief Helper method that find all occurrences of a given string in a 
	 * given sequence.
	 * 
	 * @param sequence The large String that we are interrogating.
	 * @param str The small String we are looking for in <b>sequence</b>.
	 * @param offset Offset each character index that becomes a key of the
	 * TreeMap returned by this integer value.
	 * @return TreeMap where all the values are <b>str</b>. The keys are the 
	 * character indices wherever <b>str</b> has been found in <b>sequence</b>.
	 */
	private static TreeMap<Integer,String> identifyStrLoc(
			String sequence, String str, int offset)
	{
		TreeMap<Integer,String> seqMap = new TreeMap<Integer,String>();
		int c = -1;
		while (true)
		{
			int index = sequence.indexOf(str, c + 1);
			if ( index == -1 )
				break;
			seqMap.put(offset + index, str);
			c = index;
		}
		return seqMap;
	}
	
	/**
	 * \brief Store substring (braces) in _subExpressions map, also add it to
	 * eval, prepend the {@link #INTERNAL_TAG} for later identification
	 * 
	 * @param start Character index of the first character in the substring.
	 * @param end Character index of the last character in the substring.
	 * @param eval Evaluation tree
	 * @param subs Subsequence tree
	 */
	private void setSub(int start, int end, TreeMap<Integer,String> eval, 
			TreeMap<Integer,ExpressionB> subs)
	{
		String subString = this._expression.substring(start+1, end-1);
		subs.put(start, new ExpressionB( subString, this._constants));
		eval.put(start, String.valueOf(INTERNAL_TAG + start));
	}
	
	/**
	 * \brief Register a constant (with value), unregisters it from being a
	 * variable.
	 * 
	 * @param name String name of the constant.
	 * @param value Real number value of this constant.
	 */
	public void defineConstant(String name, double value)
	{
		this._constants.put(name, value);
		// TODO Can we use this instead?
		//this._variables.remove(name);
		this._variables.remove(this._variables.indexOf(name));
	}

	/**
	 * Write full equation from tree on screen.
	 */
	public void printEval()
	{
		System.out.println(stringEval());
	}
	
	/**
	 * Return full equation from tree as string.
	 */
	// NOTE we could do a similar thing for TeX
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
		this.constructTreeMaps(eval, calc, subs);
		/*
		 * Write expression from treeMaps linear in correct order
		 */
		String str = "";
		String t;
		for ( Integer e :eval.keySet() )
		{
			t = eval.get(e);
			if ( t.contains(INTERNAL_TAG) )
			{
				int temp = Integer.valueOf(t.replaceAll("\\"+INTERNAL_TAG, ""));
				str = str + "( " + subs.get(temp).stringEval() + ") ";
			}
			else
				str = str + eval.get(e) + " ";
		}
		return str;
	}
	
	/**
	 * \brief Combine two components into one.
	 * 
	 * @param operator String tag for a kind of operator.
	 * @param prev Index for the first character of the left-hand component in
	 * the original {@link #_expression} string.
	 * @param next Index for the first character of the right-hand component in
	 * the original {@link #_expression} string.
	 * @return New component combining the previous and next components.
	 */
	private static Component constructComponent(String operator,
			int here, TreeMap<Integer,Component> calc)
	{
		int prev = (calc.floorKey( here-1 ) == null ? 
				-1 : calc.floorKey( here-1 ) );
		int next = (calc.ceilingKey( here+1 ) == null ? 
				-1 : calc.ceilingKey( here+1 ) );
		if ( Log.shouldWrite(LOG_LEVEL) )
		{
			Log.out(LOG_LEVEL,
					"operator "+operator+", prev "+prev+", next "+next);
		}
		switch (operator)
		{
		case ("+"): 
			return Expression.add(calc.get(prev),calc.get(next));
		case ("*"): 
			return Expression.multiply(calc.get(prev),calc.get(next));
		case ("*-"): 
			
			return Expression.multiply(calc.get(prev),flipSign(calc.get(next)));
		case ("/"): 
			return Expression.divide(calc.get(prev),calc.get(next));
		case ("/-"): 
			return Expression.divide(calc.get(prev),flipSign(calc.get(next)));
		case ("-"): 
			// TODO here we should really just change the sign of next
			// Bas [16.06.16] component.changeSign does not seem to work
			if (prev >= 0 )
				return new Subtraction( calc.get(prev), calc.get(next));
			else
			{
				return flipSign(calc.get(next));
			}
		case ("^"): 
			return new Power(calc.get(prev), calc.get(next));
		case ("^-"): 
			return new Power(calc.get(prev), flipSign(calc.get(next)));
		case ("SQRT"): 
			return new Power(calc.get(next), new Constant("0.5",0.5));
		case ("SQRT-"): 
			return new Power(flipSign(calc.get(next)), new Constant("0.5",0.5));
		case ("#e"): 
			return Expression.euler();
		case ("#PI"): 
			return Expression.pi();
		case ("EXP"): 
			return new Multiplication(calc.get(prev), 
				new Power(Expression.ten(), calc.get(next)));
		case ("EXP-"): 
			return new Multiplication(calc.get(prev), 
				new Power(Expression.ten(), flipSign(calc.get(next))));
		}
		System.err.println("ERROR: could not construnct component!");
		return new Constant("ERROR!",1.0);
	}
	
	private static Component flipSign(Component component)
	{
		return new Multiplication(component, new Constant("-1",-1.0));
	}
	
	/**
	 * Truncate calc tree after the operation has completed.
	 */
	private static void postOperatorTruncate(String operator,
			int here, TreeMap<Integer,Component> calc)
	{
		int prev = (calc.floorKey( here-1 ) == null ? 
				-1 : calc.floorKey( here-1 ) );
		int next = (calc.ceilingKey( here+1 ) == null ? 
				-1 : calc.ceilingKey( here+1 ) );
		switch (operator)
		{
		case ("+"): 
		case ("*"): 
		case ("/"): 
		case ("^"):
		case ("EXP"):
		case ("*-"): 
		case ("/-"): 
		case ("^-"):
		case ("EXP-"):
			if ( calc.containsKey( prev ) )
				calc.remove( prev );
			if ( calc.containsKey( next ) )
				calc.remove( next );
			break;
		case("SQRT"):
		case("SQRT-"):
			if ( calc.containsKey( next ) )
				calc.remove( next );
			break;
		case ("-"): 
			if (prev >= 0 )
			{
				if ( calc.containsKey( prev ) )
					calc.remove( prev );
				if ( calc.containsKey( next ) )
					calc.remove( next );
			}
			else
			{
				if ( calc.containsKey( next ) )
					calc.remove( next );
			}
		case("#e"):
		case("#PI"):
			break;	
		}
	}

	@Override
	public String getName()
	{
		return this._a.getName();
	}

	@Override
	public String reportEvaluation(Map<String, Double> variables) {
		return this._a.reportEvaluation(variables);
	}

	@Override
	protected double calculateValue(Map<String, Double> variables) 
	{
		return this._a.getValue(variables);
	}
	
	/**
	 * get Value for expressions where no variables are used
	 * @return double
	 */
	public double getValue()
	{
		return this.getValue(new HashMap<String,Double>());
	}

	@Override
	public Component differentiate(String withRespectTo) {
		return this._a.differentiate(withRespectTo);
	}
	
	public void appendVariablesNames(Collection<String> names)
	{
		this._a.appendVariablesNames(names);
	}

	@Override
	public ModelNode getNode()
	{
		ModelNode modelNode = new ModelNode(XmlRef.expression, 
				this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		modelNode.add(new ModelAttribute(XmlRef.valueAttribute, this._expression, null, true));
		
		for (String con : this._constants.keySet() )
			modelNode.add(getConstantNode(con));
		return modelNode;
	}
	
	public ModelNode getConstantNode(String constant)
	{
		ModelNode modelNode = new ModelNode(XmlRef.constant, 
				this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);
		
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
	
	public void removeNode()
	{
		// TODO
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
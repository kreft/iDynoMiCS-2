package expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import aspect.AspectInterface;
import dataIO.XmlHandler;
import expression.arithmetic.*;
import expression.arithmetic.Unit.SI;
import expression.logic.*;
import idynomics.Idynomics;
import instantiable.object.InstantiableMap;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import utility.GenericTrio;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Expression extends Component implements Settable
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
					"e-",	// eg 12e-5
					"E-",	// eg 12E-5
					"#PI", 	// pi
					"SIGN-", // signum function minus what follows
					"SIGN", // signum function
					"LOG", // natural logorithm
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
					"!=",	// previous .. not equal to .. following
					"=", 	// previous .. equals .. following
					"LT", 	// previous .. less than .. following
					"GT",	// previous .. greater than .. following
					"NOT",	// inverts .. following boolean
					"AND", 	// previous .. and .. following
					"OR", 	// previous .. and/or .. following
					"XOR", 	// previous .. exclusive or .. following
					"XNOR", // previous .. matches .. following
					};	
	/**
	 * TODO Work out what this does.
	 */
	private static final String INTERNAL_TAG = "$";
	
	/**
	 * Names and values of constants in this expression.
	 */
	private InstantiableMap<String, Double> _constants = new InstantiableMap<String,Double>(
			String.class, Double.class, XmlRef.nameAttribute, 
			XmlRef.valueAttribute, XmlRef.constants, XmlRef.constant, true);
	
	/**
	 * Names of variables in this expression.
	 */
	private List<String> _variables = new LinkedList<String>();
	
	protected Elemental _el;
	
	/**
	 * The component object.
	 */
	protected Component _a;

	private Settable _parentNode;
	
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
	public Expression(String expression, Map<String, Double> constants)
	{
		super(Type.omnifarious);
		init(expression, constants);
	}
	
	/**
	 * \brief Construct from a {@code String} representation of a mathematical
	 * <b>expression</b>.
	 * 
	 * @param expression String expression using variable names, real numbers,
	 * and recognised operators (+, -, *, /, etc).
	 */
	public Expression(String expression)
	{
		super(Type.omnifarious);
		if (expression == null || expression.equals("") )
			expression = Helper.obtainInput("", XmlRef.expression);
		init(expression, null);
	}
	
	/**
	 * \brief Construct from the XML node of a protocol file.
	 * 
	 * @param xmlNode XML node.
	 */
	public Expression(Node xmlNode)
	{
		super(Type.omnifarious);
		Element elem = (Element) xmlNode;

		this._constants.instantiate(elem, this);
				
		init( XmlHandler.obtainAttribute(elem, XmlRef.valueAttribute, this.defaultXmlTag()), 
				this._constants);
	}
	
	public void init(String expression, Map<String, Double> constants)
	{
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
		this._constants.putAll(constants);
		/* Build the component. */
		this.build();

	}
	
	private double modifier( Map<SI,GenericTrio<SI,String,Double>> unitSystem )
	{
		if (this._unit != null)
			if ( unitSystem != null)
				return this._unit.format(unitSystem);
			else
				return this._unit.modifier();
		return 1.0;
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
		TreeMap<Integer, Elemental> calc = new TreeMap<Integer, Elemental>();
		/* Subexpressions (braces) embedded in this expression. */
		TreeMap<Integer, Expression> subs = new TreeMap<Integer, Expression>();
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
		{
			this._el = calc.get(calc.firstKey());
			if( _el instanceof Component)
				this._a = (Component) _el;
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param eval Evaluation tree map (strings).
	 * @param calc Construction tree map (components).
	 * @param subs Subexpressions (braces) embedded in {@link #_expression}.
	 */
	private void constructTreeMaps(TreeMap<Integer, String> eval, 
			TreeMap<Integer, Elemental> calc, 
			TreeMap<Integer, Expression> subs)
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

		int depth = 0;
		int start = 0;
		for ( Integer key : brackets.keySet() )
		{
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
			else if ( Helper.dblParseable( term ) )
				calc.put(i, new Constant(term, Double.parseDouble(term)));
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
			operLoc.putAll( locations );
			for (int l : locations.keySet())
				equation = cutString(equation, l-start, oper.length());
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
			TreeMap<Integer,Expression> subs)
	{
		String subString = this._expression.substring(start+1, end-1);
		subs.put(start, new Expression( subString, this._constants));
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
		this._variables.remove(name);
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
	// NOTE we could do a similar thing for TeX TODO tex for reporting?
	public String stringEval()
	{
		/* Evaluation tree (strings). */
		TreeMap<Integer, String> eval =  new TreeMap<Integer, String>();
		/* Construction tree (components). */
		TreeMap<Integer, Elemental> calc = new TreeMap<Integer, Elemental>();
		/* Subexpressions (braces) embedded in this expression. */
		TreeMap<Integer, Expression> subs = new TreeMap<Integer, Expression>();
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
	private static Elemental constructComponent(String operator,
			int here, TreeMap<Integer,Elemental> calc)
	{
		int prev = (calc.floorKey( here-1 ) == null ? 
				-1 : calc.floorKey( here-1 ) );
		int next = (calc.ceilingKey( here+1 ) == null ? 
				-1 : calc.ceilingKey( here+1 ) );
		switch (operator)
		{
		case ("+"): 
			return Arithmetic.add((Component) calc.get(prev),(Component) calc.get(next));
		case ("*"): 
			return Arithmetic.multiply((Component) calc.get(prev),(Component) calc.get(next));
		case ("*-"): 
			
			return Arithmetic.multiply((Component) calc.get(prev),flipSign((Component) calc.get(next)));
		case ("/"): 
			return Arithmetic.divide((Component) calc.get(prev),(Component) calc.get(next));
		case ("/-"): 
			return Arithmetic.divide((Component) calc.get(prev),flipSign((Component) calc.get(next)));
		case ("^"): 
			return new Power((Component) calc.get(prev), (Component) calc.get(next));
		case ("^-"): 
			return new Power((Component) calc.get(prev), flipSign((Component) calc.get(next)));
		case ("SQRT"): 
			return new Power((Component) calc.get(next), new Constant("0.5",0.5));
		case ("SQRT-"): 
			return new Power(flipSign((Component) calc.get(next)), new Constant("0.5",0.5));
		case ("#e"): 
			return Arithmetic.euler();
		case ("#PI"): 
			return Arithmetic.pi();
		case ("EXP"): 
			return new Multiplication((Component) calc.get(prev), 
				new Power(Arithmetic.ten(), (Component) calc.get(next)));
		case ("EXP-"): 
		case ("e-"): 
		case ("E-"): 
			return new Multiplication((Component) calc.get(prev), 
				new Power(Arithmetic.ten(), flipSign((Component) calc.get(next))));
		case ("LOG"): 
			return new Logarithm((Component) calc.get(next),Arithmetic.ten());
		case ("SIGN"): 
			return 	new Sign((Component) calc.get(next));
		case ("SIGN-"): 
			return new Sign(flipSign((Component) calc.get(next)));
		case ("-"): 
			// TODO here we should really just change the sign of next
			// Bas [16.06.16] component.changeSign does not seem to work
			if (prev >= 0 )
				return new Subtraction((Component)  calc.get(prev),(Component)  calc.get(next));
			else
			{
				return flipSign((Component) calc.get(next));
			}
		case ("!="): 
			return new LogicNotEqual(calc.get(prev), calc.get(next));
		case ("="): 
			return new LogicEqual(calc.get(prev), calc.get(next));
		case ("LT"): 
			return new LogicLessThan(calc.get(prev), calc.get(next));
		case ("GT"): 
			return new LogicGreaterThan(calc.get(prev), calc.get(next));
		case ("NOT"): 
			return new LogicNot(calc.get(next));
		case ("AND"): 
			return new LogicAnd(calc.get(prev), calc.get(next));
		case ("OR"): 
			return new LogicOr(calc.get(prev), calc.get(next));
		case ("XOR"): 
			return new LogicXor(calc.get(prev), calc.get(next));
		case ("XNOR"): 
			return new LogicXnor(calc.get(prev), calc.get(next));
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
			int here, TreeMap<Integer,Elemental> calc)
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
		case ("e-"): 
		case ("E-"): 
		case ("!="): 
		case ("="): 
		case ("LT"): 
		case ("GT"): 
		case ("AND"): 
		case ("OR"): 
		case ("XOR"): 
		case ("XNOR"): 
			if ( calc.containsKey( prev ) )
				calc.remove( prev );
			if ( calc.containsKey( next ) )
				calc.remove( next );
			break;
		case("SQRT"):
		case("SQRT-"):
		case ("LOG"):
		case("SIGN"):
		case("SIGN-"):
		case ("NOT"): 
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
		return new Multiplication(_a, new Constant("unit Conversion", 
				this.format( Idynomics.unitSystem ))).reportEvaluation(variables);
	}

	/**
	 * NOTE: calculating expression but not correcting for unit system
	 */
	@Override
	protected double calculateValue(Map<String, Double> variables) 
	{
		return this._a.getValue(variables);
	}

	@Override
	public Object evaluate(AspectInterface subject) {
		return this._el.evaluate(subject);
	}
	
	/**
	 * get Value for expressions where no variables are used, applying iDynoMiCS
	 * base units
	 * @return double
	 */
	public double getValue()
	{
		return this.format( Idynomics.unitSystem );
	}
	
	/**
	 * get Value for expressions where variables are used, applying iDynoMiCS
	 * base units
	 * @return double
	 */
	public double format( Map<String,Double> variables, 
			Map<SI,GenericTrio<SI, String, Double>> unitSystem )
	{
		return this.getValue( variables ) * this.modifier( unitSystem );
	}
	
	/**
	 * get Value for expressions where no variables are used
	 * @return double
	 */
	public double format( Map<SI,GenericTrio<SI, String, Double>> unitSystem )
	{
		return this.format(new HashMap<String,Double>(), unitSystem );
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
	public Module getModule()
	{
		Module modelNode = new Module(XmlRef.expression, 
				this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		modelNode.add(new Attribute(XmlRef.valueAttribute, this._expression, null, true));
		
//		for (String con : this._constants.keySet() )
//			modelNode.add(getConstantNode(con));
		
		modelNode.add( this._constants.getModule() );
		
		return modelNode;
	}
	
	public Module getConstantNode(String constant)
	{
		Module modelNode = new Module(XmlRef.constant, 
				this);
		modelNode.setTitle(constant);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);
		
		modelNode.add(new Attribute(XmlRef.nameAttribute, constant, null, true));
		modelNode.add(new Attribute(XmlRef.valueAttribute, String.valueOf(this._constants.get(constant)), null, true));
		return modelNode;
	}

	@Override
	public void setModule(Module node) 
	{
		/* Set values for all child nodes. */
		Settable.super.setModule(node);
		
		this._expression = node.getAttribute(XmlRef.valueAttribute).getValue();
		this.build();
	}

	public void removeModule(String specifier)
	{
		if (this._constants.containsKey(specifier))
			this._constants.remove(specifier);
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return XmlRef.expression;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}
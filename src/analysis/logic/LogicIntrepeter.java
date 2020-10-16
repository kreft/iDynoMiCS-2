package analysis.logic;

import java.util.Map;
import java.util.TreeMap;

import expression.ComponentNumerical;

public class LogicIntrepeter {

	public static final String[] BOOLEANOPERATORS = new 
			String[]{
					" NOT ",	// inverts .. following
					" AND ", 	// previous .. and .. following
					" OR ", 	// previous .. and/or .. following
					" XOR ", 	// previous .. exclusive or .. following
					" XNOR ", 	// previous .. matches .. following

					};	
	
	public static final String[] COMPARATIVEOPERATORS = new 
			String[]{
					" != ",		// previous .. not equal to .. following
					" = ", 		// previous .. and .. following
					" LT ", 	// previous .. less than .. following
					" GT ",	 	// previous .. greater than .. following

					};	
	
	public static LogicalExpression build(String logicalExpression,  Map<String, Double> constants)
	{

		/* Remove all whitespace. */
//		this._expression = expression.replaceAll("\\s+","");
		int prev = 0;
		int index = logicalExpression.indexOf(" AND ", prev);
		CharSequence cS = logicalExpression.subSequence(start, key);
		return new LogicalExpression();
	}
	
	private static void postOperatorTruncate(String operator,
			int here, TreeMap<Integer,ComponentNumerical> calc)
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
		case ("LOG"):
		case("SIGN"):
		case("SIGN-"):
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
	
}

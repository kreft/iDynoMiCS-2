package analysis.logic;

import java.util.Map;

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
//		CharSequence cS = logicalExpression.subSequence(start, key);
		return new LogicalExpression();
	}

	
}

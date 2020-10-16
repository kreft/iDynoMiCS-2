package analysis.logic;

import java.util.Map;
import java.util.function.Predicate;

public class LogicalExpression {

	private Predicate<Object> test = null;
	
	private Object a;
	
	private Object b;
		
	public Boolean evaluate()
	{
		return true;
	}
}

package analysis.specs;

import aspect.AspectInterface;

public abstract interface Specification {
	
	public enum Clasification 
	{
		number,
		bool,
		factor,
		string;
	}
	
	public abstract String header();
	
	public abstract Object value(AspectInterface subject);
	
}

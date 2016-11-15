package analysis.filter;

import aspect.AspectInterface;

public interface Filter {

	public String stringValue(AspectInterface subject);
	
	public String header();
	
	public boolean match(AspectInterface subject);

}

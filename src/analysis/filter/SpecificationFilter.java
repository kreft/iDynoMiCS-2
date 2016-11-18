package analysis.filter;

import java.util.function.Predicate;

import aspect.AspectInterface;

public class SpecificationFilter implements Filter 
{
	private String header;
	private Predicate<Object> test;
	private String property;
	
	public SpecificationFilter(String property, Predicate<Object> predicate) {
		this.header =  property + predicate.toString();
		this.property = property;
		this.test = predicate;
	}

	public String stringValue(AspectInterface subject)
	{
		return String.valueOf( this.match(subject) );
	}
	
	public String header()
	{
		return this.header;
	}

	public boolean match(AspectInterface subject)
	{
		return test.test(subject.getValue(property));
	}

}

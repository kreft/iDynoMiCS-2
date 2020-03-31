package analysis.filter;

import java.util.function.Predicate;

import aspect.AspectInterface;

/**
 * The specification filter obtains a single value from an aspect interface
 * and tests this value for a given predicate, returns true when the value
 * passes the predicate, false otherwise.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
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

	public String stringValue(AspectInterface subject, String format)
	{
		return String.format( screenLocale,  format, this.match(subject) );
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

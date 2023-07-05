package analysis.filter;

import java.util.Locale;

import aspect.AspectInterface;

/**
 * General interface for filter classes
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public interface Filter {
	
	public static Locale screenLocale = Locale.ENGLISH;

	/**
	 * String representation of the filter value for the passed object, can be
	 * numeric for category filter, boolean for specification- or multi-filter
	 * or any other format for a value filter
	 */
	public String stringValue(AspectInterface subject, String format);
	
	/**
	 * returns the table header for this filter.
	 */
	public String header();
	
	/**
	 * returns the boolean value in case of specification- or multi-filter,
	 * returns valse for filters without boolean representation
	 */
	public boolean match(AspectInterface subject);

}

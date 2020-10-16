package analysis;

import analysis.filter.AndFilter;
import analysis.filter.CategoryFilter;
import analysis.filter.Filter;
import analysis.filter.SoluteFilter;
import analysis.filter.SpecificationFilter;
import analysis.filter.TimerFilter;
import analysis.filter.ValueFilter;
import compartment.Compartment;
import gereralPredicates.IsEquals;
import gereralPredicates.IsLarger;
import gereralPredicates.IsSame;
import gereralPredicates.IsSmaller;

/**
 * Identify and create Filters based on their operators.
 * 
 * , splits the individual filters captured in a CategoryFilter
 * + splits the individual filters captured in a AndFilter
 * 
 * # calls the timer filter for current time
 * 
 * specification filters:
 * == isSame (always)
 * = isEquals (if numeric) otherwise isSame
 * > isLarger
 * < isSmaller
 * 
 * NO operator results in a ValueFilter.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Sankalp Arya (stxsa33@nottingham.ac.uk), UoN, Nottingham.
 *
 */
public class FilterLogic {

	public static Filter filterFromString(String filter, Compartment comp)
	{
		filter = filter.replaceAll("\\s","");
		if ( filter.contains( "%" ) )
		{
			return new SoluteFilter( filter, comp );
		}
		else if ( filter.contains( "#" ))
		{
			String f = filter.split( "#" )[1];
			return new TimerFilter( f );
		}
		else
			return filterFromString(filter);
	}
	
	public static Filter filterFromString(String filter)
	{
		filter = filter.replaceAll("\\s","");
		if ( filter.contains( "," ) )
		{
			return new CategoryFilter( filter );
		}
		else if ( filter.contains( "+" ) )
		{
			return new AndFilter( filter );
		}
		else if ( filter.contains( "==" ) )
		{
			/* character comparison (same text) even for numeric values */
			String[] f = filter.split( "==" );
			return new SpecificationFilter( f[0], 
					new IsSame( String.valueOf( f[1] ) ) );
		}
		else if ( filter.contains( "=" ) )
		{
			String[] f = filter.split( "=" );
			try
			{
				/* numeric comparison (same value eg. 0 = 0.0 = 0L = 0.0f ) */
				return new SpecificationFilter( f[0], 
						new IsEquals( Double.valueOf( f[1] ) ) );
			}
			catch ( NumberFormatException e)
			{
				/* character comparison (same text) */
				return new SpecificationFilter( f[0], 
						new IsSame( String.valueOf( f[1] ) ) );
			}
		}
		else if ( filter.contains( ">" ) || filter.contains( "GT" )  )
		{
			String[] f = filter.split( ">" );
			if( f.length == 1 )
				f = filter.split( "GT" );
			return new SpecificationFilter( f[0], 
					new IsLarger( Double.valueOf( f[1] ) ) );
		}
		else if ( filter.contains( "<" ) || filter.contains( "LT" )  )
		{
			String[] f = filter.split( "<" );
			if( f.length == 1 )
				f = filter.split( "LT" );
			return new SpecificationFilter( f[0], 
					new IsSmaller( Double.valueOf( f[1] ) ) );
		}
		else
			return new ValueFilter( filter );
	}
}

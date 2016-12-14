package analysis;

import analysis.filter.CategoryFilter;
import analysis.filter.Filter;
import analysis.filter.MultiFilter;
import analysis.filter.SpecificationFilter;
import analysis.filter.ValueFilter;
import gereralPredicates.*;

public class FilterLogic {

	public static Filter filterFromString(String filter)
	{
		if ( filter.contains( "," ) )
		{
			String[] f = filter.split( "," );
			return new CategoryFilter( f );
		}
		else if ( filter.contains( "+" ) )
		{
			String[] f = filter.split( "\\+" );
			return new MultiFilter( f );
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
		else if ( filter.contains( ">" ) )
		{
			String[] f = filter.split( ">" );
			return new SpecificationFilter( f[0], 
					new IsLarger( Double.valueOf( f[1] ) ) );
		}
		else if ( filter.contains( "<" ) )
		{
			String[] f = filter.split( "<" );
			return new SpecificationFilter( f[0], 
					new IsSmaller( Double.valueOf( f[1] ) ) );
		}
		else
			return new ValueFilter( filter );
	}
}
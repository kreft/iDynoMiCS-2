package analysis.filter;

import java.util.LinkedList;

import analysis.FilterLogic;
import aspect.AspectInterface;

/**
 * The And-filter returns true if all nested filters return true, returns
 * false otherwise, use in combination with the specification filter.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class AndFilter implements Filter
{

	private String header = "";
	private LinkedList<Filter> filters = new LinkedList<Filter>();
	
	public AndFilter(String... filters) 
	{
		if (filters.length == 1)
			filters = filters[0].split( "\\+" );
		for ( String s : filters)
		{
			Filter filt = FilterLogic.filterFromString(s);
			this.filters.add( filt );
			this.header += filt.header() + " + ";
		}
		this.header = String.valueOf( this.header.subSequence( 0, 
				this.header.length() - 2 ) );
	}

	@Override
	public String stringValue(AspectInterface subject, String format)
	{
		return String.format( screenLocale, format, this.match( subject ) );
	}

	@Override
	public String header() 
	{
		return this.header;
	}
	
	@Override
	public boolean match(AspectInterface subject)
	{
		for ( Filter f : filters)
			if ( ! f.match( subject ) )
				return false;
		return true;
	}


}

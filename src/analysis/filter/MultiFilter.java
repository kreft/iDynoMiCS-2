package analysis.filter;

import java.util.LinkedList;

import analysis.FilterLogic;
import aspect.AspectInterface;

public class MultiFilter implements Filter
{

	private String header = "";
	private LinkedList<Filter> filters = new LinkedList<Filter>();
	
	public MultiFilter(String[] filters) 
	{
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
	public String stringValue(AspectInterface subject) 
	{
		return String.valueOf( this.match( subject ) );
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

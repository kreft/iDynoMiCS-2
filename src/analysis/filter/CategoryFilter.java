package analysis.filter;

import java.util.LinkedList;

import analysis.FilterLogic;
import aspect.AspectInterface;

public class CategoryFilter implements Filter 
{

	private String header;
	private LinkedList<Filter> filters = new LinkedList<Filter>();
	
	public CategoryFilter(String[] filters) {
		int i = 0;
		this.header = "[" + i++ + "]: Other";
		for ( String s : filters)
		{
			Filter filt = FilterLogic.filterFromString(s);
			this.filters.add( filt );
			this.header += " [" + i++ + "]: " + filt.header();
		}

	}

	public String stringValue(AspectInterface subject)
	{
		for ( int i = 0; i < filters.size(); i++)
			if (this.filters.get(i).match(subject) )
				return String.valueOf(i+1);
		return String.valueOf(0);
			
	}
	
	public String header()
	{
		return this.header;
	}

	@Override
	public boolean match(AspectInterface subject) 
	{
		return false;
	}

}

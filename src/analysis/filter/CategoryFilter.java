package analysis.filter;

import java.util.LinkedList;

import analysis.FilterLogic;
import aspect.AspectInterface;

public class CategoryFilter implements Filter 
{

	private String header;
	private LinkedList<Filter> filters = new LinkedList<Filter>();
	
	public CategoryFilter(String[] filters) {
		for ( String s : filters)
			this.filters.add( FilterLogic.filterFromString(s) );
	}

	public String stringValue(AspectInterface subject)
	{
		for ( int i = 1; i <= filters.size(); i++)
			if (this.filters.get(i).match(subject) )
				return String.valueOf(i);
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

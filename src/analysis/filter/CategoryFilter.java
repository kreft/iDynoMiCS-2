package analysis.filter;

import java.util.LinkedList;

import analysis.FilterLogic;
import aspect.AspectInterface;

/**
 * The Category filter assigns the object to a category (numeric representation)
 * The object is assigned to a category if the filter is matched. if no filter
 * is matched the category 0 is assigned (other). Note that the order is 
 * important since assignment to a category does not automatically mean no other
 * filter is matched. This fact can be used to create bins. For example:
 * 
 * filter: 	 <10, 	<20, 	 <30
 * category: x<10, 	10<x<20, 20<x<30, 30<x
 * number:   1		2		 3		  0
 * 
 * Note that supplying the filter in opposed order would result in only
 * assignment to category 3 and 0 since <10 and <20 would have already returned
 * true on <30.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class CategoryFilter implements Filter 
{

	private String header;
	private LinkedList<Filter> filters = new LinkedList<Filter>();
	
	public CategoryFilter(String... filters) {
		if (filters.length == 1 )
			filters = filters[0].split( "," );
		int i = 0;
		this.header = "[" + i++ + "]: Other";
		for ( String s : filters)
		{
			Filter filt = FilterLogic.filterFromString(s);
			this.filters.add( filt );
			this.header += " [" + i++ + "]: " + filt.header();
		}

	}

	public String stringValue(AspectInterface subject, String format)
	{
		for ( int i = 0; i < filters.size(); i++)
			if (this.filters.get(i).match(subject) )
				return String.valueOf(i+1);
		return String.format( screenLocale,  format, 0);
			
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
	
	public int size()
	{
		return filters.size()+1;
	}

}

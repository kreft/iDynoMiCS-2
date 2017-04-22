package spatialRegistry.splitTree;

import java.util.List;
import java.util.function.Predicate;

public class Area implements Predicate<Area> {
	
	final double[] low;
	
	final double[] high;
	
	boolean periodic = true;
	
	public Area(double[] low, double[] high)
	{
		this.low = low;
		this.high = high;
	}
	
	public  void add(List<Area> entries) {
	}
	
	/**
	 * test whether this area is NOT hitting the input area
	 */
	@Override
	public boolean test(Area area) 
	{
		/* periodic set use the more expensive periodic check */
		for (int i = 0; i < low.length; i++)
			if ( ( periodic || area.periodic) ? periodic(area, i) : normal(area, i) )
				return true;
		return false;		
	}
	
	public boolean sectorTest(Area area) 
	{
		/* periodic set use the more expensive periodic check */
		for (int i = 0; i < low.length; i++)
			if ( sector(area, i) )
				return true;
		return false;		
	}
	
	private boolean normal(Area area, int dim)
	{
		return ( low[dim] > area.high[dim] || 
				high[dim] < area.low[dim] );
	}
	
	private boolean sector(Area area, int dim)
	{
		return ( low[dim] > area.low[dim] || 
				high[dim] < area.low[dim] );
	}
	
	private boolean periodic(Area area, int dim)
	{
		/* if this is not passing a periodic boundary in this dimension */
		if ( low[dim] < high[dim] ) 
		{
			/* if the partner area is also not passing a periodic boundary in
			 * this dimension  */
			if ( area.low[dim] < area.high[dim] )
			{
				this.periodic = false;
				return normal(area, dim);
			}
			else
			{
				/* if the partner area is passing a periodic boundary in
				 * this dimension  */
				return ( low[dim] > area.high[dim] && 
						high[dim] < area.low[dim] );	
			}
		}
		/* if this is passing a periodic boundary in this dimension */
		else 
		{
			/* if the partner area is not passing a periodic boundary in
			 * this dimension  */
			if ( area.low[dim] < area.high[dim] )
			{
				return ( area.low[dim] > high[dim] && 
						area.high[dim] < low[dim] );	
			}
			else
			{
			/* if the partner area is also passing a periodic boundary in
			 * this dimension  */
				return ( low[dim] < area.high[dim] &&
						high[dim] > area.low[dim] );
			}
		}
	}

}
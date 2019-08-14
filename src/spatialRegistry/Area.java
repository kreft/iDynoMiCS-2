package spatialRegistry;

import java.util.function.Predicate;

import linearAlgebra.Vector;

public class Area implements Predicate<Area> {
	
	private final double[] low;
	
	private final double[] high;
	
	private boolean[] periodic;
	
	public Area(double[] low, double[] high)
	{
		this.low = low;
		this.high = high;
		this.periodic = Vector.setAll(new boolean[low.length], true);
	}
	
	public Area(double[] low, double[] high, boolean[] periodic)
	{
		this.low = low;
		this.high = high;
		this.periodic = periodic;
	}

	/**
	 * test whether this area is NOT hitting the input area
	 */
	@Override
	public boolean test(Area area) 
	{
		/* periodic set use the more expensive periodic check */
		for (int i = 0; i < getLow().length; i++)
			if ( ( periodic[i] || area.periodic[i]) ? periodic(area, i) : normal(area, i) )
				return true;
		return false;		
	}
	
	public boolean sectorTest(Area area) 
	{
		/* periodic set use the more expensive periodic check */
		for (int i = 0; i < getLow().length; i++)
			if ( sector(area, i) )
				return true;
		return false;		
	}
	
	private boolean normal(Area area, int dim)
	{
		return ( getLow()[dim] > area.getHigh()[dim] || 
				getHigh()[dim] < area.getLow()[dim] );
	}
	
	private boolean sector(Area area, int dim)
	{
		return ( getLow()[dim] > area.getLow()[dim] || 
				getHigh()[dim] < area.getLow()[dim] );
	}
	
	private boolean periodic(Area area, int dim)
	{
		/* if this is not passing a periodic boundary in this dimension */
		if ( getLow()[dim] < getHigh()[dim] ) 
		{
			this.periodic[dim] = false;
			/* if the partner area is also not passing a periodic boundary in
			 * this dimension  */
			if ( area.getLow()[dim] < area.getHigh()[dim] )
			{
				area.periodic[dim] = false;
				return normal(area, dim);
			}
			else
			{
				/* if the partner area is passing a periodic boundary in
				 * this dimension  */
				return ( getLow()[dim] > area.getHigh()[dim] && 
						getHigh()[dim] < area.getLow()[dim] );	
			}
		}
		/* if this is passing a periodic boundary in this dimension */
		else 
		{
			/* if the partner area is not passing a periodic boundary in
			 * this dimension  */
			if ( area.getLow()[dim] < area.getHigh()[dim] )
			{
				return ( area.getLow()[dim] > getHigh()[dim] && 
						area.getHigh()[dim] < getLow()[dim] );	
			}
			else
			{
			/* if the partner area is also passing a periodic boundary in
			 * this dimension  */
				return ( getLow()[dim] < area.getHigh()[dim] &&
						getHigh()[dim] > area.getLow()[dim] );
			}
		}
	}

	public double[] getLow() {
		return low;
	}

	public double[] getHigh() {
		return high;
	}
	
	public boolean[] getPeriodic() {
		return periodic;
	}

}
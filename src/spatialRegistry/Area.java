package spatialRegistry;

import java.util.function.Predicate;

/**
 * The {@link Area} object is used to quickly check for overlaps in the 
 * {@link: SplitTree}
 * 
 * @author Bastiaan
 *
 */
public class Area implements Predicate<Area> {
	
	protected double[] low;
	
	protected double[] high;
	
	protected boolean[] periodic;
	
	public Area()
	{

	}
	
	
	public Area(double[] low, double[] high)
	{
		this.low = low;
		this.high = high;
	}
	
	public void set(double[] low, double[] high)
	{
		this.low = low;
		this.high = high;
	}
	
	public Area(double[] low, double[] high, boolean[] periodic)
	{
		this.low = low;
		this.high = high;
		this.periodic = periodic;
	}
	
	public void set(double[] low, double[] high, boolean[] periodic)
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
			if ( periodic(area, i) )
				return true;
		return false;		
	}
	
	/**
	 * returns true if this has at least 1 length greater than area
	 * periodic
	 * @param area
	 * @return
	 */
	public boolean capsulating(Area area)
	{
		for (int i = 0; i < getLow().length; i++)
			if ( capsule(area, i) )
				return true;
		return false;	
	}
	
	private boolean capsule(Area area, int dim)
	{
		return ( getHigh()[dim]-getLow()[dim] > 
			area.getHigh()[dim]-area.getLow()[dim] );
	}
	
	protected boolean normal(Area area, int dim)
	{
		return ( getLow()[dim] > area.getHigh()[dim] || 
				getHigh()[dim] < area.getLow()[dim] );
	}
	
	
	public boolean periodic(Area area, int dim)
	{
		/* if this is not passing a periodic boundary in this dimension */
		if ( !this.periodic[dim] ) 
		{
			/* if the partner area is also not passing a periodic boundary in
			 * this dimension  */
			if ( !area.periodic[dim] )
			{
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
			if ( !area.periodic[dim] )
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
	
	public boolean[] periodic() {
		return periodic;
	}
	
	public void setperiodic(boolean[] periodic) {
		this.periodic = periodic;
	}

}
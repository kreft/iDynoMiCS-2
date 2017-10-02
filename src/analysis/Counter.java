package analysis;

import java.util.List;

import analysis.filter.*;
import aspect.AspectInterface;
import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;

/**
 * Attempt to obtain and summarize relevant data for a given filter.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Counter {
	
	/**
	 * Count the number of matches, the number of matches in a category or add
	 * the value's depending on what type of filter is passed.
	 * 
	 * @param filter
	 * @param subjects
	 * @return
	 */
	public static double[] count(Filter filter, List<AspectInterface> subjects)
	{		
		/* return the number of matches for each category */
		if (filter instanceof CategoryFilter)
		{
			double[] count = Vector.zerosDbl(((CategoryFilter) filter).size());
			for (AspectInterface a : subjects)
				count[ Integer.valueOf( filter.stringValue(a) ) ]++;
			return count;
		}
		/* return the sum of the aspects matching the value filter */
		else if (filter instanceof ValueFilter)
		{
			double count= 0.0;
			for (AspectInterface a : subjects)
			{
				try
				{
					count += Double.valueOf( filter.stringValue(a) );
				}
				catch (NumberFormatException e)
				{
					if (Log.shouldWrite(Tier.DEBUG))
						Log.out(Tier.DEBUG, "cannot sum " + filter.header() + 
								" because the value is not numeric in " + 
								Counter.class.getName());
				}
			}

			return new double[] { count };
		}
		/* return the number of matches for the filter */
		else
		{
			int count = 0;
			for (AspectInterface a : subjects)
				if ( filter.match( ( a ) ) )
					count ++;

			return new double[] { Double.valueOf(count) };
		}
	}

}

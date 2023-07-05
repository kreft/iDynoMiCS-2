package analysis;

import java.util.IllegalFormatConversionException;
import java.util.List;

import analysis.filter.CategoryFilter;
import analysis.filter.Filter;
import analysis.filter.ValueFilter;
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
				count[ Integer.valueOf( filter.stringValue(a,"%d") ) ]++;
			return count;
		}
		/* return the sum of the aspects matching the value filter */
		else if (filter instanceof ValueFilter)
		{
			double count= 0.0;
			int i = 0;
			for (AspectInterface a : subjects)
			{
				try
				{
					count += Double.valueOf( filter.stringValue(a,"%g") );
				}
				catch (NumberFormatException | IllegalFormatConversionException e)
				{
					i++;
				}

			}
			if (count == 0.0 & i != 0 )
			{
				count = (double) i;
			}
			else if (count != 0.0 & i != 0 )
			{
				if (Log.shouldWrite(Tier.NORMAL))
					Log.out(Tier.NORMAL, "numeric and non-numeric values for " + filter.header() + 
							" in " + 
							Counter.class.getName());
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

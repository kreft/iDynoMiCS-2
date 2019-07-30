/**
 * 
 */
package test.junit.oldTests;

import org.junit.Test;

import dataIO.Log;
import dataIO.Log.Tier;
import test.OldTests;

/**
 * \brief Test for checking speed performance of writing to Log.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class LogTest
{
	private static final int N_LOOP = 100000000;
	
	private static final Tier LOG_LEVEL = Tier.DEBUG;
	
	@Test
	public void checkWithLogShouldWork()
	{
		OldTests.setupSimulatorForTest(1.0, 1.0, "checkWithLogShouldWork");
		for ( int i = 0; i < N_LOOP; i++ )
			if ( Log.shouldWrite(LOG_LEVEL) )
				Log.out(LOG_LEVEL, "Iteration "+i);
	}
	
	@Test
	public void checkWithoutLogShouldWork()
	{
		OldTests.setupSimulatorForTest(1.0, 1.0, "checkWithoutLogShouldWork");
		for ( int i = 0; i < N_LOOP; i++ )
			Log.out(LOG_LEVEL, "Iteration "+i);
	}
}
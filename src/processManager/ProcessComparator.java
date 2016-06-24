package processManager;

import java.util.Comparator;

import dataIO.Log;
import dataIO.Log.Tier;
import utility.ExtraMath;

/**
 * \brief Helper for ordering {@code ProcessManager}s: time for next step
 * is the key metric for ordering, but we use the priority value in case
 * of a draw.
 * 
 * <p><b>IMPORTANT</b>: note that a {@code ProcessManager} with a higher
 * numerical priority will happen before one with lower priority if the two are
 * scheduled to happen at the same time.</p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ProcessComparator implements Comparator<ProcessManager>
{
	private final static Tier LEVEL = Tier.BULK;
	
	@Override
	public int compare(ProcessManager pm1, ProcessManager pm2) 
	{
		int out;
		Double temp = pm1.getTimeForNextStep() - pm2.getTimeForNextStep();
		if ( ExtraMath.areEqual(temp, 0.0, 1.0E-10) )
			out = pm2.getPriority() - pm1.getPriority();
		else
			out = (int) Math.signum(temp);
		if ( Log.shouldWrite(LEVEL) )
		{
			Log.out(LEVEL, "ProcessComparator: "+pm1._name+" vs "+pm2._name+
					" has tDiff "+temp+", so out = "+out);
		}
		return out;
	}
}
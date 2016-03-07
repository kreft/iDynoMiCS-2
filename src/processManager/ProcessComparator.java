package processManager;

import java.util.Comparator;

import utility.ExtraMath;

/**
 * \brief Helper for ordering {@code ProcessManager}s: time for next step
 * is the key metric for ordering, but we use the priority value in case
 * of a draw.
 * 
 * <p><b>IMPORTANT</b>: note that a {@code ProcessManager} with a higher
 * numerical priority will happen before one with lower priority if the two are
 * scheduled to happen at the same time.</p>
 */
public class ProcessComparator implements Comparator<ProcessManager>
{
	@Override
	public int compare(ProcessManager pm1, ProcessManager pm2) 
	{
		Double temp = pm1.getTimeForNextStep() - pm2.getTimeForNextStep();
		if ( ExtraMath.areEqual(temp, 0.0, 1.0E-10) )
			return pm2.getPriority() - pm1.getPriority();
		else
			return temp.intValue();
	}
}
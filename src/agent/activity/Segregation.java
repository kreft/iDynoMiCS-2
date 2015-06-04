package agent.activity;

import agent.Agent;
import utility.ExtraMath;

/**
 * segregation should be called on cell division, does the daughter cell receive
 * the episome?
 * FIXME: implemented as in indyno 1.2 Episome segregation method, when
 * implemented I think it would be better to have a episome distribution
 * activity that decides the episome copynumber of mother and daughter cell
 * rather than just setting daughter cell to 0 or 1.
 * @author baco
 *
 */
public class Segregation extends Activity {
	
	protected Double _lossProbability = null;
	
	public void doit(Agent childEpisome) {
		if (ExtraMath.getUniRandDbl() > _lossProbability)
			childEpisome.setCopyNumber(1);
		else
			childEpisome.setCopyNumber(0);
	}
}

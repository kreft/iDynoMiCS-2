/**
 * 
 */
package aspect.event;

import java.util.HashMap;
import java.util.Iterator;

import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.Log;
import dataIO.Log.Tier;
import processManager.library.PlasmidDynamics;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

/**
 * @author sankalp
 *
 */
public class PlasmidLoss extends Event {
	
	public String PIGMENT = AspectRef.agentPigment;
	
	public String FITNESS_COST = AspectRef.agentFitnessCost;
	
	public String COOL_DOWN_PERIOD = AspectRef.coolDownTime;

	@SuppressWarnings("unchecked")
	@Override
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep) {
		// TODO Auto-generated method stub
		Agent justBorn = (Agent) initiator;
		
		Iterator<Object> plasmidItrtr = PlasmidDynamics._plasmidList.iterator();
		while(plasmidItrtr.hasNext()) {
			String plasmidAspect = plasmidItrtr.next().toString();
			if (justBorn.isLocalAspect(plasmidAspect)) {
				Log.out(Tier.DEBUG, "Plasmid found!");
				HashMap<Object, Object> plasmidParams = (HashMap<Object, Object>) justBorn.get(plasmidAspect);
				double loss_probability = (Double) plasmidParams.get(PlasmidDynamics.LOSS_PROB);
				double rndDbl = ExtraMath.getUniRandDbl();
				String lossPigment = (String) plasmidParams.get(PlasmidDynamics.LOSS_PIGMENT);
				if (rndDbl < loss_probability) {
					justBorn.reg().remove(plasmidAspect);
					justBorn.reg().set(this.FITNESS_COST, 0.0);
					justBorn.reg().remove(this.COOL_DOWN_PERIOD);
					justBorn.set(this.PIGMENT, lossPigment);
				}
			}
		}
	}

}

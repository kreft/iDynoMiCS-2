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
import idynomics.Idynomics;
import processManager.library.PlasmidDynamics;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

/**
 * @author sankalp
 *
 */
public class PlasmidLoss extends Event {
	
	public String FITNESS_COST = AspectRef.agentFitnessCost;

	@SuppressWarnings("unchecked")
	@Override
	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep) {
		Agent justBorn = (Agent) initiator;
		Iterator<Object> plasmidItrtr = PlasmidDynamics.getAllPlasmids().iterator();
		while(plasmidItrtr.hasNext()) {
			String plasmidAspect = plasmidItrtr.next().toString();
			if (justBorn.isLocalAspect(plasmidAspect)) {
				HashMap<Object, Object> plasmidParams = (HashMap<Object, Object>) justBorn.get(plasmidAspect);
				double loss_probability = (Double) plasmidParams.get(AspectRef.lossProbability);
				double rndDbl = ExtraMath.getUniRandDbl();
				String[] aspects_change = (String[]) plasmidParams.get(AspectRef.aspectsToTransfer);
				if (rndDbl < loss_probability) {
					Log.out(Tier.DEBUG, "Agent "+justBorn.identity()+" lost plasmid "+plasmidAspect);
					justBorn.reg().remove(plasmidAspect);
					justBorn.set(this.FITNESS_COST, 0.0);
					for (int i = 0; i < aspects_change.length; i++) {
						String aspectName = aspects_change[i];
						String keyName = aspectName + "_on_loss";
						justBorn.set(aspectName, plasmidParams.get(keyName));
					}
				}
				else {
					PlasmidDynamics.addToPreviousConjugated(justBorn, Idynomics.simulator.timer.getCurrentTime());
				}
			}
		}
	}

}

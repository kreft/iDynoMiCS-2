package processManager.library;

import agent.Agent;
import agent.Body;
import idynomics.Idynomics;
import instantiable.object.InstantiableMap;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import surface.Rod;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static aspect.methods.TransferMethod.massAdjust;
import static processManager.ProcessMethods.getAgentMassMap;

public class CytoplasmicStreaming extends ProcessManager {

    @Override
    protected void internalStep() {

        double timeRemaining = this.getTimeStepSize();
        boolean shouldStep = true;
        double maxTransferRate = 0.0;

        while( shouldStep )
        {
            /* pass one: calculate transfer rates  */
            for ( Agent a : this._agents.getAllAgents() ) {
                /* calculate transferRates for all agents, skip if there are no partners or transfer types  */
                if ( a.isAspect(AspectRef.partners) && a.isAspect(AspectRef.tranferMassType) ) {

                    List<Agent> recipients = getRecipients(a);
                    if (recipients.isEmpty())
                        continue;

                    Map<String, Double> vocalMassMap = getAgentMassMap(a);

                    //TODO we can switch to transferMaps to track and transfer multiple components
                    String transferType = (a.isAspect(AspectRef.tranferMassType) ?
                            a.getString(AspectRef.tranferMassType) : null);

                    //// will currently never be null
                    if (transferType == null)
                        transferType = (vocalMassMap.containsKey("mass") ? "mass" : "biomass");

                    Body initiatorBody = (Body) a.getValue(AspectRef.agentBody);

                    double dX; // cell compartment length
                    double psi = 1.0; // 833 ~ 0.05 dm/h
                    if (initiatorBody.getMorphology().equals(Body.Morphology.COCCOID)) {
                        dX = 2.0 * a.getDouble(AspectRef.bodyRadius);
                    } else if (initiatorBody.getMorphology().equals(Body.Morphology.BACILLUS)) {
                        Rod rod = (Rod) initiatorBody.getSurfaces().get(0);
                        dX = 2.0 * rod.getRadius() + rod.getLength();
                    } else
                        dX = 1.0;

                    double transferRate = (psi / dX);
                    maxTransferRate = Math.max(maxTransferRate,transferRate);

                    // NOTE we are multiplying both sides of the equation by compartment volume to have a mass rate.
                    a.set(AspectRef.massTransferRate, transferRate * vocalMassMap.get(transferType));
                    // we could also set a limit eg: initiator.getDouble( AspectRef.massTransferLimit )
                }
            }

            // NOTE we are currently setting the internal timestep based on the max mass transfer rate in the system.
            // We are ensuring the amount of mass transferred per step is always fraction.
            double dt = Math.min( 0.05 / maxTransferRate, timeRemaining);
            timeRemaining -= dt;

            /* pass two: transfer and cleanup  */
            for (Agent a : this._agents.getAllAgents())
            {
                if( a.isAspect(AspectRef.partners) && a.isAspect( AspectRef.tranferMassType ) )
                {
                    String transferType = (a.isAspect(AspectRef.tranferMassType) ?
                            a.getString(AspectRef.tranferMassType) : null);
                    double massTransferRate = (a.isAspect(AspectRef.massTransferRate) ?
                            a.getDouble(AspectRef.massTransferRate) : 0.0);

                    List<Agent> recipients = getRecipients(a);
                    if (recipients.isEmpty())
                        continue;

                    for (Agent recipient : recipients) {
                        massAdjust( recipient, dt * (massTransferRate / recipients.size()), transferType);
                    }
                    massAdjust( a, dt * -massTransferRate , transferType);
                }

                // cleanup temporary aspect
                a.delete(AspectRef.massTransferRate);
            }
            shouldStep = (timeRemaining > 0.0);
        }
    }

    public List<Agent> getRecipients(Agent agent)
    {
        @SuppressWarnings("unchecked")
        InstantiableMap<Integer, String> partnerMap =
                (InstantiableMap<Integer, String>) agent.getValue(AspectRef.partners);
        LinkedList<Agent> recipients = new LinkedList<Agent>();

        String recipientSpec = null;
        if (agent.isAspect(AspectRef.recipientType))
            recipientSpec = agent.getString(AspectRef.recipientType);

        if (recipientSpec == null)
            return recipients;

        for (int p : partnerMap.keySet()) {
            /* Could get slow with large number of agents.
             *  We could store direct references to agents
             *  but store and fetch iD for xml io */
            if (partnerMap.get(p).equals("child")) //TODO get "child" from reference to make it renamable
            {
                Agent m = Idynomics.simulator.findAgent(p);
                recipients.add(m);
            }
        }
        return recipients;

    }
}

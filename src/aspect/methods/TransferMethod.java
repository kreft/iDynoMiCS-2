package aspect.methods;

import agent.Agent;
import dataIO.Log;
import dataIO.ObjectFactory;
import processManager.ProcessMethods;
import referenceLibrary.AspectRef;

import java.util.HashMap;
import java.util.Map;

public class TransferMethod {

    // Could possibly be grouped with other helper methods
    public static void massAdjust(Agent agent, double quantity, String massType )
    {
        Map<String, Double> biomass = ProcessMethods.getAgentMassMap(agent);
        @SuppressWarnings("unchecked")
        Map<String, Double> newBiomass = (HashMap<String, Double>)
                ObjectFactory.copy(biomass);
        if ( massType != null )
        {
            newBiomass.put(massType, biomass.get( massType ) + quantity);
        }
        else if ( newBiomass.containsKey(AspectRef.agentMass) )
        {
            newBiomass.put(AspectRef.agentMass, newBiomass.get( AspectRef.agentMass ) + quantity);
        }
        else if( newBiomass.containsKey("biomass") )
            newBiomass.put("biomass", newBiomass.get("biomass") + quantity);
        else {
            Log.out(Log.Tier.CRITICAL,"Unkown mass transfer type in " + TransferMethod.class.getSimpleName());
        }
        ProcessMethods.updateAgentMass(agent, newBiomass);
    }

}

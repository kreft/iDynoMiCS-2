package aspect.event;

import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import compartment.Compartment;
import dataIO.Log;
import dataIO.ObjectFactory;
import idynomics.Idynomics;
import instantiable.object.InstantiableMap;
import processManager.ProcessMethods;
import referenceLibrary.AspectRef;
import utility.Helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BuddingMassTransfer extends Event {

    @Override
    public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
    {
        if( initiator.isAspect(AspectRef.partners) )
        {
            @SuppressWarnings("unchecked")
            InstantiableMap<Integer,String> initiatorMap =
                    (InstantiableMap<Integer, String>) initiator.getValue(AspectRef.partners);

            String recipientSpec = null;
            LinkedList<Agent> receipients = new LinkedList<Agent>();
            if( initiator.isAspect(AspectRef.recipientType) )
                recipientSpec = initiator.getString( AspectRef.recipientType );
            if ( recipientSpec == null )
                return;

//            Agent vocalAgent = (Agent) initiator;
//            Compartment compartment = vocalAgent.getCompartment();

            for ( int p : initiatorMap.keySet() )
            {
                /* could get slow with large number of agents */
                Agent m = Idynomics.simulator.findAgent( p );
                if( m.isAspect(AspectRef.agentType) && m.getString(AspectRef.agentType).equals( recipientSpec ) )
                    receipients.add( m );
            }

            if( receipients.isEmpty() )
                return;

            Object massObject = initiator.getValue( AspectRef.agentMass );
            double vocalMass = Helper.totalMass( massObject );
            double transferMass = (vocalMass - initiator.getDouble( AspectRef.massTransferLimit ) );
            double quantity = transferMass/receipients.size();

            for( Agent agent : receipients ) {
                massAdjust( agent, quantity, (initiator.isAspect( AspectRef.tranferMassType ) ?
                        initiator.getString( AspectRef.tranferMassType ) : null ));
            }
            massAdjust( (Agent) initiator, -(transferMass), (initiator.isAspect( AspectRef.tranferMassType ) ?
                    initiator.getString( AspectRef.tranferMassType ) : null ));
        }
    }

    private void massAdjust(Agent agent, double quantity, String massType )
    {
        Map<String, Double> biomass = ProcessMethods.getAgentMassMap(agent);
        @SuppressWarnings("unchecked")
        Map<String, Double> newBiomass = (HashMap<String, Double>)
                ObjectFactory.copy(biomass);
        if ( massType != null )
        {
            newBiomass.put(massType, agent.getDouble( massType ) + quantity);
        }
        else if ( newBiomass.containsKey(AspectRef.agentMass) )
        {
            newBiomass.put(AspectRef.agentMass, newBiomass.get( AspectRef.agentMass ) + quantity);
        }
        else if( newBiomass.containsKey("biomass") )
            newBiomass.put("biomass", newBiomass.get("biomass") + quantity);
        else {
            Log.out(Log.Tier.CRITICAL,"Unkown mass transfer type in " + this.getClass().getSimpleName());
        }
        ProcessMethods.updateAgentMass(agent, newBiomass);
    }
}

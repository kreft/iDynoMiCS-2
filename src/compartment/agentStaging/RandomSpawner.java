package compartment.agentStaging;

import agent.AgentHelperMethods;
import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import processManager.ProcessMethods;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

import java.util.Map;


/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class RandomSpawner extends Spawner {
	
	
	public void init(Element xmlElem, AgentContainer agents, 
			String compartmentName)
	{
		super.init(xmlElem, agents, compartmentName);
	}

	@Override
	public void spawn() 
	{
		for(int j = 0; j < this.getNumberOfAgents(); j++)
		{
			/* use copy constructor */
			Agent newRandom = new Agent(this.getTemplate());
			newRandom.set(AspectRef.agentBody, 
					new Body( this.getMorphology(), this.getSpawnDomain() ));
			newRandom.setCompartment( this.getCompartment() );
			AgentHelperMethods.springInitialization(newRandom);

			// FIXME test feature to randomize agent mass at start
			if( newRandom.isAspect( "randomize" ))
			{
				String ran = newRandom.getString( "randomize" );
				Double factor =  newRandom.getDouble( "factor" );
				Map<String,Double> biomass = ProcessMethods.getAgentMassMap( newRandom );

				Double out = ExtraMath.getUniRand( 1.0-factor, 1.0+factor);
				if ( biomass.containsKey(ran) )
				{
					out = biomass.get(ran) * out;
					biomass.put(ran, out);
				}
				else if ( newRandom.isAspect(ran) )
				{
					/*
					 * Check if the agent has other mass-like aspects
					 * (e.g. EPS).
					 */
					out = newRandom.getDouble(ran) * out;
					biomass.put(ran, out);
				}
				ProcessMethods.updateAgentMass(newRandom,biomass);
			}
			newRandom.registerBirth();
		}
	}
}

package compartment.agentStaging;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import dataIO.XmlHandler;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import surface.BoundingBox;

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
		for(int j = 0; j < this.calculateNumberOfAgents(); j++)
		{
			/* use copy constructor */
			Agent newRandom = new Agent(this.getTemplate());
			newRandom.set(AspectRef.agentBody, 
					new Body( this.getMorphology(), this.getSpawnDomain() ));
			newRandom.setCompartment( this.getCompartment() );
			newRandom.registerBirth();
		}
	}


	
}

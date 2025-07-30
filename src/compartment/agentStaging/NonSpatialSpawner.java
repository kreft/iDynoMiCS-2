package compartment.agentStaging;

import agent.Agent;
import agent.AgentHelperMethods;
import agent.Body;
import compartment.AgentContainer;
import org.w3c.dom.Element;
import processManager.ProcessMethods;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

import java.util.Map;
/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class NonSpatialSpawner extends Spawner {
	
	
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
			newRandom.setCompartment( this.getCompartment() );

			randomizeMass(newRandom);
			newRandom.registerBirth();
		}
	}
}

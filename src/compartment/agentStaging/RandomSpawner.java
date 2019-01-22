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
	
	/**
	 * BoundingBox for spawn domain
	 * TODO maybe this can be more generally applied and we should move this to
	 * the Spawner super class.
	 */
	private BoundingBox spawnDomain = new BoundingBox();
	
	
	public void init(Element xmlElem, AgentContainer agents, 
			String compartmentName)
	{
		super.init(xmlElem, agents, compartmentName);
		
		Element p = (Element) xmlElem;
		if ( XmlHandler.hasAttribute(p, XmlRef.spawnDomain) )
		{
			double[][] input = 
					Matrix.dblFromString(p.getAttribute(XmlRef.spawnDomain));
			if( Matrix.rowDim(input) < 2)
				spawnDomain.get(input[0], Vector.zeros(input[0]));
			else
				spawnDomain.get(input[0], input[1]);
		}
			
	}

	@Override
	public void spawn() 
	{
		for(int j = 0; j < this.getNumberOfAgents(); j++)
		{
			/* use copy constructor */
			Agent newRandom = new Agent(this.getTemplate());
			newRandom.set(AspectRef.agentBody, 
					new Body( this.getMorphology(), spawnDomain ));
			newRandom.setCompartment( this.geCompartment() );
			newRandom.registerBirth();
		}
	}


	
}

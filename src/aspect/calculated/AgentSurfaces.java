package aspect.calculated;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentSurfaces extends Calculated
{
	public String BODY = AspectRef.agentBody;
	/**
	 * Input body.
	 */
	public Object get(AspectInterface aspectOwner)
	{
		return ((Body) aspectOwner.getValue(BODY)).getSurfaces();
	}

}
package aspect.calculated;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import idynomics.NameRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentSurfaces extends Calculated
{
	public String BODY = NameRef.agentBody;
	/**
	 * Input body.
	 */
	public Object get(AspectInterface aspectOwner)
	{
		return ((Body) aspectOwner.getValue(BODY)).getSurfaces();
	}

}
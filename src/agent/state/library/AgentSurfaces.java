package agent.state.library;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class AgentSurfaces extends Calculated
{
	/**
	 * Input body.
	 */
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return ((Body) agent.get(input[0])).getSurfaces();
	}

}
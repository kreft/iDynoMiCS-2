package agent.state.library;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import generalInterfaces.Quizable;
import idynomics.NameRef;

public class AgentSurfaces extends Calculated {
	
	public String BODY = NameRef.agentBody;
	
	public Object get(AspectInterface aspectOwner)
	{
		Quizable agent = (Quizable) aspectOwner;
		return ((Body) agent.get(BODY)).getSurfaces();
	}

}
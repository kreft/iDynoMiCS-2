package agent.state.library;

import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import idynomics.NameRef;

/**
 * input body
 * @author baco
 *
 */
public class CoccoidCenter extends Calculated {
	
	public CoccoidCenter()
	{
		setInput(NameRef.agentBody);
	}

	public Object get(AspectInterface aspectOwner)
	{
		return ((Body) aspectOwner.getValue(input[0])).getJoints().get(0);
	}

}

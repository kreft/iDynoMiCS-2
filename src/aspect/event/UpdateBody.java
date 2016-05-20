package aspect.event;

import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import idynomics.NameRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class UpdateBody extends Event
{
	
	public String BODY = NameRef.agentBody;
	public String RADIUS = NameRef.bodyRadius;
	
	public UpdateBody()
	{
		setInput("body,radius,length");
	}

	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		Body body = (Body) initiator.getValue(BODY);
		body.update( initiator.getDouble(RADIUS), 0.0);
	}
}
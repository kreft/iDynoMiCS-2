package aspect.event;

import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import idynomics.NameRef;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class UpdateBody extends Event
{
	
	public String BODY = NameRef.agentBody;
	public String RADIUS = NameRef.bodyRadius;
	public String VOLUME = NameRef.agentVolume;
	
	public UpdateBody()
	{
		setInput("body,radius,length");
	}

	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		double l = 0.0;
		Body body = (Body) initiator.getValue(BODY);
		
		// TODO cleanup
		if ( body.getJoints().size() > 1 )
		{
			double r = initiator.getDouble(RADIUS);
			double v = initiator.getDouble(VOLUME) - ExtraMath.volumeOfASphere( r );
			l = ExtraMath.lengthOfACylinder( v, r );
		}
		body.update( initiator.getDouble(RADIUS), l);
	}
}
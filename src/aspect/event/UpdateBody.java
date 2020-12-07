package aspect.event;

import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class UpdateBody extends Event
{
	
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String VOLUME = AspectRef.agentVolume;

	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		double l = 0.0;
		Body body = (Body) initiator.getValue(BODY);
		
		// TODO cleanup
		if ( body.getNumberOfPoints() > 1 )
		{
			double r = initiator.getDouble(RADIUS);
			double v = initiator.getDouble(VOLUME) - ExtraMath.volumeOfASphere( r );
			l = ExtraMath.lengthOfACylinder( v, r );
		}
		body.update( initiator.getDouble(RADIUS), l, initiator);
	}
}
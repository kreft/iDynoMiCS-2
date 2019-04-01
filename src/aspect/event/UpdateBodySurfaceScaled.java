package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import referenceLibrary.AspectRef;
import shape.Dimension.DimName;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * Update Agent body with radius/length scaled to representative 3D equivilant
 * of 2D shape: 
 * - A cylinder for a circular agent were the z-Dimension equals the length
 * - A Cylinder + cuboid for a 2D bacillus were the height of the cylinder and
 * cuboid equal the z-dimension, the radius is kept constant and the length of 
 * the cuboid is scaled so that the volume remains equal to that of a 3D capsule
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, density.
 */
public class UpdateBodySurfaceScaled extends Event {
	
	public String MASS = AspectRef.agentMass;
	public String REPRESENTED_DENSITY = AspectRef.agentRepresentedDensity;
	public String RADIUS = AspectRef.bodyRadius;
	public String VOLUME = AspectRef.agentVolume;
	public String BODY = AspectRef.agentBody;
	
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		Agent anAgent = (Agent) initiator;
		Body body = (Body) initiator.getValue(this.BODY);
		double volume = initiator.getDouble(this.VOLUME);
		Compartment comp = anAgent.getCompartment();
		double zLength = comp.getShape().getDimension(DimName.Z).getLength();
		
		switch(body.getMorphology()) {
		  case COCCOID:
			  body.update( ExtraMath.radiusOfACylinder(volume, zLength), 0.0 );
			  break;
		  case BACILLUS:
			  double r = initiator.getDouble(RADIUS);
			  body.update( r, (volume - ExtraMath.volumeOfACylinder(r, zLength)) 
					  /	(zLength * r ) );
			  break;
		  default:
			  Log.out(Tier.CRITICAL, this.getClass().getSimpleName()
			  		+ " attempting to scale " + body.getMorphology() + " agent"
			  		+ " with undifined scaling procedure, returning input value");
		}
	}
}

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
 * \brief Scale body in 2D compart, use normal scaling in others
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
	
	private boolean warned = false;
	
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
		
		if ( comp.getNumDims() != 2)
		{
			if(!warned && Log.shouldWrite(Tier.CRITICAL))
				Log.out( Tier.CRITICAL, "Warning! standard scaling method for "
						+ "non 2D compartment: " + comp.getNumDims() + 
						" dimensional domain in " +	
						this.getClass().getSimpleName() );
			
			/* standard agent body */
			double l = 0.0;
			if ( body.getNumberOfPoints() > 1 )
			{
				double r = initiator.getDouble(RADIUS);
				double v = initiator.getDouble(VOLUME) - 
						ExtraMath.volumeOfASphere( r );
				l = ExtraMath.lengthOfACylinder( v, r );
			}
			body.update( initiator.getDouble(RADIUS), l, initiator);
			this.warned = true;
		}
		else
		{
			double zLen = comp.getShape().getDimension(DimName.Z).getLength();
			switch(body.getMorphology()) {
			  case COCCOID:
				  body.update( ExtraMath.radiusOfACylinder( volume, zLen ), 
						  0.0, initiator);
				  break;
			  case BACILLUS:
				  double r = initiator.getDouble(RADIUS);
				  body.update(r, (volume - ExtraMath.volumeOfACylinder(r, zLen)) 
						  /	(zLen * r ) , initiator);
				  break;
			  default:
				  Log.out(Tier.CRITICAL, this.getClass().getSimpleName()
				  		+ " attempting to scale " + body.getMorphology() + 
				  		" agent with undifined scaling procedure, returning "
				  		+ "input value");
			}
		}
	}
}

package aspect.event;

import java.util.HashMap;
import java.util.Map;

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
	public String EPS = AspectRef.productEPS;
	
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		Agent anAgent = (Agent) initiator;
		Body body = (Body) initiator.getValue(this.BODY);
		Object volume = initiator.getValue(this.VOLUME);
		Compartment comp = anAgent.getCompartment();
		
		if ( comp.getNumDims() != 2)
		{
			if(!warned && Log.shouldWrite(Tier.CRITICAL))
				Log.out( Tier.CRITICAL, "Warning! standard scaling method for "
						+ "non 2D compartment: " + comp.getNumDims() + 
						" dimensional domain in " +	
						this.getClass().getSimpleName() );
			
			/* standard agent body */
			
			UpdateBody updateBody = new UpdateBody();
			
			updateBody.start(initiator, compliant, timeStep);
			
			this.warned = true;
		}
		else
		{
			double zLen = comp.getShape().getDimension(DimName.Z).getLength();
			switch(body.getMorphology()) {
			  case COCCOID:
				  
				  if (!(volume instanceof Map))
				  {
					  double volumeDouble = (double) volume;
					  /*
					   * Do we need to use zLen and ExtraMath here?
					   * Isn't this already handled by the CylinderRadius 
					   * calculated aspect?
					   */
					  body.update( ExtraMath.radiusOfACylinder(
							  volumeDouble, zLen ), 0.0, initiator);
				  }
				  
				  else
				  {
					  body.update( initiator.getDouble(RADIUS), 0.0,
								initiator);
				  }
				  
				  break;
				  
			  case BACILLUS:
				  double r = initiator.getDouble(RADIUS);
				  
				  if (!(volume instanceof Map))
				  {
					  double volumeDouble = (double) volume;
					  
					  /*
					   * Same question as above here
					   */
					  
					  double l = (volumeDouble - ExtraMath.
							  volumeOfACylinder(r, zLen)) /(zLen * 2 * r);
					  
					  if (l > 0.0)
					  {
						  body.update(r, l , initiator);
					  }
					  
					  /*
					   * If the rod would have a length less than 0, just make
					   * it effectively a coccoid, without changing its
					   * morphology aspect.
					   */
					  else
					  {
						  body.update( ExtraMath.radiusOfACylinder(
								  volumeDouble, zLen ), 0.0, initiator);
					  }
					  
				  }
				  
				  else
				  {
					  Map<String, Double> volumeMap = 
							  (HashMap<String, Double>) volume;
					  
					  double cellVolume = 0.0;
					  double epsVolume = 0.0;
					  for (String component : volumeMap.keySet())
					  {
						  if (component.equals(this.EPS))
							  epsVolume += volumeMap.get(component);
						  else
							  cellVolume += volumeMap.get(component);
					  }
					  
					  if (epsVolume == 0.0)
					  {
						  double rectangleVolume = cellVolume - 
								  ExtraMath.volumeOfACylinder( r, 1.0 );
						  
						  double l = rectangleVolume / (2 * r);
						  
						  if (l > 0.0)
						  {
							  body.update( initiator.getDouble(RADIUS),
									  l, initiator);
						  }
						  
						  else
						  {
							  body.update( ExtraMath.radiusOfACylinder(
									  cellVolume, zLen ), 0.0, initiator);
						  }
					  }
					  
					  else
					  {
						  double totalVolume = cellVolume + epsVolume;
						  
						  double rectangleVolume = cellVolume - 
								  ExtraMath.volumeOfACylinder( r, 1.0 );
						  
						  double l = rectangleVolume / (2 * r);
						  
						  if (l > 0.0)
						  {
							  double radiusWithEPS = 
									 ExtraMath.radiusOfAStadium(totalVolume, l);
							  
							  body.update( radiusWithEPS, l, initiator);
						  }
						  
						  else
						  {
							  body.update( ExtraMath.radiusOfACylinder(
									  totalVolume, zLen ), 0.0, initiator);
						  }
					  }
					  
				  }
				  
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

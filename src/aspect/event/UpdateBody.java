package aspect.event;

import java.util.HashMap;
import java.util.Map;

import agent.Body;
import agent.Body.Morphology;
import aspect.AspectInterface;
import aspect.Event;
import referenceLibrary.AspectRef;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class
UpdateBody extends Event
{
	
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String VOLUME = AspectRef.agentVolume;
	public String EPS = AspectRef.productEPS;

	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		double l = 0.0;
		Body body = (Body) initiator.getValue(BODY);
		
		// TODO cleanup
		if (body.getMorphology() == Morphology.COCCOID)
		{
			body.update( initiator.getDouble(RADIUS), 0.0,
					initiator);
		}
		
		else
		{
			Object volume = initiator.getValue(VOLUME);
			
			double r = initiator.getDouble(RADIUS);
			
			if (!(volume instanceof Map))
			{
				double volumeDouble = (double) volume;
				
				/*
				 * Volume of the cylinder part of the capsule shape
				 */
				double cylinderVolume = volumeDouble - 
						ExtraMath.volumeOfASphere( r );
				
				l = ExtraMath.lengthOfACylinder( cylinderVolume, r );
				
				if (l > 0.0)
				{
					body.update( initiator.getDouble(RADIUS), l, initiator);
				}
				
				/*
				 * If the agent is too small to take on rod shape, just make
				 * it a coccoid
				 */
				else
				{
					body.update( ExtraMath.radiusOfASphere(volumeDouble), 0.0,
							initiator);
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
				
				/*
				 * If no EPS, do the same simple method as above
				 */
				if (epsVolume == 0.0)
				{
					double cylinderVolume = cellVolume - 
							ExtraMath.volumeOfASphere( r );
					
					l = ExtraMath.lengthOfACylinder( cylinderVolume, r );
					
					if (l > 0.0)
					{
						body.update( initiator.getDouble(RADIUS), l, initiator);
					}
					
					else
					{
						body.update( ExtraMath.radiusOfASphere(cellVolume), 0.0,
								initiator);
					}
					
				}
				
				
				else
				{
					double totalVolume = cellVolume + epsVolume;
					
					/*
					 * Volume of the cylinder part of the capsule shape,
					 * calculated using only the cell volume
					 */
					double cylinderVolume = cellVolume - 
							ExtraMath.volumeOfASphere( r );
					
					l = ExtraMath.lengthOfACylinder( cylinderVolume, r );
					
					if (l > 0.0)
					{
						/*
						 * Recalculate radius using the total volume including
						 * EPS
						 */
						double radiusWithEPS = ExtraMath.radiusOfACapsule(
								totalVolume, l);
						
						body.update( radiusWithEPS, l, initiator);
					}
					
					else
					{
						body.update( ExtraMath.radiusOfASphere(totalVolume),
								0.0, initiator);
					}
				}
			}
		}
		
	}
}
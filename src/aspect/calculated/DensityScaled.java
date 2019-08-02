package aspect.calculated;

import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import referenceLibrary.AspectRef;
import shape.Dimension.DimName;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief TODO
 * 
 * Calculating scaled agent density, this allows to maintain the same surface
 * area occupation per amount of biomass.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * Input: mass, density.
 */
public class DensityScaled extends Calculated {
	
	public String MASS = AspectRef.agentMass;
	public String REPRESENTED_DENSITY = AspectRef.agentRepresentedDensity;
	public String RADIUS = AspectRef.bodyRadius;
	public String BODY = AspectRef.agentBody;
	
	public DensityScaled()
	{
		setInput("mass, density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Agent anAgent = (Agent) aspectOwner;
		Body body = (Body) aspectOwner.getValue(this.BODY);
		Double original = aspectOwner.getDouble(REPRESENTED_DENSITY);
		if (body.nDim() > 2)
			return original;
		
		
		Compartment comp = anAgent.getCompartment();
		
		Object massObject = aspectOwner.getValue(this.MASS);
		double totalMass = 0.0;
		if ( massObject instanceof Double )
			totalMass = (double) massObject;
		else if ( massObject instanceof Double[] )
		{
			Double[] massArray = (Double[]) massObject;
			for ( Double m : massArray )
				totalMass += m;
		}
		else if ( massObject instanceof Map )
		{
			// TODO assume all mass types used unless specified otherwise
			@SuppressWarnings("unchecked")
			Map<String,Double> massMap = (Map<String,Double>) massObject;
			totalMass = Helper.totalValue(massMap);
		}
		else
		{
			// TODO safety?
		}
		Double virtual = totalMass / original;
		Double zLength = comp.getShape().getDimension(DimName.Z).getLength();

		switch(body.getMorphology()) {
		  case COCCOID:
			  return original * ( virtual / (Math.PI * 2.0 * 
					  ExtraMath.radiusOfASphere( virtual ) * zLength ) );
		  case BACILLUS:
			  Double radius = aspectOwner.getDouble(RADIUS);
			  return original * ( virtual / ( ( Math.PI * 2.0 * 
					  ExtraMath.radiusOfASphere( virtual ) * zLength ) + 
					  ( ExtraMath.lengthOfACylinder( virtual , radius ) * radius
					  * zLength ) ) );
		  default:
			  Log.out(Tier.CRITICAL, this.getClass().getSimpleName()
			  		+ " attempting to scale " + body.getMorphology() + " agent"
			  		+ " with undifined scaling procedure, returning input value");
			  return original;
		}
	}

}

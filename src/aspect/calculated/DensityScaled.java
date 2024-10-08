package aspect.calculated;

import java.util.HashMap;
import java.util.Map;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import compartment.Compartment;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import shape.Dimension.DimName;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief TODO
 * 
 * Calculating scaled agent density, this allows to maintain the same surface
 * area occupation per amount of biomass in both 2D and 3D settings.
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
	public String EPS = AspectRef.productEPS;
	
	public DensityScaled()
	{
		setInput("mass, density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Agent anAgent = (Agent) aspectOwner;
		Body body = (Body) aspectOwner.getValue(this.BODY);
		Object representedDensity = aspectOwner.getValue(REPRESENTED_DENSITY);
		
		if (body.nDim() > 2)
			return representedDensity;
		
		
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
			Idynomics.simulator.interupt("Interrupted in " + 
					this.getClass().getSimpleName() + ". Mass object " + 
					massObject + "is not a Double, Double[] or hashmap.");
		}
		
		/*
		 * If represented density is one value, simple method as below
		 */
		if (!(representedDensity instanceof Map))
		{
			double representedDensityDouble = (double) representedDensity;
			
			Double virtual = totalMass / representedDensityDouble;
			Double zLength = comp.getShape().
					getDimension(DimName.Z).getLength();

			switch(body.getMorphology()) {
			  case COCCOID:
				  double volumeIn2D = Math.PI * 
				  Math.pow( ExtraMath.radiusOfASphere( virtual ), 2) * zLength;
				  return representedDensityDouble * ( virtual / volumeIn2D );
			  case BACILLUS:
				  Double radius = aspectOwner.getDouble(RADIUS);
				  double lengthOfVirtualRod = ExtraMath.lengthOfACylinder(
						  virtual - ExtraMath.volumeOfASphere(radius) ,
						  radius);
				  double bacillusVolumeIn2D = (Math.PI * 
						  radius *  radius * zLength ) + ( lengthOfVirtualRod * 
						  radius * 2.0 * zLength);
				  return representedDensityDouble * 
						  ( virtual / bacillusVolumeIn2D );
			  default:
				  Log.out(Tier.CRITICAL, this.getClass().getSimpleName()
				  		+ " attempting to scale " + body.getMorphology() + 
				  		" agent with undifined scaling procedure, returning "
				  		+ "input value");
				  return representedDensityDouble;
			}
		}
		
		/*
		 * If represented density is a map with different values for different
		 * types of mass, more complex method as below
		 */
		else
		{
			if (!(massObject instanceof Map))
			{
				Idynomics.simulator.interupt("Interrupted in " + 
						this.getClass().getSimpleName() + ". Represented "
								+ "density provided as a hashmap, but mass is"
								+ " not a hashmap. Structure of density "
								+ "aspect should match structure of mass "
								+ "aspect.");
			}
			
			Map<String, Double> representedDensityMap = 
					(HashMap<String, Double>) representedDensity;
			
			Double zLength = comp.getShape().
					getDimension(DimName.Z).getLength();
			
			double virtualCellVolume = 0.0;
			double virtualEpsVolume = 0.0;
			double totalVirtualVolume = virtualCellVolume + virtualEpsVolume;
			
			double scaling;
			HashMap<String, Double> scaledDensity = 
					new HashMap<String, Double>();
			
			
			Map<String,Double> massMap = (Map<String,Double>) massObject;
			
			for (String component : massMap.keySet())
			{
				if (!(representedDensityMap.containsKey(component)))
				{
					Idynomics.simulator.interupt("Interrupted in " + 
							this.getClass().getSimpleName() + ". No "
									+ "corresponding density for mass "
									+ "type " + component + " in density "
									+ "map.");
				}
				
				else
				{
					if (component.equals(this.EPS))
					{
						virtualEpsVolume += massMap.get(component) / 
							representedDensityMap.get(component);
					}
					
					else
					{
						virtualCellVolume += massMap.get(component) / 
							representedDensityMap.get(component);
					}
				}
			}
			
			switch(body.getMorphology()) 
			{
			case COCCOID:
				
				scaling =  ( totalVirtualVolume / (Math.PI *
					Math.pow(ExtraMath.radiusOfASphere( totalVirtualVolume ), 2)
					* zLength ) );
				
				for (String component : representedDensityMap.keySet())
				{
					scaledDensity.put(component,
							representedDensityMap.get(component) * scaling );
				}
				
				return scaledDensity;
				
				
			case BACILLUS:
				
				Double radius = aspectOwner.getDouble(RADIUS);
				/*
				 * Calculate length based on cell volume and fixed radius aspect
				 */
				double lengthOfVirtualRod = ExtraMath.lengthOfACylinder(
						  virtualCellVolume - ExtraMath.volumeOfASphere(radius)
						  , radius);
				
				/*
				 * Recalculate the radius taking into account all volume
				 * including EPS
				 */
				double radiusOfVirtualRod = ExtraMath.radiusOfACapsule(
						totalVirtualVolume, lengthOfVirtualRod);
				
				/*
				 * 2D bacillus based on final length and radius from above
				 */
				double bacillusVolumeIn2D = (Math.PI * 
						radiusOfVirtualRod *  radiusOfVirtualRod * zLength )
						+ ( lengthOfVirtualRod * radiusOfVirtualRod * 2.0 *
						zLength);
				
				scaling = totalVirtualVolume / bacillusVolumeIn2D;
				
				for (String component : representedDensityMap.keySet())
				{
					scaledDensity.put(component,
							representedDensityMap.get(component) * scaling );
				}
				
				return scaledDensity;
				
			default:
				  Log.out(Tier.CRITICAL, this.getClass().getSimpleName()
				  		+ " attempting to scale " + body.getMorphology() + 
				  		" agent with undefined scaling procedure, returning "
				  		+ "input value");
				  return representedDensityMap;
			}
		}
	}

}

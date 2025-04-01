package aspect.calculated;

import java.util.HashMap;
import java.util.Map;

import aspect.AspectInterface;
import aspect.Calculated;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import utility.Helper;

/**
 * 
 */
public class StructuredVolumeState extends Calculated {
	
	public String MASS = AspectRef.agentMass;
	public String DENSITY = AspectRef.agentDensity;
	public String REPRESENTED_DENSITY =
			AspectRef.agentRepresentedDensity;
	
	public StructuredVolumeState()
	{
		setInput("mass, density");
	}
	
	public Object get(AspectInterface aspectOwner)
	{
		Object densityObject = aspectOwner.getValue(DENSITY);
		Object massObject = aspectOwner.getValue(this.MASS);
		
		if ( !(densityObject instanceof Map ) )
		{
			if ( !(massObject instanceof Map ) )
			{
				double totalMass = Helper.totalMass(massObject);
				
				return totalMass / aspectOwner.getDouble(DENSITY);
			}
			
			else
			{
				Map<String, Double> massMap = 
						(Map<String, Double>) massObject;
				
				double density = (double) densityObject;
				
				Map<String, Double> volumeMap = 
						new HashMap<String, Double>();
				
				
				for (String string : (massMap).keySet())
				{
					volumeMap.put((String) string, 
							massMap.get(string) / density);
				}
				
				return volumeMap;
			}
			
		}
		
		else
		{
			if ( !(massObject instanceof Map ) )
			{
				Idynomics.simulator.interupt("Interrupted in " + 
						this.getClass().getSimpleName() + ". Density "
								+ "map provided, but no corresponding"
								+ "mass map.");
				return null;
			}
			
			else
			{
				Map<String, Double> massMap = 
						(Map<String, Double>) massObject;
				
				Map<String, Double> densityMap = 
						(Map<String, Double>) densityObject;
				
				Map<String, Double> volumeMap = 
						new HashMap<String, Double>();
				
				for (String component : massMap.keySet())
				{
					if (!(densityMap.containsKey(component)))
					{
						Idynomics.simulator.interupt("Interrupted in " + 
								this.getClass().getSimpleName() + ". No "
										+ "corresponding density for mass "
										+ "type " + component + " in density "
										+ "map.");
					}
					
					else
					{
						volumeMap.put( component, (massMap.get(component) / 
								densityMap.get(component)) );
					}
				}
				
				return volumeMap;
			}
		}
	}

}

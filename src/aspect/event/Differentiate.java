package aspect.event;

import java.util.Map;

import analysis.FilterLogic;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.ObjectFactory;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;

/**
 * \brief Agent growth where the agent has only one kind of biomass.
 * 
 * <p>For more complex growth, consider using the event InternalProduction
 * instead.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * 
 * TODO double check, clean-up
 */
public class Differentiate extends Event
{
	public String DIFF = AspectRef.differentiationMap;

	@Override
	public void start(AspectInterface initiator, 
			AspectInterface compliant, Double timeStep)
	{
		@SuppressWarnings("unchecked")
		Map<String,String> differentiations = 
			(Map<String,String>) initiator.getValue(DIFF);
		for ( String key : differentiations.keySet() )
		{
			if( FilterLogic.filterFromString( key ).match( initiator ))
			{
				String[] diffs = (String[]) ObjectFactory.loadObject( 
						differentiations.get(key), "String[]");
				if( diffs.length > 1 )
				{
					/* Replace module 0 with module 1, no species switch 
					 * TODO reflect this switch clearer in model output? */
					initiator.reg().removeModule( diffs[0]);
					initiator.reg().addModule( diffs[1] );
				}
				else
				{
					/* Switching to new species def, removing old modules */
					initiator.reg().removeModules();
					initiator.reg().addModule( diffs[0] );
					
					if( initiator.isAspect(XmlRef.species) )
						initiator.set(XmlRef.species, diffs[0]);
				}
			}
		}
	}
}

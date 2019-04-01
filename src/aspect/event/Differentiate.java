package aspect.event;

import java.util.Map;

import analysis.FilterLogic;
import aspect.AspectInterface;
import aspect.Event;
import dataIO.ObjectFactory;
import referenceLibrary.AspectRef;

/**
 * \brief Agent growth where the agent has only one kind of biomass.
 * 
 * <p>For more complex growth, consider using the event InternalProduction
 * instead.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
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
				String[] diffs = (String[]) ObjectFactory.loadObject( differentiations.get(key), "String[]");
				if( diffs.length > 1 )
				{
					initiator.reg().removeModule( diffs[0]);
					initiator.reg().addModule( diffs[1] );
				}
				else
				{
					initiator.reg().removeModules();
					initiator.reg().addModule( diffs[0] );
				}
			}
		}
	}
}

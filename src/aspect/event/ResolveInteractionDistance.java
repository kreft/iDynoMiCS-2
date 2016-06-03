package aspect.event;

import java.util.LinkedList;

import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import aspect.AspectRef;

/**
 * \brief Event that resolves current interaction distance/pull distance of
 * initiator with compliant and saves it as an aspect (#curPullDist).
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class ResolveInteractionDistance extends Event
{
	
	public String PULL_DIST = AspectRef.agentPulldistance;
	public String PREF_DIST = AspectRef.agentPreferencedistance;
	public String PREF_ID = AspectRef.agentPreferenceIdentifier;
	public String PREFERENCE = AspectRef.agentAttachmentPreference;
	public String LINKED = AspectRef.agentLinks;
	public String LINKER_DIST = AspectRef.linkerDistance;
	public String CURRENT_PULL_DIST = AspectRef.agentCurrentPulldistance;
	
	@SuppressWarnings("unchecked")
	public void start(AspectInterface initiator, 
								AspectInterface compliant, Double timeStep)
	{
		// NOTE currently they are added up not leveled
		double pullDist = initiator.isAspect(AspectRef.agentPulldistance) ?
				initiator.getDouble(AspectRef.agentPulldistance) : 0.0;
				
		pullDist += compliant.isAspect(AspectRef.agentPulldistance) ?
				compliant.getDouble(AspectRef.agentPulldistance) : 0.0;
		
		// NOTE they are currently not added up
		if ( initiator.isAspect(PREF_DIST) )
		{
			if ( initiator.getInt(PREFERENCE).equals(compliant.getInt(PREF_ID)))
			{
				pullDist = Math.max(initiator.getDouble(PREF_DIST), pullDist);
			}
		}
		else if(compliant.isAspect(PREF_DIST))
		{
			if(compliant.getInt(PREFERENCE).equals(initiator.getInt(PREF_ID)))
			{
				pullDist = Math.max(initiator.getDouble(PREF_DIST), pullDist);
			}
		}
		
		// TODO correct behavior for filial link breakage
		if ( initiator.isAspect(LINKER_DIST) )
		{
			if ( initiator.isAspect(LINKED) )
			{
				LinkedList<Integer> linkers = (LinkedList<Integer>) 
						initiator.getValue(LINKED);
				if ( linkers.contains(((Agent) compliant).identity()) )
				{
					pullDist = Math.max(initiator.getDouble(LINKER_DIST), 
							pullDist);
				}
			}
		}
		if ( compliant.isAspect(LINKER_DIST) )
		{
			if ( compliant.isAspect(LINKED) )
			{
				LinkedList<Integer> linkers = (LinkedList<Integer>) 
						compliant.getValue(LINKED);
				if ( linkers.contains(((Agent) initiator).identity()) )
				{
					pullDist = Math.max(compliant.getDouble(LINKER_DIST), 
							pullDist);
				}
			}
		}
		
		initiator.set(CURRENT_PULL_DIST, pullDist);
	}
}

package agent.event.library;

import java.util.LinkedList;

import agent.Agent;
import aspect.AspectInterface;
import aspect.Event;
import idynomics.NameRef;

/**
 * \brief Method that initiates the division.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class ResolveInteractionDistance extends Event
{
	@SuppressWarnings("unchecked")
	public void start(AspectInterface initiator, 
								AspectInterface compliant, Double timeStep)
	{
		// NOTE currently they are added up not leveled
		double pullDist = initiator.isAspect(NameRef.agentPulldistance) ?
				initiator.getDouble(NameRef.agentPulldistance) : 0.0;
				
		pullDist += compliant.isAspect(NameRef.agentPulldistance) ?
				compliant.getDouble(NameRef.agentPulldistance) : 0.0;
		
		// NOTE they are currently not added up
		if ( initiator.isAspect("prefDist") )
		{
			if ( initiator.getInt("preference").equals(compliant.getInt("prefIdentifier")) )
			{
				pullDist = Math.max(initiator.getDouble("prefDist"), pullDist);
			}
		}
		else if(compliant.isAspect("prefDist"))
		{
			if(compliant.getInt("preference").equals(initiator.getInt("prefIdentifier")))
			{
				pullDist = Math.max(initiator.getDouble("prefDist"), pullDist);
			}
		}
		
		// TODO correct behavior for filial link breakage
		if ( initiator.isAspect("linkerDist") )
		{
			if ( initiator.isAspect("linkedAgents") )
			{
				LinkedList<Integer> linkers = (LinkedList<Integer>) 
						initiator.getValue("linkedAgents");
				if ( linkers.contains(((Agent) compliant).identity()) )
				{
					pullDist = Math.max(initiator.getDouble("linkerDist"), pullDist);
				}
			}
		}
		if ( compliant.isAspect("linkerDist") )
		{
			if ( compliant.isAspect("linkedAgents") )
			{
				LinkedList<Integer> linkers = (LinkedList<Integer>) 
						compliant.getValue("linkedAgents");
				if ( linkers.contains(((Agent) initiator).identity()) )
				{
					pullDist = Math.max(compliant.getDouble("linkerDist"), pullDist);
				}
			}
		}
		
		initiator.set("#curPullDist", pullDist);
	}
}

package agent.event.library;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Event;

public class InternalProduction  extends Event {

	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		if ( initiator.isAspect("internalProduction"))
		{
			@SuppressWarnings("unchecked")
			HashMap<String,Double> internalProduction = 
					(HashMap<String,Double>) initiator.getValue("internalProduction");
			
			@SuppressWarnings("unchecked")
			HashMap<String,Double> internalProducts = 
					(initiator.isAspect("internalProducts") ? (HashMap<String,
					Double>) initiator.getValue("internalProducts") :
					new HashMap<String,Double>());
			
			
			for( String p : internalProduction.keySet())
			{
				double product = (internalProducts.containsKey(p) ?
						internalProducts.get(p) : 0.0);
				double rate = internalProduction.get(p);
				internalProducts.put(p, product + rate * timeStep);
			}
			initiator.set("internalProducts", internalProducts);
		}
	}
}

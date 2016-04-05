package agent.event.library;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Event;
import idynomics.NameRef;

public class InternalProduction  extends Event {
	
	public String INTERNAL_PRODUCTS = NameRef.internalProducts;
	public String INTERNAL_PRODUCTION = NameRef.internalProduction;

	public void start(AspectInterface initiator, AspectInterface compliant, Double timeStep)
	{
		if ( initiator.isAspect(INTERNAL_PRODUCTION))
		{
			@SuppressWarnings("unchecked")
			HashMap<String,Double> internalProduction = 
					(HashMap<String,Double>) initiator.getValue(INTERNAL_PRODUCTION);
			
			@SuppressWarnings("unchecked")
			HashMap<String,Double> internalProducts = 
					(initiator.isAspect(INTERNAL_PRODUCTS) ? (HashMap<String,
					Double>) initiator.getValue(INTERNAL_PRODUCTS) :
					new HashMap<String,Double>());
			
			
			for( String p : internalProduction.keySet())
			{
				double product = (internalProducts.containsKey(p) ?
						internalProducts.get(p) : 0.0);
				double rate = internalProduction.get(p);
				internalProducts.put(p, product + rate * timeStep);
			}
			initiator.set(INTERNAL_PRODUCTS, internalProducts);
		}
	}
}

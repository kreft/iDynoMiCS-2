package aspect.event;

import java.util.HashMap;

import aspect.AspectInterface;
import aspect.Event;
import idynomics.NameRef;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class InternalProduction  extends Event
{
	
	public String INTERNAL_PRODUCTS = NameRef.internalProducts;
	public String INTERNAL_PRODUCTION = NameRef.internalProductionRate;

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
			for( String p : internalProduction.keySet() )
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

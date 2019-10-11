package boundary.library;

import org.w3c.dom.Element;

import boundary.Boundary;
import compartment.Compartment;
import settable.Settable;

/**
 * \brief Connective boundary linking one dimensionless compartment to another.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToChemostat extends ChemostatBoundary
{
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 */

	public ChemostatToChemostat()
	{
		super();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		// TODO Auto-generated method stub
		
	}

	
	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return ChemostatToChemostat.class;
	}

	@Override
	public Boundary makePartnerBoundary()
	{
		ChemostatToChemostat cIn = 
				(ChemostatToChemostat) super.makePartnerBoundary();
		cIn.setVolumeFlowRate( - this._volumeFlowRate);
		return cIn;
	}

	/**
	 * \brief Make a partner boundary and set it to the compartment given.
	 * 
	 * @param comp
	 */
	// TODO consider deletion
	public void setPartnerCompartment(Compartment comp)
	{
		Boundary cIn = this.makePartnerBoundary();
		comp.addBoundary(cIn);
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/

	@Override
	public void additionalPartnerUpdate() {}
}

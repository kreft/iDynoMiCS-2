/**
 * 
 */
package boundary.library;

import java.util.Collection;

import org.w3c.dom.Element;

import agent.Agent;
import boundary.Boundary;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import boundary.standardBehaviours.DilutionAgentOutflowBehaviour;
import settable.Settable;

/**
 * \brief Boundary connecting a dimensionless compartment to a compartment
 * this spatial structure.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ChemostatToBoundaryLayer extends Boundary
{
	/**
	 * \brief This boundary's behaviour for grabbing agents to be removed by
	 * outflow.
	 * 
	 * Encapsulated here as it is used by many other chemostat boundaries.
	 */
	private DilutionAgentOutflowBehaviour _agentOutflowBehaviour;
	
	public ChemostatToBoundaryLayer()
	{
		super();
		this._agentOutflowBehaviour = new DilutionAgentOutflowBehaviour();
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) {
		// TODO Auto-generated method stub
		
	}

	/* ************************************************************************
	 * PARTNER BOUNDARY
	 * ***********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return BiofilmBoundaryLayer.class;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	// TODO [Rob 13June2016]: We need to grab agents from the chemostat here,
	// in a similar way to ChemostatToChemostat, but there is no "flow rate".


	@Override
	public Collection<Agent> agentsToGrab()
	{
		return this._agentOutflowBehaviour.agentsToGrab(
				this._agents, this.getDilutionRate());
	}
}

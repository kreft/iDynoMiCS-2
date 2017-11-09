package boundary.library;

import java.util.Collection;
import java.util.LinkedList;

import agent.Agent;
import boundary.Boundary;
import idynomics.Idynomics;
import utility.ExtraMath;

/**
 * \brief abstract class that captures identical agent transfer behvior for
 * chemostat boundaries. 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public abstract class ChemostatBoundary extends Boundary {
	
	protected boolean _agentRemoval = true;
	
	public ChemostatBoundary()
	{
		super();
	}

	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/

	/**
	 * The number of agents at time t can be found using a simple 
	 * differential equation:
	 * 
	 * dA/dt = rA
	 * A(t) = A(0) * e^(rt)
	 * 
	 * here r is the removal rate and A is the number of agents.
	 * Translating this to agent based we can say the chance of any
	 * agent being removed over time t equals e^(rt).
	 */
	@Override
	public Collection<Agent> agentsToGrab()
	{
		int nAllAgents = this._agents.getNumAllAgents();
		LinkedList<Agent> removals = new LinkedList<Agent>();
		if ( (nAllAgents > 0) && (this._volumeFlowRate < 0.0) )
		{
			/* do not remove if agent removal is disabled */
			if ( _agentRemoval )
			{
				/* calculate (1 - removal chance) */
				double e = Math.exp( ( this.getDilutionRate() * 
						Idynomics.simulator.timer.getTimeStepSize() ) ); 
				
				for ( int i = 0; i < nAllAgents; i++ )
				{
					if( ExtraMath.getUniRandDbl() > e )
						removals.add( this._agents.chooseAgent(i) );
				}
			}
		}
		return removals;
						
	}

}

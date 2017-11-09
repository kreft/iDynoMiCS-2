package boundary.library;

import java.util.Collection;

import agent.Agent;
import boundary.Boundary;
import dataIO.Log;
import dataIO.Log.Tier;
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
		if ( (nAllAgents > 0) && (this._volumeFlowRate < 0.0) )
		{
			/* do not remove if agent removal is disabled */
			if ( _agentRemoval )
			{
				/* calculate removal chance */
				double e = Math.exp( ( this.getDilutionRate() * 
						Idynomics.simulator.timer.getTimeStepSize() ) ); 
				for ( int i = 0; i < nAllAgents; i++ )
					if( ExtraMath.getUniRandDbl() > e )
					{
						/* add to be removed agents to departure launch */
						Agent a = this._agents.chooseAgent(i);
						if ( !this._departureLounge.contains(a))
								this._departureLounge.add(a);
						if ( Log.shouldWrite(Tier.DEBUG) )
							Log.out(Tier.DEBUG, "Washed out agent");
					}
			}
		}
		return this._departureLounge;
						
	}

}

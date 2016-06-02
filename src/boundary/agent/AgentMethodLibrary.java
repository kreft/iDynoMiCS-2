/**
 * 
 */
package boundary.agent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import dataIO.Log.Tier;
import dataIO.Log;
import idynomics.AgentContainer;
import shape.Dimension.DimName;
/**
 * \brief Collection of commonly used methods for 
 * {@code Agent}-{@code Boundary} interactions.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class AgentMethodLibrary
{
	/**************************************************************************
	 * SPATIAL BOUNDARIES
	 *************************************************************************/

	/**
	 * \brief Agent boundary method that does not allow agents top cross it.
	 * 
	 * <p>Agents in the arrivals lounge are placed at a random location along
	 * the boundary surface: this may be useful for simulation 
	 * initialisation.</p>
	 */
	public static class SolidSurface extends AgentMethod
	{
		@Override
		public String getName()
		{
			return "Solid surface";
		}

		@Override
		public void agentsArrive(
				AgentContainer agentCont, DimName dimN, int extreme)
		{
			this.placeAgentsRandom(agentCont, dimN, extreme);
			this._arrivalsLounge.clear();
		}

		@Override
		public void agentsArrive(AgentContainer agentCont)
		{
			/* Do nothing! */
		}

		@Override
		public List<Agent> agentsToGrab(AgentContainer agentCont,
				DimName dimN, int extreme, double timeStep)
		{
			/* Do nothing! */
			return new LinkedList<Agent>();
		}

		@Override
		public int agentsToGrab(AgentContainer agentCont, double timeStep)
		{
			return 0;
		}
	}

	/**
	 * \brief Agent boundary method to use in a biofilm that is connected to
	 * a chemostat compartment.
	 */
	// TODO rename to something more agent-specific?
	public static class BoundaryLayer extends AgentMethod
	{
		/**
		 * Boundary layer thickness.
		 */
		// TODO set this!
		private double _layerTh = 10.0;

		@Override
		public String getName()
		{
			return "Boundary layer";
		}

		@Override
		public void agentsArrive(
				AgentContainer agentCont, DimName dimN, int extreme)
		{
			/*
			 * Unlocated agents can be simply added to the Compartment.
			 */
			for ( Agent anAgent : this._arrivalsLounge )
				if ( ! AgentContainer.isLocated(anAgent) )
				{
					agentCont.addAgent(anAgent);
					this._arrivalsLounge.remove(anAgent);
				}
			/*
			 * Give all (located) agents a random position along this boundary
			 * surface.
			 */
			this.placeAgentsRandom(agentCont, dimN, extreme);
			/*
			 * Calculate the step size and direction that agents will use to
			 * move.
			 */
			// NOTE Rob [19/5/2016]: the value of 0.1 is arbitrary.
			double dist = 0.1 * this._layerTh;
			if ( extreme == 1 )
				dist = -dist;
			/*
			 * Move each agent away from the boundary surface until it reaches
			 * the top of the boundary layer.
			 */
			Collection<Agent> nbhAgents;
			Collection<AgentMethod> bndries;
			boolean hasCollided = false;
			for ( Agent anAgent : this._arrivalsLounge )
			{
				Log.out(Tier.DEBUG, "Moving agent (UID: "+anAgent.identity()+
						") to top of boundary layer");
				// NOTE Rob [19/5/2016]: this loop is work in progress.
				while ( ! hasCollided )
				{
					nbhAgents = agentCont.treeSearch(anAgent, this._layerTh);
					if ( ! nbhAgents.isEmpty() )
					{
						hasCollided = true;
						break;
					}
					bndries = agentCont.boundarySearch(anAgent, this._layerTh);
					if ( ! bndries.isEmpty() )
					{
						// FIXME stopping is a temporary fix: we need to apply
						// the boundary here
						hasCollided = true;
						break;
					}
					agentCont.moveAlongDimension(anAgent, dimN, dist);
				}
				// TODO ask the agent to move now?
			}
			this._arrivalsLounge.clear();
		}

		@Override
		public void agentsArrive(AgentContainer agentCont)
		{
			/* Do nothing! */
		}

		@Override
		public List<Agent> agentsToGrab(AgentContainer agentCont,
				DimName dimN, int extreme, double timeStep)
		{
			List<Agent> out = new LinkedList<Agent>();
			/*
			 * Find all agents who are unattached to others or to a boundary,
			 * and who are on this side of the biofilm (in, e.g., the case of a
			 * floating granule).
			 */
			// TODO
			return out;
		}

		@Override
		public int agentsToGrab(AgentContainer agentCont, double timeStep)
		{
			return 0;
		}
	}

	/**************************************************************************
	 * CHEMOSTAT BOUNDARIES
	 *************************************************************************/

	/**
	 * \brief 
	 */
	public static class DilutionIn extends AgentMethod
	{
		@Override
		public String getName()
		{
			return "Dilution in";
		}

		@Override
		public void agentsArrive(
				AgentContainer agentCont, DimName dimN, int extreme)
		{
			/*
			 * In the unexpected case that this method is called, use the
			 * non-spatial method.
			 */
			this.agentsArrive(agentCont);
		}

		@Override
		public void agentsArrive(AgentContainer agentCont)
		{
			for ( Agent anAgent : this._arrivalsLounge )
				agentCont.addAgent(anAgent);
			this._arrivalsLounge.clear();
		}

		@Override
		public List<Agent> agentsToGrab(AgentContainer agentCont,
				DimName dimN, int extreme, double timeStep)
		{
			/* Do nothing! */
			return new LinkedList<Agent>();
		}

		@Override
		public int agentsToGrab(AgentContainer agentCont, double timeStep)
		{
			return 0;
		}
	}

	/**
	 * \brief 
	 */
	public static class DilutionOut extends AgentMethod
	{
		protected double _flowRate;

		protected double _agentsToDiluteTally;

		@Override
		public String getName()
		{
			return "Dilution out";
		}

		public void setFlowRate(double flowRate)
		{
			this._flowRate = flowRate;
		}

		@Override
		public void agentsArrive(
				AgentContainer agentCont, DimName dimN, int extreme)
		{
			/* Do nothing! */
		}

		@Override
		public void agentsArrive(AgentContainer agentCont)
		{
			/* Do nothing! */
		}

		@Override
		public List<Agent> agentsToGrab(AgentContainer agentCont,
				DimName dimN, int extreme, double timeStep)
		{
			return new LinkedList<Agent>();
		}

		@Override
		public int agentsToGrab(AgentContainer agentCont, double timeStep)
		{
			// TODO break this into two steps, update and get?
			/* Remember to subtract, since flow rate out is negative. */
			this._agentsToDiluteTally -= this._flowRate * timeStep;
			return (int) this._agentsToDiluteTally;
		}

		@Override
		public void addOutboundAgent(Agent anAgent)
		{
			super.addOutboundAgent(anAgent);
			this._agentsToDiluteTally--;
		}
	}
}

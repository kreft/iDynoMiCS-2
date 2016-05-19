/**
 * 
 */
package boundary.agent;

import java.util.Collection;

import agent.Agent;
import idynomics.AgentContainer;
import shape.ShapeConventions.DimName;
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
	}
	
	/**
	 * \brief 
	 * 
	 */
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
			this.placeAgentsRandom(agentCont, dimN, extreme);
			Collection<Agent> nbhAgents;
			Collection<AgentMethod> boundaries;
			boolean hasCollided = false;
			for ( Agent anAgent : this._arrivalsLounge )
			{
				if ( ! AgentContainer.isLocated(anAgent) )
				{
					agentCont.addAgent(anAgent);
					continue;
				}
				// NOTE Rob [19/5/2016]: this loop is work in progress.
				while ( ! hasCollided )
				{
					nbhAgents = agentCont.treeSearch(anAgent, this._layerTh);
					if ( ! nbhAgents.isEmpty() )
					{
						hasCollided = true;
						break;
					}
					boundaries = 
							agentCont.boundarySearch(anAgent, this._layerTh);
					if ( ! boundaries.isEmpty() )
					{
						// TODO
						
					}
					// TODO Rob [19/5/2016]: the value of 0.1 is arbitrary.
					agentCont.moveAlongDimension(
							anAgent, dimN, 0.1*this._layerTh);
				}
			}
			this._arrivalsLounge.clear();
		}

		@Override
		public void agentsArrive(AgentContainer agentCont)
		{
			/* Do nothing! */
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
	}
}

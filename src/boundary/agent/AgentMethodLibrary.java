/**
 * 
 */
package boundary.agent;

import java.util.Collection;

import agent.Agent;
import idynomics.AgentContainer;
import shape.ShapeConventions.DimName;
/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class AgentMethodLibrary
{
	
	
	
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
				while ( ! hasCollided )
				{
					
					nbhAgents = agentCont.treeSearch(anAgent, this._layerTh);
					if ( ! nbhAgents.isEmpty() )
						hasCollided = true;
					boundaries = agentCont.boundarySearch(anAgent, this._layerTh);
					if ( ! boundaries.isEmpty() )
					{
						// TODO
					}
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
			/* Do nothing! */
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

/**
 * 
 */
package boundary.agent;

import java.util.Collection;
import java.util.List;

import agent.Agent;
import agent.Body;
import idynomics.AgentContainer;
import idynomics.NameRef;
import shape.Shape;
import shape.ShapeConventions.DimName;
import surface.BoundingBox;
import surface.Collision;
import surface.Surface;

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
		// TODO set this!
		private double _layerThickness = 10.0;
		
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
			Shape shape = agentCont.getShape();
			Collection<Surface> shapeSurfs = shape.getSurfaces();
			Collision collision = new Collision(null, shape);
			Body body;
			List<BoundingBox> boxes;
			for ( Agent anAgent : this._arrivalsLounge )
			{
				if ( AgentContainer.isLocated(anAgent) )
				{
					body = (Body) anAgent.get(NameRef.agentBody);
					
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

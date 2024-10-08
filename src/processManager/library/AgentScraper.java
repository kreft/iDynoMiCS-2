package processManager.library;

import java.util.LinkedList;
import java.util.List;

import linearAlgebra.Vector;
import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import processManager.ProcessDeparture;
import referenceLibrary.AspectRef;
import surface.Point;
import surface.Surface;
import surface.Voxel;
import utility.Helper;


/**
 * Simple process that removes agents above a certain height.
 * This can be used to maintain a maximum thickness for a biofilm.
 * 
 * Author - Tim Foster trf896@student.bham.ac.uk,
 * Bastiaan Cockx @BastiaanCockx (baco@dtu.dk), DTU, Denmark.
 *
 */


public class AgentScraper extends ProcessDeparture {
	
	private String MAX_THICKNESS = AspectRef.maxThickness;
	
	private double _maxThickness;

	private boolean _centerPointRemoval;
	
	public void init( Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		this._maxThickness = Helper.setIfNone( 
				this.getDouble( MAX_THICKNESS ),
				agents.getShape().getDimensionLengths()[1] );

		this._centerPointRemoval = Helper.setIfNone(
				this.getBoolean( AspectRef.centerPointRemoval ),
				false );
	}

	/**
	 *
	 * @return List of agents to be removed
	 *
	 * TODO: currently the agent scraper always considers dimension [1] for scraping, if this is made settable by the
	 * user, the scraper can also be used for other model configurations.
	 */
	@Override
	public LinkedList<Agent> agentsDepart()
	{
		LinkedList<Agent> departures = new LinkedList<Agent>();

		/* scrape agents based on their mass point position */
		if(this._centerPointRemoval) {
			List<Agent> allAgents = this._agents.getAllAgents();

			for (Agent a : allAgents) {
				List<Point> points = ((Body) a.getValue(AspectRef.agentBody)).
						getPoints();
				for (Point p : points) {
					if (p.getPosition()[1] > this._maxThickness) {
						departures.add(a);
						break;
					}
				}
			}
		}
		/* scrape agents based on agent-region collision */
		else {
			double[] dimLengths = this._agents.getShape().getDimensionLengths();
			double[] lowerBounds = Vector.zeros(dimLengths);
			lowerBounds[1] += this._maxThickness;
			/* note that in this specific case we don't need additional steps to confirm actual collision as voxels is
			identical to it's bounding box and we are looking at the full depth and with of the domain */
			departures.addAll( this._agents.treeSearch(new Voxel(lowerBounds,dimLengths),0.0) );
		}
		return departures;
	}
	
	
}

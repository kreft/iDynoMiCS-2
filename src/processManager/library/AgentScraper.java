package processManager.library;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import processManager.ProcessDeparture;
import referenceLibrary.AspectRef;
import surface.Point;
import utility.Helper;


/**
 * Simple process that removes agents above a certain height.
 * This can be used to maintain a maximum thickness for a biofilm.
 * 
 * Author - Tim Foster trf896@student.bham.ac.uk
 *
 */


public class AgentScraper extends ProcessDeparture {
	
	private String MAX_THICKNESS = AspectRef.maxThickness;
	
	private double _maxThickness;
	
	public void init( Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		this._maxThickness = Helper.setIfNone( 
				this.getDouble( MAX_THICKNESS ),
				agents.getShape().getDimensionLengths()[1] );
	}
	
	@Override
	public LinkedList<Agent> agentsDepart()
	{
		LinkedList<Agent> departures = new LinkedList<Agent>();
		
		List <Agent> allAgents = this._agents.getAllAgents();
		
		for ( Agent a : allAgents )
		{
			List<Point> points = ((Body) a.getValue(AspectRef.agentBody)).
					getPoints();
			for (Point p: points)
			{
				if (p.getPosition()[1] > this._maxThickness)
				{
					departures.add(a);
					break;
				}
			}
		}
		
		return departures;
	}
	
	
}

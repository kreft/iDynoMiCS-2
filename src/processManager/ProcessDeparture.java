package processManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import bookkeeper.KeeperEntry.EventType;
import boundary.SpatialBoundary;
import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import instantiable.object.InstantiableMap;
import processManager.library.AgentsOutsideDomainDepart;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import surface.Point;
import utility.ExtraMath;
import utility.Helper;

/**
 * Abstract class for processes that manage the departure of agents from a
 * compartment. Extending classes should provide a list of agents to depart
 * which will then be passed to the relevant arrivals lounge by this super class.
 * 
 * @author Tim Foster - trf896@student.bham.ac.uk
 */

public abstract class ProcessDeparture extends ProcessManager {
	
	private DepartureType _departureType;
	
	private InstantiableMap<String, Double> _destinationNames;
	
	private HashMap<Compartment, Double> _destinations;
	
	private double _departureWeightsTotal;
	
	private LinkedList<Agent> _departureLounge;
	
	protected Shape _shape;
	
	public enum DepartureType
	{
		REMOVAL,
		
		TRANSFER,
	}
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		this._shape = this._agents.getShape();
		
		if (this.isLocalAspect(XmlRef.destinationNames))
		{
			if (this.getValue(XmlRef.destinationNames) 
					instanceof InstantiableMap<?,?>)
				{
					this._destinationNames = 
							(InstantiableMap<String, Double>) this.getValue(
									XmlRef.destinationNames);
					this._departureWeightsTotal = 0.0;
					
					this._departureType = DepartureType.TRANSFER;
				}
			else
			{
				if (Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, "destinationNames provided is not"
							+ "an InstantiableMap. Agents will be removed and"
							+ "not transferred.");
				this._departureType = DepartureType.REMOVAL;
			}
		}
		
		else
		{
			this._departureType = DepartureType.REMOVAL;
		}
	}


	@Override
	protected void internalStep() 
	{
	switch (_departureType)
	{
		
		case REMOVAL:
		{
			this._departureLounge = this.agentsDepart();

			LinkedList<Agent> agentsOutsideOrLeaving =
					this.agentsOutsideOrLeavingDomain();

			if (!agentsOutsideOrLeaving.isEmpty() &&
					!(this instanceof AgentsOutsideDomainDepart))
			{
				if (Log.shouldWrite(Tier.NORMAL))
					Log.out(Tier.NORMAL, "Departure process " + this._name +
							" encountered agents leaving the computational "
							+ "domain. Adding to departure lounge.");
			}

			this._departureLounge.addAll(agentsOutsideOrLeaving);

			this._agents.registerRemoveAgents(this._departureLounge,
					EventType.REMOVED, "Removed from simulation by departure "
						+ "process" + this.getName(), null);
			this._departureLounge.clear();
			break;
		}

		case TRANSFER:
		{
		
			//Check whether destinations have been stored in _destinations.
			//If not, get them from the InstantiableMap _destinationNames.
			if (Helper.isNullOrEmpty(this._destinations))
			{
				this._destinations = new HashMap<Compartment, Double>();
				for (String k : _destinationNames.keySet())
				{
					Compartment destination = 
							Idynomics.simulator.getCompartment(k);
					
					if (Helper.isNullOrEmpty(destination))
					{
						if (Log.shouldWrite(Tier.CRITICAL))
							Log.out(Tier.CRITICAL, "Could not assign a "
									+ "destination compartment to departure"
									+ " process " + this.getName() + "in "
									+ "compartment " + this._compartmentName + 
									". Input provided: " + k);
					}
					else
					{
						double departureWeight = _destinationNames.get(k);
						_destinations.put(destination, departureWeight);
						this._departureWeightsTotal += departureWeight;
					}
				}
			}
			
			//Carry out process-specific agent departure mechanism, which fills
			//the departure lounge
			this._departureLounge = this.agentsDepart();
			
			LinkedList<Agent> agentsOutsideOrLeaving = 
					this.agentsOutsideOrLeavingDomain();
			
			if (!agentsOutsideOrLeaving.isEmpty() && 
					!(this instanceof AgentsOutsideDomainDepart))
			{
				if (Log.shouldWrite(Tier.NORMAL))
					Log.out(Tier.NORMAL, "Departure process " + this._name +
							" encountered agents leaving the computational "
							+ "domain. Adding to departure lounge.");
			}
			
			this._departureLounge.addAll(agentsOutsideOrLeaving);
			
			
			if (!this._departureLounge.isEmpty())
			{
				//Send agents in departure lounge to their destinations
				
				int dLSize = this._departureLounge.size();
				LinkedList<Compartment> destinations = 
						new LinkedList<Compartment>(_destinations.keySet());
				
				//Shuffling removes systematic error that might result
				//from destinations always being stepped in the same order
				Collections.shuffle(destinations, ExtraMath.random);
				Collections.shuffle(this._departureLounge, ExtraMath.random);
				
				//This will loop through all destinations but the last
				for (int i = 0; i < destinations.size() - 1; i++)
				{
					//The proportion of agents in the departure lounge that
					//will go to this particular destination.
					double departProportion = this._destinations.get
							(destinations.get(i))/this._departureWeightsTotal;
					
					//the number of agents to depart to this particular
					//destination.
					int numberDeparting = (int) 
							Math.round(dLSize*departProportion);
					
					int departed = 0;
					
					//Build list of agents departing to a particular destination
					//Agents leave departure lounge and enter temporary list,
					//departingAgents
					LinkedList<Agent> departingAgents = new LinkedList<Agent>();
					for (Agent a : this._departureLounge)
					{
						if (departed < numberDeparting)
						{
							departingAgents.add(a);
							departed++;
						}
					}
					for (Agent d : departingAgents)
						this._departureLounge.remove(d);
					
					//Remove departing agents from this compartment's 
					//AgentContainer.
					this._agents.registerRemoveAgents(departingAgents, 
						EventType.TRANSFER, "Transferred from compartment " + 
								this._compartmentName + "to new compartment, " +
								destinations.get(i).getName() + " by "
								+ "departure process" + this._name, null);
					
					//Send departing agents to their destination
					destinations.get(i).acceptAgents(
							this._compartmentName, departingAgents);
					
					//Now clear departingAgents list before continuing to next
					//destination in the loop
					departingAgents.clear();
				}
				
				//Last destination receives all remaining agents
				if (!this._departureLounge.isEmpty())
				{
					//Remove departing agents from this compartment's
					//AgentContainer.
					//TODO - ask Bas what parameters event and value are for.
					this._agents.registerRemoveAgents(this._departureLounge,
						EventType.TRANSFER, "Transferred from compartment " +
							this._compartmentName + " to new compartment, " +
							destinations.get(destinations.size()-1).getName()
							+ " by departure process " + this._name, null);

					//Send departing agents to their destination
					destinations.get(destinations.size()-1).acceptAgents(
							this._compartmentName, this._departureLounge);

					this._departureLounge.clear();
				}
				
				//Refresh the spatial registry of the agent container that the
				//agents have just departed.
				this._agents.refreshSpatialRegistry();
			}
		}
	}
	}
	
	/**
	 * A method that fills the departure lounge with outbound agents.
	 * To be overwritten in implementing methods.
	 */
	protected abstract LinkedList<Agent> agentsDepart();
	
	protected LinkedList<Agent> agentsOutsideOrLeavingDomain()
	{
		LinkedList<Agent> agentsToDepart = new LinkedList<Agent>();

		if (this._shape.getNumberOfDimensions() > 0)
		{
			for (Agent a : this._agents.getAllAgents())
			{

				/*
				 * Find agents that are outside the computational domain.
				 */
				Body body = (Body) a.get(AspectRef.agentBody);

				for (Point p : body.getPoints())
				{
					this._shape.applyBoundaries(p.getPosition());

					if (!this._shape.isInside(p.getPosition()))
					{
						agentsToDepart.add(a);
					}
				}

				/*
				 * Find agents colliding with spatial boundaries that are not
				 * solid.
				 *
				 * FIXME: this may be better handled by the boundary themselves eg: ask a boundary
				 *  whether an agent should be considered for removal.
				 */
				Collection<SpatialBoundary> collidingBoundaries =
						this._agents.boundarySearch(a, 0.0);

				for (SpatialBoundary boundary : collidingBoundaries)
				{
					if (!boundary.isSolid())
					{
						agentsToDepart.add(a);
					}
				}

			}
		}
		
		return agentsToDepart;
	}
	
	
	public void setDepartureType(DepartureType dt)
	{
		this._departureType = dt;
	}
	
	public void setShape (Shape shape)
	{
		this._shape = shape;
	}
	
}

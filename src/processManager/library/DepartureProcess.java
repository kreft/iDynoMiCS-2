package processManager.library;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import bookkeeper.KeeperEntry.EventType;
import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import instantiable.object.InstantiableMap;
import processManager.ProcessManager;
import referenceLibrary.XmlRef;
import utility.ExtraMath;
import utility.Helper;

public abstract class DepartureProcess extends ProcessManager {
	
	private DepartureType _departureType;
	
	private InstantiableMap<String, Double> _destinationNames;
	
	private HashMap<Compartment, Double> _destinations;
	
	private double _departureWeightsTotal;
	
	private LinkedList<Agent> _departureLounge;
	
	private enum DepartureType
	{
		REMOVAL,
		
		TRANSFER,
	}
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		if (this.isLocalAspect(XmlRef.destinationNames))
		{
			if (this.getValue(XmlRef.destinationNames) instanceof InstantiableMap<?,?>)
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
			this._agents.registerRemoveAgents(this._departureLounge, 
					EventType.REMOVED, "Removed from simulation by departure "
						+ "process", "??");
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
			
			
			//Remove departing agents from this compartment's AgentContainer.
			//TODO - ask Bas what parameters event and value are for.
			this._agents.registerRemoveAgents(this._departureLounge, 
					EventType.TRANSFER, "Transferred to new compartment by "
							+ "departure process", "??");
			
			//Send agents in departure lounge to their destinations
			
			int dLSize = this._departureLounge.size();
			LinkedList<Compartment> destinations = 
					new LinkedList<Compartment>(_destinations.keySet());
			
			Collections.shuffle(destinations, ExtraMath.random);
			Collections.shuffle(this._departureLounge, ExtraMath.random);
			
			//This will loop through all destinations but the last
			for (int i = 0; i < destinations.size() - 1; i++)
			{
				double departProportion = this._destinations.get
						(destinations.get(i))/this._departureWeightsTotal;
				
				//the number of agents to depart to this particular compartment
				int numberDeparting = (int) 
						Math.round(dLSize*departProportion);
				
				int departed = 0;
				
				//List of agents departing to a particular destination
				LinkedList<Agent> departingAgents = new LinkedList<Agent>();
				for (Agent a : this._departureLounge)
				{
					if (departed < numberDeparting)
					{
						departingAgents.add(a);
						this._departureLounge.remove(a);
						departed++;
					}
				}
				destinations.get(i).acceptAgents(
						this._compartmentName, departingAgents);
			}
			//Last destination receives all remaining agents
			if (!this._departureLounge.isEmpty())
			{
				destinations.get(destinations.size()-1).acceptAgents(
						this._compartmentName, this._departureLounge);
			}
		}
	}
	}
	
	/**
	 * A method that fills the departure lounge with outbound agents.
	 * To be overwritten in implementing methods.
	 */
	public abstract LinkedList<Agent> agentsDepart();
	
}

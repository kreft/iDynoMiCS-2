package processManager;

import java.util.LinkedList;

import org.w3c.dom.Element;

import agent.Agent;
import compartment.AgentContainer;
import compartment.Compartment;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import instantiable.object.InstantiableList;
import referenceLibrary.XmlRef;

/**
 * Abstract class for processes that manage the arrival of agents in a
 * compartment. Extending classes are passed a list of agents from the arrivals
 * lounge and should position and insert agents into the agent container.
 * 
 * @author Tim Foster - trf896@student.bham.ac.uk
 */

public abstract class ProcessArrival extends ProcessManager {
	
	protected LinkedList<Agent> _arrivals;
	protected Compartment _compartment;
	private InstantiableList<String> _originNames;
	private boolean _originsChecked = false;
	
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		this._compartment = 
				Idynomics.simulator.getCompartment(this._compartmentName);
		
		this._arrivals = new LinkedList<Agent>();
		
		if (this.isLocalAspect(XmlRef.originNames))
		{
			if (this.getValue(XmlRef.originNames) instanceof InstantiableList<?>)
			{
				this._originNames = (InstantiableList<String>)
						this.getValue(XmlRef.originNames);
			}
		}
		else
		{
			if (Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, "No origin list provided for "
						+ "ArrivalProcess " + this._name + ". Please provide "
						+ "an InstantiableList<String> with the names of"
						+ "origin compartments for this ArrivalProcess. "
						+ "Producing empty origins list.");
			this._originNames = new InstantiableList<String>();
		}
	}
	
	@Override
	protected void internalStep() 
	{
		//During the first step, origin names are checked and a warning is
		//issued if they do not match the names of any compartments.
		if (!this._originsChecked)
		{
			for (String origin : this._originNames)
			{
				boolean match = false;
				for (String c : Idynomics.simulator.getCompartmentNames())
				{
					if (c.contentEquals(origin))
						match = true;
				}
				if (!match)
				{
					if (Log.shouldWrite(Tier.CRITICAL))
						Log.out(Tier.CRITICAL, "Arrival process " + 
							this.getName() + "cannot receive agents from origin"
							+ " " + origin + ". No matching compartment name. "
							+ "The arrival process will only receive empty "
							+ "lists.");
				}
				
				this._originsChecked = true;
			}
		}
		
		//Populate arrivals list
		for (String k : _originNames)
		{
			//Note, if the origin named does not exist or has not sent any
			//departures in the last timestep, an empty list will be returned.
			this._arrivals.addAll(this._compartment.getArrivals(k));
		}
		
		//Process-specific arrival behaviour
		if (! this._arrivals.isEmpty())
		{
			this.agentsArrive(this._arrivals);
			
			this._arrivals.clear();
		}
	}
	
	/**
	 * A method that processes agents in the arrivals lounge.
	 * To be overwritten in implementing methods. 
	 * Agents in the _arrivals list are to be added to the AgentContainer here.
	 */
	protected abstract void agentsArrive(LinkedList<Agent> arrivals);

}
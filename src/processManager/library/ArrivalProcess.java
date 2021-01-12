package processManager.library;

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
import processManager.ProcessManager;
import referenceLibrary.XmlRef;

public abstract class ArrivalProcess extends ProcessManager {
	
	protected LinkedList<Agent> _arrivals;
	private Compartment _compartment;
	private InstantiableList<String> _originNames;
	
	
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
				this._originNames = (InstantiableList<String>) this.getValue(XmlRef.originNames);
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
		//Populate arrivals list
		for (String k : _originNames)
		{
			//Note, if the String k does not correspond to the name of a
			//compartment that has provided agents in the previous timestep
			//the getArrivals method will produce a warning message.
			this._arrivals.addAll(this._compartment.getArrivals(k));
		}
		
		//Process-specific arrival behaviour
		this.agentsArrive(this._arrivals);
		
		this._arrivals.clear();
	}
	
	/**
	 * A method that processes agents in the arrivals lounge.
	 * To be overwritten in implementing methods. 
	 * Agents in the _arrivals list are to be added to the AgentContainer here.
	 */
	public abstract void agentsArrive(LinkedList<Agent> arrivals);

}
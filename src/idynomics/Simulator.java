package idynomics;

import java.util.HashMap;

import agent.SpeciesLib;
import generalInterfaces.CanPrelaunchCheck;
import utility.*;


public class Simulator implements CanPrelaunchCheck
{
	
	protected HashMap<String, Compartment> _compartments = 
										   new HashMap<String, Compartment>();
	
	public SpeciesLib speciesLibrary = new SpeciesLib();
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Simulator()
	{
		//TODO fully implement MTRandom
		ExtraMath.initialiseRandomNumberGenerator();
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public Compartment addCompartment(String name, String shape)
	{
		if ( this._compartments.containsKey(name) )
			System.out.println("Warning: overwriting comaprtment "+name);
		Compartment aCompartment = new Compartment(shape);
		aCompartment.name = name;
		this._compartments.put(name, aCompartment);
		return aCompartment;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	public void step()
	{
		/*
		 * Loop through all compartments, calling their internal steps. 
		 */
		this._compartments.forEach((s,c) -> {c.step();});
		/*
		 * Once this is done loop through all again, this time exchanging
		 * cells that have tried to cross connected boundaries. 
		 */
		this._compartments.forEach((s,c) -> {c.pushAllOutboundAgents();});
		/*
		 * 
		 */
		Timer.step();
	}
	
	public void launch()
	{
		if ( ! isReadyForLaunch() )
		{
			System.out.println("Simulator not ready to launch!");
			return;
		}
		while ( Timer.isRunning() )
		{
			this.step();
		}
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printAll()
	{
		this._compartments.forEach((s,c) -> 
		{
			System.out.println("COMPARTMENT: "+s);
			c.printAllSoluteGrids();
		});
	}
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		/* Check the log file is initialised. */
		// TODO
		/* Check the random number generator is initialised. */
		if ( ExtraMath.random == null )
		{
			System.out.println("Random number generator not initialised!");
			return false;
		}
		/* Check we have at least one compartment. */
		if ( this._compartments.isEmpty() )
			return false;
		/* If any compartments are not ready, then stop. */
		for ( Compartment c : this._compartments.values() )
			if ( ! c.isReadyForLaunch() )
				return false;
		
		
		return true;
	}
}

package idynomics;

import java.util.HashMap;
import utility.*;


public class Simulator
{
	
	protected HashMap<String, Compartment> _compartments = 
										   new HashMap<String, Compartment>();
	
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
}

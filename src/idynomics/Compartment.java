package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import spatialgrid.SoluteGrid;
import mechanism.Mechanism;

public class Compartment
{
	
	protected String _name;
	
	/**
	 * Number of spatial dimensions in this compartment.
	 */
	protected int _nDims;
	
	/**
	 * 
	 */
	protected AgentContainer _agents;
	
	/**
	 * 
	 */
	protected SoluteGrid[] _soluteGrids; 
	
	/**
	 * 
	 */
	protected LinkedList<Mechanism> _mechanisms;
	
	protected MechanismComparator mechComp;
	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Compartment()
	{
		
	}
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * 
	 */
	public void step()
	{
		Mechanism currentMech = _mechanisms.getFirst();
		while ( currentMech.getTimeForNextStep() < Timer.getEndOfCurrentIteration() )
		{
			currentMech.step(_soluteGrids, _agents);
			Collections.sort(_mechanisms, mechComp);
			currentMech = _mechanisms.getFirst();
		}
	}
	
	protected static class MechanismComparator implements Comparator<Mechanism>
	{
		@Override
		public int compare(Mechanism mech1, Mechanism mech2) 
		{
			Double temp = mech1.getTimeForNextStep() -
												mech2.getTimeForNextStep();
			/*
			 * TODO Should deal with numerical rounding errors here, rather
			 * than just checking for zero. 
			 */
			if ( temp == 0.0 )
				return mech1.getPriority() - mech2.getPriority();
			else
				return temp.intValue();
		}
	}
	
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}

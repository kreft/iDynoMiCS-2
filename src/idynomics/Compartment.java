package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import processManager.ProcessManager;
import spatialgrid.SoluteGrid;

public class Compartment
{
	/**
	 * Unique name of this compartment.
	 */
	protected String _name;
	
	/**
	 * Number of spatial dimensions in this compartment.
	 */
	private int _nDims;
	
	/**
	 * AgentContainer deals with 
	 */
	protected AgentContainer _agents;
	
	/**
	 * 
	 */
	protected SoluteGrid[] _solutes; 
	
	/**
	 * 
	 */
	protected LinkedList<ProcessManager> _processes;
	
	protected ProcessComparator _procComp;
	
	
	protected Double _localTime;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Compartment()
	{
		_localTime = 0.0;
	}
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	protected int getNumDims()
	{
		return _nDims;
	}


	protected void setNumDims(int _nDims)
	{
		this._nDims = _nDims;
	}
	
	/**
	 * TODO
	 */
	protected void setSpeciesInfo()
	{
		
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * 
	 */
	public void step()
	{
		ProcessManager currentMech = _processes.getFirst();
		while ( currentMech.getTimeForNextStep() < Timer.getEndOfCurrentIteration() )
		{
			currentMech.step(_solutes, _agents);
			Collections.sort(_processes, _procComp);
			currentMech = _processes.getFirst();
		}
	}
	
	/**
	 * 
	 * 
	 */
	protected static class ProcessComparator implements Comparator<ProcessManager>
	{
		@Override
		public int compare(ProcessManager mech1, ProcessManager mech2) 
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

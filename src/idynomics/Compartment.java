package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import boundary.Boundary;
import boundary.BoundaryConnected;
import grid.SpatialGrid;
import processManager.ProcessManager;

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
	protected HashMap<String, SpatialGrid> _solutes; 
	
	/**
	 * 
	 */
	protected LinkedList<Boundary> _boundaries;
	
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
	
	public void addBoundary(Boundary aBoundary)
	{
		this._boundaries.add(aBoundary);
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	/**
	 * 
	 */
	public void step()
	{
		ProcessManager currentProcess = _processes.getFirst();
		while ( currentProcess.getTimeForNextStep() < 
											Timer.getEndOfCurrentIteration() )
		{
			_localTime = currentProcess.getTimeForNextStep();
			/*
			 * First process on the list does its thing. This should then
			 * increase its next step time.
			 */
			currentProcess.step(_solutes, _agents);
			/*
			 * Reinsert this process at the appropriate position in the list.
			 */
			Collections.sort(_processes, _procComp);
			/*
			 * Choose the new first process for the next iteration.
			 */
			currentProcess = _processes.getFirst();
		}
	}
	
	/**
	 * 
	 * 
	 */
	protected static class ProcessComparator 
										implements Comparator<ProcessManager>
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
	
	/**
	 * 
	 */
	public void pushAllOutboundAgents()
	{
		for ( Boundary b : this._boundaries )
			if ( b instanceof BoundaryConnected )
				((BoundaryConnected) b).pushAllOutboundAgents();
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}

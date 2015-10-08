package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import boundary.Boundary;
import boundary.BoundaryConnected;
import processManager.ProcessManager;

public class Compartment
{	
	/**
	 * N-dimensional vector describing the shape of this compartment. 
	 * 
	 * TODO Rob [8Oct2015]: This may need to be replaced with some sort of 
	 * shape object if we want to use non-rectangular compartments (e.g., 
	 * spherical). This is low priority for now.
	 */
	private double[] _sideLengths;
	
	/**
	 * AgentContainer deals with 
	 */
	protected AgentContainer _agents = new AgentContainer();
	
	/**
	 * 
	 */
	protected EnvironmentContainer _environment = new EnvironmentContainer();
	
	/**
	 * 
	 */
	protected LinkedList<Boundary> _boundaries = new LinkedList<Boundary>();
	
	/**
	 * 
	 */
	protected LinkedList<ProcessManager> _processes = 
											new LinkedList<ProcessManager>();
	
	/**
	 * 
	 */
	protected ProcessComparator _procComp;
	
	/**
	 * 
	 */
	protected Double _localTime = 0.0;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Compartment()
	{
		
	}
	

	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public int getNumDims()
	{
		return this._sideLengths.length;
	}
	
	public void setSideLengths(double[] sideLengths)
	{
		this._sideLengths = sideLengths;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aBoundary
	 */
	public void addBoundary(Boundary aBoundary)
	{
		this._boundaries.add(aBoundary);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aProcessManager
	 */
	public void addProcessManager(ProcessManager aProcessManager)
	{
		aProcessManager.showBoundaries(this._boundaries);
		this._processes.add(aProcessManager);
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
			currentProcess.step(this._environment, this._agents);
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

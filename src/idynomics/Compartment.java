package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import agent.Agent;
import boundary.Boundary;
import boundary.BoundaryConnected;
import generalInterfaces.CanPrelaunchCheck;
import grid.*;
import processManager.ProcessManager;
import shape.Shape;
import shape.BoundarySide;

public class Compartment implements CanPrelaunchCheck
{
	/**
	 * The Compartment is now aware of its own name.
	 * 
	 * TODO Rob [12Jan2016]: I'd rather it didn't, but this is low priority.
	 */
	public String name;
	
	/**
	 * TODO
	 */
	protected Shape _shape;
	
	/**
	 * AgentContainer deals with TODO
	 */
	public AgentContainer agents = new AgentContainer();
	
	/**
	 * TODO
	 */
	public EnvironmentContainer _environment;
	
	/**
	 * 
	 */
	protected LinkedList<ProcessManager> _processes = 
											new LinkedList<ProcessManager>();
	
	/**
	 * ProcessComparator orders Process Managers by their time priority.
	 */
	protected ProcessComparator _procComp = new ProcessComparator();
	
	/**
	 * 
	 */
	protected Double _localTime = 0.0;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Compartment(Shape aShape)
	{
		this._shape = aShape;
		this.setupShape();
	}
	
	public Compartment(String aShapeName)
	{
		try
		{
			// TODO check 
			this._shape = (Shape) Class.forName(aShapeName).newInstance();
		}
		catch ( Exception e )
		{
			// TODO
		}
		this.setupShape();
	}
	
	protected void setupShape()
	{
		this.agents.init( getNumDims() );
		this._environment = new EnvironmentContainer(this._shape.gridGetter());
	}
	
	public void init()
	{
		
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	
	public boolean isDimensionless()
	{
		return this._shape.getNumberOfDimensions() == 0;
	}
	
	public int getNumDims()
	{
		return this._shape.getNumberOfDimensions();
	}
	
	public void setSideLengths(double[] sideLengths)
	{
		this._shape.setSideLengths(sideLengths);
		this._environment.setSize(sideLengths, 1.0);
	}
	
	/**
	 * \brief TODO
	 * 
	 * <p>To add a side to a dimensionless compartment, it doesn't matter
	 * what "side" is.</p>
	 * 
	 * @param side
	 * @param aBoundary
	 */
	public void addBoundary(String side, Boundary aBoundary)
	{
		this.addBoundary(BoundarySide.getSideFor(side), aBoundary);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aBoundary
	 */
	public void addBoundary(BoundarySide aSide, Boundary aBoundary)
	{
		this._shape.addBoundary(aSide, aBoundary);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aProcessManager
	 */
	public void addProcessManager(ProcessManager aProcessManager)
	{
		aProcessManager.showBoundaries(this._shape.getOtherBoundaries());
		this._processes.add(aProcessManager);
	}
	
	/**
	 * 
	 * @param soluteName
	 */
	public void addSolute(String soluteName)
	{
		this._environment.addSolute(soluteName);
	}
	
	/**
	 * 
	 * @param Agent
	 */
	public void addAgent(Agent agent)
	{
		this.agents.addAgent(agent);
		agent.setCompartment(this);
	}
	
	public SpatialGrid getSolute(String soluteName)
	{
		return this._environment.getSoluteGrid(soluteName);
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
			currentProcess.step(this._environment, this.agents);
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
		public int compare(ProcessManager manager1, ProcessManager manager2) 
		{
			Double temp = manager1.getTimeForNextStep() -
												manager2.getTimeForNextStep();
			/*
			 * TODO Should deal with numerical rounding errors here, rather
			 * than just checking for zero. 
			 */
			if ( temp == 0.0 )
				return manager1.getPriority() - manager2.getPriority();
			else
				return temp.intValue();
		}
	}
	
	/**
	 * 
	 */
	public void pushAllOutboundAgents()
	{
		// TODO Rob [12Jan2016]: I broke my own rule about using instanceof...
		// need to check if it's justified here.
		for ( Boundary b : this._shape.getSideBoundaries() )
			if ( b instanceof BoundaryConnected )
				((BoundaryConnected) b).pushAllOutboundAgents();
	}
	
	/*************************************************************************
	 * PRE-LAUNCH CHECK
	 ************************************************************************/
	
	public boolean isReadyForLaunch()
	{
		if ( this._shape == null )
		{
			System.out.println("Compartment shape is undefined!");
			return false;
		}
		
		return true;
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void printSoluteGrid(String soluteName)
	{
		this._environment.printSolute(soluteName);
	}
	
	public void printAllSoluteGrids()
	{
		this._environment.printAllSolutes();
	}
}

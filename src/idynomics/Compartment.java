package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import agent.Agent;
import boundary.Boundary;
import boundary.BoundaryConnected;
import grid.CartesianGrid;
import grid.SpatialGrid;
import grid.SpatialGrid.GridGetter;
import processManager.ProcessManager;

public class Compartment
{
	public static enum CompartmentShape
	{
		/*
		 * A compartment without spatial structure, e.g. a chemostat.
		 */
		DIMENSIONLESS(0),
		
		LINE(1),
		
		RECTANGLE(2),
		
		CUBOID(3),
		
		UNKNOWN(-1);
		
		private int nDim;
		
		private CompartmentShape(int nDim)
		{
			this.nDim = nDim;
		}
		
		public static CompartmentShape getShapeFor(String shape)
		{
			if ( shape.equalsIgnoreCase("dimensionless") )
				return DIMENSIONLESS;
			else if ( shape.equalsIgnoreCase("line") )
				return LINE;
			else if ( shape.equalsIgnoreCase("rectangle") )
				return RECTANGLE;
			else if ( shape.equalsIgnoreCase("cuboid") )
				return CUBOID;
			else
				return UNKNOWN;
		};
		
		public static HashMap<BoundarySide,Boundary> 
									sideBoundariesFor(CompartmentShape aShape)
		{
			if ( aShape == DIMENSIONLESS || aShape == UNKNOWN )
				return null;
			HashMap<BoundarySide,Boundary> out = new 
											HashMap<BoundarySide,Boundary>();
			if ( aShape == LINE || aShape == RECTANGLE || aShape == CUBOID)
			{
				out.put(BoundarySide.XMIN, null);
				out.put(BoundarySide.XMAX, null);
			}
			if ( aShape == RECTANGLE || aShape == CUBOID)
			{
				out.put(BoundarySide.YMIN, null);
				out.put(BoundarySide.YMAX, null);
			}
			if ( aShape == CUBOID)
			{
				out.put(BoundarySide.ZMIN, null);
				out.put(BoundarySide.ZMAX, null);
			}
			return out;
		}
		
		public static GridGetter gridFor(CompartmentShape aShape)
		{
			if ( aShape == LINE || aShape == RECTANGLE || aShape == CUBOID)
				return CartesianGrid.standardGetter();
			//TODO Safety
			return null;
		}
	}
	
	public static enum BoundarySide
	{
		/*
		 * Cartesian boundaries.
		 */
		XMIN, XMAX, YMIN, YMAX, ZMIN, ZMAX,
		/*
		 * Polar/cylindrical boundaries
		 */
		CIRCUMFERENCE,
		/*
		 * 
		 */
		INTERNAL,
		/*
		 * 
		 */
		UNKNOWN;
		
		public static BoundarySide getSideFor(String side)
		{
			if ( side.equalsIgnoreCase("xmin") )
				return XMIN;
			else if ( side.equalsIgnoreCase("xmax") )
				return XMAX;
			else if ( side.equalsIgnoreCase("ymin") )
				return YMIN;
			else if ( side.equalsIgnoreCase("ymax") )
				return YMAX;
			else if ( side.equalsIgnoreCase("zmin") )
				return ZMIN;
			else if ( side.equalsIgnoreCase("zmax") )
				return ZMAX;
			else if ( side.equalsIgnoreCase("internal") )
				return INTERNAL;
			else
				return UNKNOWN;
		}
	};
	
	protected CompartmentShape _shape;
	
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
	public AgentContainer agents = new AgentContainer();
	
	/**
	 * The Compartment is now aware of it's own name
	 */
	public String name;
	
	/**
	 * 
	 */
	public EnvironmentContainer _environment;
	
	/**
	 * Directory of boundaries that are linked to a specific side.
	 */
	protected HashMap<BoundarySide,Boundary> _sideBoundaries;
	
	/**
	 * List of boundaries in a dimensionless compartment, or internal
	 * boundaries in a dimensional compartment.
	 */
	protected LinkedList<Boundary> _otherBoundaries = 
												new LinkedList<Boundary>();
	
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
	
	public Compartment(CompartmentShape aShape)
	{
		this._shape = aShape;
		this.setupShape();
	}
	
	public Compartment(String aShapeName)
	{
		this._shape = CompartmentShape.getShapeFor(aShapeName);
		this.setupShape();
	}
	
	protected void setupShape()
	{
		if ( this._shape == CompartmentShape.UNKNOWN )
		{
			//TODO
		}
		this._otherBoundaries = new LinkedList<Boundary>();
		this._sideBoundaries = CompartmentShape.sideBoundariesFor(this._shape);
		this.agents.init(getNumDims());
		this._environment = new 
				  EnvironmentContainer(CompartmentShape.gridFor(this._shape));
	}
	
	public void init()
	{
		if ( this._sideLengths == null )
		{
			// TODO
			System.out.println("Warning! Compartment side lengths not set.");
			return;
		}
		for ( String soluteName : this._environment.getSoluteNames() )
		{
			this._sideBoundaries.forEach( (side, boundary) ->
			{
				this._environment.addBoundary(side, soluteName, 
										boundary.getGridMethod(soluteName));
			});
		}
		
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	
	public boolean isDimensionless()
	{
		return this._shape == CompartmentShape.DIMENSIONLESS;
	}
	
	public int getNumDims()
	{
		return this._shape.nDim;
	}
	
	public void setSideLengths(double[] sideLengths)
	{
		this._sideLengths = sideLengths;
		this._environment.setSize(this._sideLengths, 1.0);
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
	public void addBoundary(BoundarySide side, Boundary aBoundary)
	{
		if ( this.isDimensionless() || side == BoundarySide.INTERNAL )
			this._otherBoundaries.add(aBoundary);
		else
		{
			this._sideBoundaries.put(side, aBoundary);
			for ( String soluteName : this._environment.getSoluteNames() )
			{
				this._environment.addBoundary(side, soluteName,
										aBoundary.getGridMethod(soluteName));
			}
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aProcessManager
	 */
	public void addProcessManager(ProcessManager aProcessManager)
	{
		aProcessManager.showBoundaries(this._sideBoundaries.values());
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
		for ( Boundary b : this._sideBoundaries.values() )
			if ( b instanceof BoundaryConnected )
				((BoundaryConnected) b).pushAllOutboundAgents();
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

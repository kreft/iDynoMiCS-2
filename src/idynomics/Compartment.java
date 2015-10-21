package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import boundary.Boundary;
import boundary.BoundaryConnected;
import processManager.ProcessManager;

public class Compartment
{
	public static enum CompartmentShape
	{
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
	}
	
	public enum BoundarySide
	{
		/*
		 * Cartesian boundaries.
		 */
		XMIN, XMAX, YMIN, YMAX, ZMIN, ZMAX,
		/*
		 * TODO Polar/cylindrical boundaries
		 */
		
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
	protected AgentContainer _agents = new AgentContainer();
	
	/**
	 * 
	 */
	protected EnvironmentContainer _environment = new EnvironmentContainer();
	
	/**
	 * Directory of boundaries that are linked to a specific side.
	 */
	protected HashMap<BoundarySide,Boundary> _sideBoundaries;
	
	/**
	 * List of boundaries in a dimensionless compartment, or internal
	 * boundaries in a dimensional compartment.
	 */
	protected LinkedList<Boundary> _otherBoundaries;
	
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
	
	public void setShape(String shape)
	{
		this._shape = CompartmentShape.getShapeFor(shape);
		if ( this._shape == CompartmentShape.UNKNOWN )
		{
			//TODO
		}
		this._otherBoundaries = new LinkedList<Boundary>();
		this._sideBoundaries = CompartmentShape.sideBoundariesFor(this._shape);
	}
	
	public boolean isDimensionless()
	{
		return this._shape == CompartmentShape.DIMENSIONLESS;
	}
	
	public int getNumDims()
	{
		return this._shape.nDim;
		//return this._sideLengths.length;
	}
	
	public void setSideLengths(double[] sideLengths)
	{
		this._sideLengths = sideLengths;
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
			this._sideBoundaries.put(side, aBoundary);
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
		for ( Boundary b : this._sideBoundaries.values() )
			if ( b instanceof BoundaryConnected )
				((BoundaryConnected) b).pushAllOutboundAgents();
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
}

package idynomics;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agent.Agent;
import boundary.Boundary;
import boundary.BoundaryConnected;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.tier;
import generalInterfaces.CanPrelaunchCheck;
import grid.*;
import processManager.ProcessManager;
import shape.Shape;
import shape.ShapeConventions.DimName;
import utility.Helper;

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
	 * 
	 */
	public AgentContainer agents;
	
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
	
	public Compartment()
	{
		
	}
	
	public Compartment(Shape aShape)
	{
		this.setShape(aShape);
	}
	
	public Compartment(String aShapeName)
	{
		this((Shape) Shape.getNewInstance(aShapeName));
	}
	
	/**
	 * \brief
	 * 
	 * TODO This should go back to being private once tests are based on XML
	 * protocols.
	 * 
	 * @param aShape
	 */
	public void setShape(Shape aShape)
	{
		this._shape = aShape;
		this._environment = new EnvironmentContainer(this._shape);
		this.agents = new AgentContainer(this._shape);
	}
	
	/**
	 * \brief Initialise this {@code Compartment} from an XML node. 
	 * 
	 * @param xmlNode An XML node from a protocol file.
	 */
	public void init(Node xmlNode)
	{
		Element elem = (Element) xmlNode;
		String str;
		NodeList children;
		Element child;
		/* 
		 * First, make the shape this Compartment will take.
		 */
		children = XmlHandler.getAll(elem, "shape");
		if ( children.getLength() != 1 )
		{
			Log.out(tier.CRITICAL, "Warning: Compartment must have one shape!");
			return;
		}
		child = (Element) children.item(0);
		str = child.getAttribute("class");
		str = Helper.obtainInput(str, "compartment shape class");
		this.setShape((Shape) Shape.getNewInstance(str));
		this._shape.init(child);
		/*
		 * Give it solutes.
		 */
		children = XmlHandler.getAll(elem, "solute");
		for ( int i = 0; i < children.getLength(); i++ )
		{
			child = (Element) children.item(i);
			str = XmlHandler.loadUniqueAtribute(child, "name", "string");
			str = Helper.obtainInput(str, "solute name");
			this.addSolute(str);
			// TODO diffusivity
			// TODO initial value
		}
		/*
		 * Finally, finish off the initialisation as standard.
		 */
		this.init();
	}
	
	public void init()
	{
		/*
		 * NOTE: Bas [06.02.16] this may be set elsewhere as long as it is after
		 * the Dimensions and sideLengths are set.
		 * 
		 * NOTE: Rob [8Feb2016] here is fine (_environment also needs
		 * sideLengths, etc).
		 */
		this._shape.setSurfaces();
		
		this._environment.init();
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public Shape getShape()
	{
		return this._shape;
	}
	
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
		this._shape.setDimensionLengths(sideLengths);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param dim
	 * @param index
	 * @param aBoundary
	 */
	public void addBoundary(DimName dim, int index, Boundary aBoundary)
	{
		this._shape.setBoundary(dim, index, aBoundary);
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
	 * TODO
	 */
	public void pushAllOutboundAgents()
	{
		for ( BoundaryConnected b : this._shape.getConnectedBoundaries() )
			b.pushAllOutboundAgents();
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
		if ( ! this._shape.isReadyForLaunch() )
			return false;
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

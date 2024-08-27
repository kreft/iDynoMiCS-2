package processManager.library;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import boundary.Boundary;
import boundary.SpatialBoundary;
import boundary.spatialLibrary.BiofilmBoundaryLayer;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import processManager.ProcessArrival;
import referenceLibrary.AspectRef;
import shape.Dimension.DimName;
import surface.Point;
import shape.Shape;

public class AgentAttachmentRandomWalk extends ProcessArrival {

	private String DIMENSION= AspectRef.dimensionName;
	
	private String EXTREME = AspectRef.dimensionExtreme;
	
	private String STEP_SIZE = AspectRef.stepSize;
	
	private String BL_THICKNESS = AspectRef.boundaryLayerThickness;
	
	public String BODY = AspectRef.agentBody;
	
	/**
	 * Agents enter at one extreme of a dimension: this is the name of that
	 * dimension.
	 */
	protected DimName _dim;
	
	/**
	 * Agents enter at one extreme of a dimension: this is the index of
	 * that extreme (0 for minimum, 1 for maximum).
	 */
	protected int _extreme;
	
	/**
	 * The shape belonging to the compartment running this process
	 */
	protected Shape _shape;
	
	protected double _stepSize;
	
	/**
	 * Biofilm boundary layer thickness. This can be given as an aspect of the
	 * process or can be fetched from the biofilm boundary if it exists
	 */
	protected double _layerThickness;
	
	/**
	 * Number of dimensions in the shape
	 */
	protected int _numDims;
	
	public static String CURRENT_PULL_DISTANCE = 
			AspectRef.collisionCurrentPullDistance;
	
	/**
	 * Pull distance - the distance to neighbouring agents at which the
	 * arriving agent is considered to have attached to the biofilm
	 */
	private double _pull;
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
		String dimName = this.getString(DIMENSION);
		
		switch (dimName)
		{
		case "X":
			this._dim = DimName.X;
			break;
		
		case "Y":
			this._dim = DimName.Y;
			break;
			
		case "Z":
			this._dim = DimName.Z;
			break;
			
		case "THETA":
			this._dim = DimName.THETA;
			break;
			
		case "PHI":
			this._dim = DimName.PHI;
			break;
		}
		
		this._extreme = this.getInt(EXTREME);
		
		this._shape = this._compartment.getShape();
		
		this._numDims = this._shape.getNumberOfDimensions();
		
		this._stepSize = this.getDouble(STEP_SIZE);
		
		if (this._extreme == 1)
			this._stepSize = - this._stepSize;
		
		if (this.isLocalAspect(BL_THICKNESS))
		{
			this._layerThickness = this.getDouble(BL_THICKNESS);
		}
		
		else
		{
			for (Boundary b : this._shape.getAllBoundaries())
			{
				if (b instanceof BiofilmBoundaryLayer)
				{
					this.reg().add(BL_THICKNESS, 
						((BiofilmBoundaryLayer) b).getLayerThickness());
					this._layerThickness = ((BiofilmBoundaryLayer) b).
						getLayerThickness();
				}
			}
		}
		
		if (!this.isAspect(BL_THICKNESS))
		{
			if (Log.shouldWrite(Tier.NORMAL))
				Log.out(Tier.NORMAL, "No boundary layer thickness specified. "
						+ "Setting to 0.0.");
			this._layerThickness = 0.0;
			this.reg().add(BL_THICKNESS, 0.0);
		}
		
	}
	
	@Override
	protected void agentsArrive(LinkedList<Agent> arrivals) 
	{
		for (Agent agent : arrivals)
		{
			/*
			 * Check to see if the compartment is empty. If so, simply place
			 * the agent at a random location at the extreme opposite to where
			 * agents arrive.
			 */
			
			Body agentBody = (Body) agent.get(BODY);
			List<Point> points = agentBody.getPoints();
			
			if (this._agents.getAllLocatedAgents().isEmpty())
			{
				
				double[] newLocation;
				
				int dimIndex = this._shape.getDimensionIndex(this._dim);
				
				switch (this._extreme)
				{
				
				/*
				 * case 1 - The agents enter at the "top" of the domain.
				 * Agents to be placed at the bottom of the domain.
				 */
				case 1:
				{
					//Find the agent's lowest point in this dimension
					Point lowestPoint = points.get(0);
					
					for (Point p : points)
					{
						if (p.getPosition()[dimIndex] < 
								lowestPoint.getPosition()[dimIndex] )
						{
							lowestPoint = p;
						}
					}
					
					newLocation = this._shape.
							getRandomLocationOnBoundary(
							this._dim, 0);
					
					/*
					 * This is the vector that needs to be ADDED to the lowest
					 * point to get the new position
					 */
					double[] difference = new double[newLocation.length];
					
					Vector.minusTo(
							difference, newLocation, lowestPoint.getPosition());
					
					for (Point p : points)
					{
						double[] newPosition = new double[difference.length];
						
						Vector.addTo(newPosition, p.getPosition(), difference);
						
						p.setPosition(newPosition);
					}
					
					break;
				}
				
				/*
				 * Case 0 - Agents enter at the "bottom" of the domain
				 * and are placed at the "top"
				 */
				case 0:
				{
					//Find the agent's highest point in the dimension
					Point highestPoint = points.get(0);
					
					for (Point p : points)
					{
						if (p.getPosition()[dimIndex] > 
								highestPoint.getPosition()[dimIndex] )
						{
							highestPoint = p;
						}
					}
					
					newLocation = this._shape.
							getRandomLocationOnBoundary(
							this._dim, 1);
					
					/*
					 * This is the vector that needs to be ADDED to the highest
					 * point to get the new position
					 */
					double[] difference = new double[newLocation.length];
					
					Vector.minusTo(
							difference, newLocation, highestPoint.getPosition());
					
					for (Point p : points)
					{
						double[] newPosition = new double[difference.length];
						
						Vector.addTo(newPosition, p.getPosition(), difference);
						
						p.setPosition(newPosition);
					}
					
					break;
				}
				
				}
			}
			
			//If the compartment is not empty...
			else
			{
				if (agent.isAspect(CURRENT_PULL_DISTANCE))
					this._pull = agent.getDouble(CURRENT_PULL_DISTANCE);
				
				//If no pull distance is specified, the random walk step size is
				//used
				else
					this._pull = Math.abs(this._stepSize);
				
				this.relocateAgent(agent, points);
			}
		}
		for (Agent agent : arrivals)
			this._agents.addAgent( agent );
	}
	
	
	private void relocateAgent(Agent agent, List<Point> points)
	{
		//Find which dimension the agents will be "falling" through
		//from the partner compartment towards the biofilm
		int dimIndex = this._shape.getDimensionIndex(this._dim);
		
		attachmentLoop: while (true)
		{
			//Get a random location at the extreme opposite the biofilm
			double[] newLocation = this._shape.getRandomLocationOnBoundary(
					this._dim, this._extreme);
			
			/*
			 * Of the agent's current points, find the value of the one closest
			 * to the new location.
			 */
			double closestValue = points.get(0).getPosition()[dimIndex];
			
			//The index of the point closest to the new location
			int closestPoint = 0;
			
			for (int i = 0; i < points.size(); i++)
			{
				if (points.get(i).getPosition()[dimIndex] > closestValue)
				{
					closestValue = points.get(i).getPosition()[dimIndex];
					closestPoint = i;
				}
			}
			
			//The distance between the new location and the closest point.
			double[] difference = new double[this._numDims];
			
			Vector.minusTo(difference, newLocation, 
					points.get(closestPoint).getPosition());
			
			
			for (Point p : points)
			{
				double[] newPosition = new double[this._numDims];
				double[] currentPosition = p.getPosition();
				
				//Add the previously calculated differrence to each of the
				//agent's points, so that the agent now resides at the new
				//location and if it has multiple points, it retains its
				//shape.
				Vector.addTo(newPosition, currentPosition, difference);
				p.setPosition(newPosition);
			}
			
			if (this.attemptToReachBoundaryLayer(agent, points))
			{
				this.randomWalkAttachment(
						agent, (LinkedList<Point>) points);
				break attachmentLoop;
			}
		}
		
		
	}
	
	/**
	 * Attempt the "drop" the agent towards the biofilm boundary layer
	 * @param agent
	 * @param points
	 * @return
	 */
	private boolean attemptToReachBoundaryLayer(Agent agent, List<Point> points)
	{
		while (true)
		{
			this._agents.moveAlongDimension(agent, this._dim, 
					this._stepSize);
			
			if (this.isInsideCompartment(points))
			{
				
				Collection<SpatialBoundary> boundaries;
				
				boundaries = this._agents.boundarySearch(agent, this._pull);
				
				if (!boundaries.isEmpty())
				{
					for (SpatialBoundary b : boundaries)
					{
						if (b.isSolid())
						{
							//If agent has hit a solid boundary, continue to 
							//random walk and insertion
							return true;
						}
					}
				}
				
				if ( this.hasNeighbours(agent, this._layerThickness) )
				{
					return true;
				}
			}
			
			/**
			 * This can trigger if agent is above the domain in the
			 * dimension it is "falling" through after just one
			 * moveAlongDimension. Rework, or find way to ignore a
			 * dimension.
			 * 
			 */
			else
				return false;
		}
	}
	
	/**
	 * Attempt to find an attachment site within the pull distance by random
	 * walk.
	 * @param agent
	 * @param points
	 */
	private void randomWalkAttachment(
			Agent agent, LinkedList<Point> points)
	{
		Collection<SpatialBoundary> boundaries;
		
		Body agentBody = (Body) agent.get(BODY);
		
		LinkedList<Point> oldPoints = this.copyPoints(points);
		
		attachmentLoop: while (true)
		{
			this.randomStep(agent, points);
			
			/*
			 * Apply boundaries to the agent. This ensures that any cyclic
			 * boundaries are taken into account and the agent remains within
			 * the compartment.
			 */
			for (Point p : points)
				p.setPosition(this._shape.applyBoundaries(
						p.getPosition() ));
			
			/*
			 * Look for boundaries
			 */
			boundaries = this._agents.boundarySearch(agent, this._pull);
			
			if (!boundaries.isEmpty())
			{
				for (Boundary b : boundaries)
				{
					if (((SpatialBoundary) b).isSolid())
					{
						//If agent has hit a solid spatial boundary, add it to
						//the compartment.
						break attachmentLoop;
					}
				}
			}
			
			/*
			 * If the agent has moved out of the compartment, restart the
			 * relocation.
			 */
			if (!this.isInsideCompartment(points))
			{
				agentBody.setPoints(oldPoints);
				continue attachmentLoop;
			}
			
			
			//If agent strays more than the boundary layer thickness
			//away from other agents, restart the relocation.
			if (!this.hasNeighbours(agent, this._layerThickness))
			{
				agentBody.setPoints(oldPoints);
				continue attachmentLoop;
			}
			
			/*
			 * If this agent has neighbours within the pull distance, add it 
			 * to the agent container.
			 */
			Collection<Agent> neighbours = 
					this._agents.treeSearch(agent, this._pull);
			if (neighbours.size() > 1)
			{
				break attachmentLoop;
			}
			
			oldPoints.clear();
			
			oldPoints = this.copyPoints(points);
		}
	}
	
	/**
	 * Check if these points are inside the compartment.
	 * @param points
	 * @return
	 */
	private boolean isInsideCompartment(List<Point> points)
	{
		for (Point p : points)
		{
			if (!this._shape.isInside(p.getPosition()))
				return false;
		}
		
		return true;
	}
	
	/**
	 * Check whether this agent has neighbours
	 * @param agent
	 * @param searchDist
	 * @return
	 */
	private boolean hasNeighbours(Agent agent, double searchDist)
	{
		Collection<Agent> neighbours = 
				this._agents.treeSearch(agent, searchDist);
		return (!neighbours.isEmpty());
	}
	
	/**
	 * Return a list of points that is a deep copy of the argument.
	 * @param points
	 * @return
	 */
	private LinkedList<Point> copyPoints(List<Point> points)
	{
		LinkedList<Point> out = new LinkedList<Point>();
		
		for (int i = 0; i < points.size(); i++)
		{
			Point outPoint = new Point(points.get(i).getPosition());
			out.add(outPoint);
		}
		
		return out;
	}
	
	/**
	 * Move an agent in a random direction by a distance equal to the
	 * _stepSize.
	 * @param agent
	 * @param points
	 */
	private void randomStep (Agent agent, List<Point> points)
	{
		double[] vector = new double[this._numDims];
		
		double[] move = Vector.randomPlusMinus(vector);
		
		Vector.normaliseEuclidEquals(move);
		
		Vector.timesEquals(move, this._stepSize);
		
		for (Point p : points)
		{
			p.setPosition( Vector.add(p.getPosition(), move) );
		}
	}
	
}

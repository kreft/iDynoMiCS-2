package processManager;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import agent.Agent;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.subgrid.SubgridPoint;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import reaction.Reaction;
import surface.Ball;
import surface.Collision;
import surface.Surface;
import utility.Copier;

public class ConstructProductRateGrids extends ProcessManager
{
	
	String[] _prodGrid;
	
	public void init()
	{
		this._prodGrid = getStringA("ProductionGrids");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void internalStep(EnvironmentContainer environment, AgentContainer agents) {
		
		/* Collision evaluation object */
		Collision collisionEval = new Collision(null, agents.getShape());
		/**
		 * First clear them agent vol distribs
		 */
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			HashMap<int[],Double> distributionMap = new HashMap<int[],Double>();
			a.set("volumeDistribution", distributionMap);
		}
		
		/*
		 * Iterate through all solute grids to update their productionRate
		 * arrays.
		 */
		SpatialGrid solute;
		Predicate<Agent> noReacFilter = hasNoReactions();
		List<SubgridPoint> sgPoints;
		for ( String grid : this._prodGrid )
		{
			//TODO make a general grid getter method?
			solute = environment.getSoluteGrid(grid);
			/*
			 * Reset the domain array.
			 */
			solute.newArray(ArrayType.PRODUCTIONRATE);
			/*
			 * Iterate over all voxels, checking if there are agents nearby.
			 */
			int[] coord = solute.resetIterator();
			double[] origin;
			double[] dimension = new double[3];
			List<Agent> neighbors;
			HashMap<int[],Double> distributionMap;
			while ( solute.isIteratorValid() )
			{
				/* Find all agents that overlap with this voxel. */
				origin = solute.getVoxelOrigin(coord);
				solute.getVoxelSideLengthsTo(dimension, coord);
				/* NOTE the agent tree is always the amount of actual dimension */
				neighbors = agents._agentTree.cyclicsearch(
							  Vector.subset(origin,agents.getNumDims()),
							  Vector.subset(dimension,agents.getNumDims()));
				/* If there are none, move onto the next voxel. */
				if ( neighbors.isEmpty() )
				{
					coord = solute.iteratorNext();
					continue;
				}
				/* Filter the agents for those with reactions. */
				neighbors.removeIf(noReacFilter);
				/* 
				 * Find the sub-grid resolution from the smallest agent, and
				 * get the list of sub-grid points.
				 */
				// TODO This is a quick-fix... job for Bas
				double minRadius = Double.MAX_VALUE;
				for(Agent a : agents.getAllLocatedAgents())
				{
					if(a.reg().isGlobalAspect(NameRef.agentReactions) && 
							a.getDouble(NameRef.bodyRadius) < minRadius)
						minRadius = a.getDouble(NameRef.bodyRadius);		
				}
				double subRes = Vector.min(dimension) * 0.25 * minRadius;
				sgPoints = solute.getCurrentSubgridPoints(subRes);
				/* 
				 * Get the subgrid points and query the agents.
				 */
				for ( Agent a : neighbors )
				{
					List<Surface> surfaces = 
									(List<Surface>) a.get(NameRef.surfaceList);
					distributionMap = (HashMap<int[],Double>) 
											a.getValue("volumeDistribution");
					
					sgLoop: for ( SubgridPoint p : sgPoints )
					{
						/* NOTE only give coords in actual dimensions */
						Ball b = new Ball(Vector.subset(p.realLocation,agents.getNumDims()), 0.0);
						b.init(collisionEval);
						for( Surface s : surfaces )
							if ( b.distanceTo(s) < 0.0 )
							{
								/*
								 * If this is not the first time the agent has
								 * seen this coordinate, we need to add the
								 * volume rather than overwriting it.
								 */
								double newVolume = p.volume;
								if ( distributionMap.containsKey(coord) )
									newVolume += distributionMap.get(coord);
								distributionMap.put((int[]) Copier.copy(coord), newVolume);
								// NOTE copy since otherwise you update the in the hahsmap to when iteratorNext()!
								/*
								 * We only want to count this point once, even
								 * if other surfaces of the same agent hit it.
								 */
								continue sgLoop;
							}
					}
				}
				coord = solute.iteratorNext();
			}
		}
		/*
		 * Now loop over all agents, applying their reactions to the relevant
		 * solute grids, in the voxels calculated before.
		 */
		HashMap<String,Double> concentrations = new HashMap<String,Double>();
		SpatialGrid aSG;
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			List<Reaction> reactions = (List<Reaction>) a.get("reactions");
			HashMap<int[],Double> distributionMap = 
					(HashMap<int[],Double>) a.getValue("volumeDistribution");
			/*
			 * Calculate the total volume covered by this agent, according to
			 * the distribution map. This is likely to be slightly different to
			 * the agent volume calculated directly.
			 */
			double totalVoxVol = 0.0;
			for ( double voxVol : distributionMap.values() )
				totalVoxVol += voxVol;
			
			for ( int[] coord : distributionMap.keySet() )
			{
				for ( Reaction r : reactions )
				{
					/* 
					 * Build the dictionary of variable values. Note that these
					 * will likely overlap with the names in the reaction
					 * stoichiometry (handled after the reaction rate), but 
					 * will not always be the same. Here we are interested in
					 * those that affect the reaction, and not those that are
					 * affected by it.
					 */
					for ( String varName : r.variableNames )
					{
						if ( environment.isSoluteName(varName) )
						{
							aSG = environment.getSoluteGrid(varName);
							concentrations.put(varName, 
									aSG.getValueAt(ArrayType.CONCN, coord));
							// FIXME: was getting strange [16,0,0] coord values
							// here (index out of bounds)
						}
						else if ( a.checkAspect(varName) )
						{
							// TODO divide by the voxel volume here?
							concentrations.put(varName, 
									a.getDouble(varName) * 
									(distributionMap.get(coord)/totalVoxVol));
						}
						else
						{
							// TODO safety?
							concentrations.put(varName, 0.0);
						}
					}
					/*
					 * Calculate the reaction rate based on the variables just
					 * retrieved.
					 */
					double rate = r.getRate(concentrations);
					/* 
					 * Now that we have the reaction rate, we can distribute
					 * the effects of the reaction. Note again that the names
					 * in the stoichiometry may not be the same as those in the
					 * reaction variables (although there is likely to be a
					 * large overlap).
					 */
					double productionRate;
					for ( String productName : r.getStoichiometry().keySet())
					{
						productionRate = rate * r.getStoichiometry(productName);
						if ( environment.isSoluteName(productName) )
						{
							aSG = environment.getSoluteGrid(productName);
							aSG.addValueAt(ArrayType.PRODUCTIONRATE, coord, 
															productionRate);
						}
						else if ( a.checkAspect(productName) )
						{
							/* 
							 * NOTE Bas [17Feb2016]: Put this here as example,
							 * though it may be nicer to launch a separate 
							 * agent growth process manager here.
							 */
							/* 
							 * NOTE Bas [17Feb2016]: The average growth rate
							 * for the entire agent, not just for the part that
							 * is in one grid cell later this may be specific
							 * separate expressions that control the growth of
							 * separate parts of the agent (eg lipids/ other
							 * storage compounds)
							 */
							productionRate += (double) a.getDouble("growthRate");
							a.set("growthRate", productionRate);
							
							/* Timespan of growth event */
							// TODO Rob[18Feb2016]: Surely this should happen
							// at the very end? 
							double dt = this._timeStepSize;
							a.event("growth", dt * productionRate);
							a.event("divide", _timeStepSize);
						}
						else
						{
							// TODO safety?
						}
					}
				}
			}
		}
	}
	
	/**
	 * \brief Helper method for filtering local agent lists, so that they only
	 * include those that have reactions.
	 */
	private static Predicate<Agent> hasNoReactions()
	{
		return a -> ! a.aspectRegistry.isGlobalAspect("reactions");
	}
}
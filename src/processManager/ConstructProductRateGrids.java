package processManager;

import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import com.sun.j3d.utils.geometry.Sphere;

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
				neighbors = agents._agentTree.cyclicsearch(origin, dimension);
				/* If there are none, move onto the next voxel. */
				if ( neighbors.isEmpty() )
					continue;
				/* Filter the agents for those with reactions. */
				neighbors.removeIf(noReacFilter);
				/* 
				 * Find the sub-grid resolution from the smallest agent, and
				 * get the list of sub-grid points.
				 */
				// TODO This is a quick-fix... job for Bas
				double subRes = Vector.min(dimension) * 0.25;
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
						Ball b = new Ball(p.realLocation,0.0);
						for( Surface s : surfaces )
						{
							if ( s.distanceTo(b) < 0.0 )
							{
								
								double newVolume = p.volume;
								if ( distributionMap.containsKey(coord) )
									newVolume += distributionMap.get(coord);
								distributionMap.put(coord, newVolume);
								/*
								 * We only want to count this point once, even
								 * if other surfaces of the same agent hit it.
								 */
								continue sgLoop;
							}
						}
					}
				}
				coord = solute.iteratorNext();
			}
		}
		
		
		for ( Agent a : agents.getAllLocatedAgents() )
		{
			List<Reaction> reactions = (List<Reaction>) a.get("reactions");
			
			HashMap<String,Double> productRateMap = new HashMap<String,Double>();
			for(Reaction r : reactions)
			{
				double reactionRate = r.getRate(agentSpecificConcentrationHashmap??);
				if(productRateMap.containsKey(r.getStoichiometry(reactant)));
				{
					productRateMap.put(recatant, productRateMap.get(reactant) + r.getStoichiometry(reactant) * reactionRate);
				}
			}
			
			for(String key : productRateMap.keySet())
			{
				SpatialGrid aGrid = environment.getSoluteGrid(productionGridFromKey?);
				HashMap<int[],Double> distributionMap = 
						(HashMap<int[],Double>) a.getValue("volumeDistribution"); // Bas I will make a secondary state that calculates the exact agent mass for each int[]
				for(int[] coord : distributionMap.keySet())
					aGrid.addValueAt(ArrayType.PRODUCTIONRATE, coord, distributionMap.get(coord) * productRateMap.get(key));
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

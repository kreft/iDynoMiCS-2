package processManager;

import java.util.HashMap;
import java.util.List;

import com.sun.j3d.utils.geometry.Sphere;

import agent.Agent;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import reaction.Reaction;
import surface.Ball;
import surface.Collision;
import surface.Surface;

public class ConstructProductRateGrids extends ProcessManager {
	
	String[] _prodGrid;
	
	public void init()
	{
		this._prodGrid = getStringA("ProductionGrids");
	}

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
		
		/**
		 * iterate trough all porductionRate grids
		 */
		for(String grid : _prodGrid )
		{
			SpatialGrid aGrid = environment.getSoluteGrid(grid); //TODO make a general grid getter method?
			/*
			 * Reset the domain array.
			 */
			aGrid.newArray(ArrayType.PRODUCTIONRATE);
			/*
			 * Iterate over all voxels, checking if there are agents nearby.
			 */
			int[] coords = aGrid.resetIterator();
			List<Agent> neighbors;
			while ( aGrid.isIteratorValid() )
			{
				 /**
				 * find all closeby agents
				 */
				neighbors = agents._agentTree.cyclicsearch(aGrid.getBoundingBox());
				for ( Agent a : neighbors )
					for ( Surface s : (List<Surface>) a.get(NameRef.surfaceList))
						for(SubPoint p : aGrid.getSubPoints())
							if(s.distanceTo(new Ball(p.getLocation(),0.0)) < 0.0)
							{
								HashMap<int[],Double> distributionMap = 
										(HashMap<int[],Double>) a.getValue("volumeDistribution");
								distributionMap.put(p.getCoord(), p.getVolume());
							}
				coords = aGrid.iteratorNext();
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
}

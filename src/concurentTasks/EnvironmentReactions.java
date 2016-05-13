package concurentTasks;

import static grid.SpatialGrid.ArrayType.CONCN;
import static grid.SpatialGrid.ArrayType.PRODUCTIONRATE;

import java.util.HashMap;
import java.util.List;

import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import reaction.Reaction;

public class EnvironmentReactions implements ConcurrentTask 
{
	
	final List<int[]> _coords;
	final EnvironmentContainer environment;
	final AgentContainer agents;

	public EnvironmentReactions(List<int[]> coords, EnvironmentContainer 
			environment, AgentContainer agents)
	{
		this._coords = coords;
		this.environment = environment;
		this.agents = agents;
	}
	

	public ConcurrentTask part(int start, int end) 
	{
		return new EnvironmentReactions(_coords.subList(start, end), 
				environment, agents);
	}

	public void task() 
	{
		
		SpatialGrid solute;

		for ( int[] coord : _coords)
		{
			/* Iterate over all compartment reactions. */
			for (Reaction r : environment.getReactions() )
			{
				/* Obtain concentrations in gridCell. */
				HashMap<String,Double> concentrations = 
						new HashMap<String,Double>();
				for ( String varName : r.variableNames )
				{
					if ( environment.isSoluteName(varName) )
					{
						solute = environment.getSoluteGrid(varName);
						concentrations.put(varName,
									solute.getValueAt(CONCN, coord));
					}
				}
				/* Obtain rate of the reaction. */
				double rate = r.getRate(concentrations);
				double productionRate;
				for ( String product : r.getStoichiometry().keySet())
				{
					productionRate = rate * r.getStoichiometry(product);
					if ( environment.isSoluteName(product) )
					{
						/* Write rate for each product to grid. */
						solute = environment.getSoluteGrid(product);
						solute.addValueAt(PRODUCTIONRATE, 
											coord, productionRate);
					}
				}
			}
		}
		
	}

	public int size() 
	{
		return _coords.size();
	}

}

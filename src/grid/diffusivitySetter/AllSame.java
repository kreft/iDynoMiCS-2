/**
 * 
 */
package grid.diffusivitySetter;

import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;

/**
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class AllSame implements IsDiffusivitySetter
{
	protected double _diffusivity;
	
	/**
	 * \brief TODO
	 * 
	 */
	public AllSame()
	{
		// TODO Auto-generated constructor stub
	}
	
	
	
	@Override
	public void updateDiffusivity(SpatialGrid diffusivityGrid, SpatialGrid[] concentrationGrids,
			EnvironmentContainer env, AgentContainer agents) {
		// TODO Auto-generated method stub
		
	}

}

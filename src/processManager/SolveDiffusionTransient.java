/**
 * 
 */
package processManager;

import java.util.HashMap;


import grid.SpatialGrid;
import idynomics.AgentContainer;
import solver.PDEexplicit;
import solver.PDEsolver;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public class SolveDiffusionTransient extends ProcessManager
{
	/**
	 * TODO
	 */
	protected PDEsolver _solver;
	
	/**
	 * TODO
	 */
	protected String[] _soluteNames;
	
	/**
	 * \brief TODO
	 * 
	 */
	public SolveDiffusionTransient()
	{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEexplicit();
		this._solver.init(this._soluteNames, false);
	}
	
	@Override
	protected void internalStep(HashMap<String, SpatialGrid> solutes,
														AgentContainer agents)
	{
		/*
		 * TODO
		 */
		this._solver.setUpdaterFunc((HashMap<String, SpatialGrid> hm) ->
		{
			//TODO
			SpatialGrid sg;
			for ( String soluteName : this._soluteNames )
			{
				sg = hm.get(soluteName);
				sg.setAllTo(SpatialGrid.domain, 1.0, true);
			}
		});
		/*
		 * 
		 */
		this._solver.solve(solutes, this._timeStepSize);
	}

}

package processManager;

import static grid.SpatialGrid.ArrayType.DIFFUSIVITY;
import static grid.SpatialGrid.ArrayType.PRODUCTIONRATE;

import java.util.HashMap;

import org.w3c.dom.Element;

import grid.SpatialGrid;
import grid.wellmixedSetter.AllSame;
import grid.wellmixedSetter.IsWellmixedSetter;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import solver.PDEexplicit;
import solver.PDEsolver;
import solver.PDEupdater;

public final class ProcessManagerLibrary
{
	public static class SolveDiffusionTransient extends ProcessManager
	{
		/**
		 * Instance of a subclass of {@code PDEsolver}, e.g. {@code PDEexplicit}.
		 */
		protected PDEsolver _solver;
		/**
		 * The names of all solutes this solver is responsible for.
		 */
		protected String[] _soluteNames;
		/**
		 * 
		 */
		protected HashMap<String,IsWellmixedSetter> _wellmixed;
		/**
		 * TODO this may need to be generalised to some method for setting
		 * diffusivities, e.g. lower inside biofilm.
		 */
		protected HashMap<String,Double> _diffusivity;
		
		public void init(Element xmlElem)
		{
			super.init(xmlElem);
			
			this.init(getStringA("solutes"));
		}
		
		public void init(String[] soluteNames)
		{
			this._soluteNames = soluteNames;
			// TODO Let the user choose which ODEsolver to use.
			this._solver = new PDEexplicit();
			this._solver.init(this._soluteNames, false);
			
			// TODO quick fix for now
			this._wellmixed = new HashMap<String,IsWellmixedSetter>();
			for ( String soluteName : this._soluteNames )
			{
				AllSame mixer = new AllSame();
				mixer.setValue(1.0);
				this._wellmixed.put(soluteName, mixer);
			}
			// TODO enter a diffusivity other than one!
			this._diffusivity = new HashMap<String,Double>();
			for ( String sName : soluteNames )
				this._diffusivity.put(sName, 1.0);
		}
		
		@Override
		protected void internalStep(
					EnvironmentContainer environment, AgentContainer agents)
		{
			ProcessManagerTools.setupAgentDistributionMaps(environment, agents);
			/*
			 * Reset the solute grids.
			 */
			SpatialGrid solute;
			for ( String soluteName : _soluteNames )
			{
				solute = environment.getSoluteGrid(soluteName);
				/* Set up the relevant arrays in each of our solute grids.*/
				solute.newArray(PRODUCTIONRATE);
				// TODO use a diffusion setter
				solute.newArray(DIFFUSIVITY, _diffusivity.get(soluteName));
				this._wellmixed.get(soluteName).updateWellmixed(solute, agents);
			}
			/*
			 * Make the updater method
			 */
			PDEupdater updater = 
					ProcessManagerTools.standardUpdater(environment, agents);
			/*
			 * Set the updater method and solve.
			 */
			this._solver.setUpdater(updater);
			this._solver.solve(environment.getSolutes(), this._timeStepSize);
		}
	}
}

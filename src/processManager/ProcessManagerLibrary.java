package processManager;

import static grid.SpatialGrid.ArrayType.PRODUCTIONRATE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Element;

import boundary.ChemostatConnection;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.diffusivitySetter.AllSameDiffuse;
import grid.diffusivitySetter.IsDiffusivitySetter;
import grid.wellmixedSetter.AllSameMixing;
import grid.wellmixedSetter.IsWellmixedSetter;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import solver.ODEheunsmethod;
import solver.ODErosenbrock;
import solver.ODEsolver;
import solver.PDEexplicit;
import solver.PDEsolver;
import solver.PDEupdater;
import utility.Helper;

public final class ProcessManagerLibrary
{
	/**
	 * \brief Simulate the diffusion of solutes and their
	 * production/consumption by reactions in a time-dependent manner.
	 * 
	 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, UK
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
	 */
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
		protected Map<String,IsWellmixedSetter> _wellmixed;
		/**
		 * TODO this may need to be generalised to some method for setting
		 * diffusivities, e.g. lower inside biofilm.
		 */
		protected Map<String,IsDiffusivitySetter> _diffusivity;
		
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
				AllSameMixing mixer = new AllSameMixing();
				mixer.setValue(1.0);
				this._wellmixed.put(soluteName, mixer);
			}
			// TODO enter a diffusivity other than one!
			this._diffusivity = new HashMap<String,IsDiffusivitySetter>();
			for ( String sName : soluteNames )
				this._diffusivity.put(sName, new AllSameDiffuse(1.0));
		}
		
		@Override
		protected void internalStep(
					EnvironmentContainer environment, AgentContainer agents)
		{
			PMToolsDiffuseReact.setupAgentDistributionMaps(environment, agents);
			/*
			 * Reset the solute grids.
			 */
			SpatialGrid solute;
			for ( String soluteName : _soluteNames )
			{
				solute = environment.getSoluteGrid(soluteName);
				/* Set up the relevant arrays in each of our solute grids.*/
				solute.newArray(PRODUCTIONRATE);
				this._diffusivity.get(soluteName).updateDiffusivity(
												solute, environment, agents);
				this._wellmixed.get(soluteName).updateWellmixed(solute, agents);
			}
			/*
			 * Make the updater method
			 */
			PDEupdater updater = 
					PMToolsDiffuseReact.standardUpdater(environment, agents);
			/*
			 * Set the updater method and solve.
			 */
			this._solver.setUpdater(updater);
			this._solver.solve(environment.getSolutes(), this._timeStepSize);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK
	 */
	public static class SolveChemostat extends ProcessManager
	{
		/**
		 * The ODE solver to use when updating solute concentrations. 
		 */
		protected ODEsolver _solver;
		/**
		 * The names of all solutes this is responsible for.
		 */
		protected String[] _soluteNames = new String[0];
		/**
		 * 
		 */
		protected LinkedList<ChemostatConnection> _outflows = 
										new LinkedList<ChemostatConnection>();
		
		/**
		 * Temporary vector of solute concentrations in the same order as
		 * _soluteNames.
		 */
		private double[] _y;
		
		@Override
		public void init(Element xmlElem)
		{
			super.init(xmlElem);
			this.init();
		}
		
		/**
		 * TODO
		 */
		public void init()
		{
			this.init((String[]) reg().getValue(this, "soluteNames"));
		}
		
		public void init(String[] soluteNames)
		{
			this._soluteNames = soluteNames;
			/*
			 * Initialise the solver.
			 */
			// TODO This should be done better
			String solverName = this.getString("solver");
			solverName = Helper.setIfNone(solverName, "rosenbrock");
			double hMax = Helper.setIfNone(this.getDouble("hMax"), 1.0e-6);
			if ( solverName.equals("heun") )
				this._solver = new ODEheunsmethod(soluteNames, false, hMax);
			else
			{
				double tol = Helper.setIfNone(this.getDouble("tolerance"), 1.0e-6);
				this._solver = new ODErosenbrock(soluteNames, false, tol, hMax);
			}
		}
		
		@Override
		protected void internalStep(EnvironmentContainer environment,
				AgentContainer agents)
		{
			PMToolsChemostat.acceptAllInboundAgents(environment, agents);
			
			this._solver.setDerivatives(
					PMToolsChemostat.standardDerivs(
									this._soluteNames, environment, agents));
			/*
			 * Solve the system and update the environment.
			 */
			for ( int i = 0; i < this._soluteNames.length; i++ )
			{
				this._y[i] = environment.getAverageConcentration(
														this._soluteNames[i]);
			}
			try { this._y = this._solver.solve(this._y, this._timeStepSize); }
			catch ( Exception e) { e.printStackTrace();}
			Log.out(Tier.DEBUG, "y is now "+Arrays.toString(this._y));
			for ( int i = 0; i < this._soluteNames.length; i++ )
			{
				environment.setAllConcentration(
										this._soluteNames[i], this._y[i]);
			}
			/*
			 * Finally, select Agents to be washed out of the Compartment.
			 */
			PMToolsChemostat.diluteAgents(
									this._timeStepSize, environment, agents);
		}
	}
}

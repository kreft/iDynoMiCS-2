package processManager;

import java.util.Arrays;
import java.util.LinkedList;

import org.w3c.dom.Element;

import boundary.ChemostatConnection;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import solver.ODEheunsmethod;
import solver.ODErosenbrock;
import solver.ODEsolver;
import utility.Helper;

public final class ProcessManagerLibrary
{
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

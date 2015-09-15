package processManager;

import java.util.HashMap;

import agent.Agent;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import solver.ODErosenbrock;
import utility.ExtraMath;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) Centre for Computational
 * Biology, University of Birmingham, U.K.
 * @since August 2015
 */
public class SolveChemostat extends ProcessManager
{
	/**
	 * TODO Could let the user choose which ODEsolver to use, if we ever get
	 * around to implementing more.
	 */
	protected ODErosenbrock _solver;
	
	/**
	 * TODO
	 */
	protected String[] _soluteNames;
	
	/**
	 * 
	 */
	protected HashMap<String, Double> _inflow;
	
	/**
	 * Dilution rate in units of time<sup>-1</sup>.
	 */
	protected double _dilution;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 *
	 */
	public SolveChemostat()
	{
		
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		this._solver = new ODErosenbrock();
		this._solver.init(this._soluteNames, false, 1.0e-6, 1.0e-6);
	}
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param inflow
	 * @param dilution
	 */
	public void setInflow(HashMap<String, Double> inflow)
	{
		this._inflow = inflow;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param dilution
	 */
	public void setDilution(double dilution)
	{
		this._dilution = dilution;
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep(HashMap<String, SpatialGrid> solutes,
														AgentContainer agents)
	{
		/*
		 * Update the solver's 1st derivative function (dY/dT).
		 */
		this._solver.set1stDeriv( (double[] y) ->
		{
			/*
			 * First deal with inflow and dilution: dYdT = D(Sin - S)
			 */
			double[] dYdT = Vector.copy(y);
			for ( int i = 0; i < this._soluteNames.length; i++ )
				dYdT[i] -= this._inflow.get(this._soluteNames[i]);
			Vector.times(dYdT, -this._dilution);
			/*
			 * TODO Apply agent reactions
			 */
			//for ( Agent agent : agents.getAllAgents() )
			//	agent.
			/*
			 * TODO Apply extracellular reactions.
			 */
			/*System.out.println("\tS -> dYdT:"); //Bughunt
			for ( int i = 0; i < y.length; i++ )
				System.out.println("\t"+y[i]+"->"+dYdT[i]);*/
			return dYdT;
		});
		/*
		 * TODO Update the solver's 2nd derivative function (dF/dT)?
		 * Leave out if we don't want to do this for reactions (the solver
		 * will estimate dFdT numerically)
		 */
		this._solver.set2ndDeriv( (double[] y) ->
		{
			double[] dFdT = Vector.copy(y);
			for ( int i = 0; i < this._soluteNames.length; i++ )
				dFdT[i] -= this._inflow.get(this._soluteNames[i]);
			Vector.times(dFdT, ExtraMath.sq(this._dilution));
			/*
			 * TODO Apply agent reactions
			 */
			//for ( Agent agent : agents.getAllAgents() )
			//	agent.
			/*
			 * TODO Apply extracellular reactions.
			 */
			return dFdT;
		});
		/*
		 * Update the solver's Jacobian function (dF/dY).
		 */
		this._solver.setJacobian( (double[] y) ->
		{
			/*
			 * First deal with dilution: dYdY = -D
			 */
			double[][] jac = Matrix.identityDbl(y.length);
			Matrix.times(jac, -this._dilution);
			/*
			 * TODO Apply agent reactions
			 */
			//for ( Agent agent : agents.getAllAgents() )
			//	agent.
			/*
			 * TODO Apply extracellular reactions.
			 */
			
			return jac;
		});
		/*
		 * Finally, solve the system.
		 */
		double[] y = getY(solutes);
		try { y = this._solver.solve(y, this._timeStepSize); }
		catch ( Exception e) {}
		updateSolutes(solutes, y);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @return
	 */
	protected double[] getY(HashMap<String, SpatialGrid> solutes)
	{
		double[] y = Vector.zerosDbl(this._soluteNames.length);
		SpatialGrid sg;
		for ( int i = 0; i < y.length; i++ )
		{
			sg = solutes.get(this._soluteNames[i]);
			//TODO Use average?
			y[i] = sg.getMax(SpatialGrid.concn);
		}
		return y;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @param y
	 */
	protected void updateSolutes(HashMap<String, SpatialGrid> solutes,
																double[] y)
	{
		for ( int i = 0; i < y.length; i++ )
		{
			solutes.get(this._soluteNames[i]).setAllTo(
											SpatialGrid.concn, y[i], true);
		}
	}
}

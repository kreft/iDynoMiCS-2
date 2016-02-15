package processManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import agent.Agent;
import boundary.Boundary;
import boundary.ChemostatConnection;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import reaction.Reaction;
import solver.ODEheunsmethod;
import solver.ODErosenbrock;
import solver.ODEsolver;
import solver.ODEsolver.Derivatives;
import utility.ExtraMath;
import utility.Helper;

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
	protected ODEsolver _solver;

	/**
	 * TODO
	 */
	protected String[] _soluteNames;
	
	/**
	 * Temporary 
	 */
	protected HashMap<String,Double> _rates = new HashMap<String,Double>();
	
	/**
	 * 
	 */
	protected HashMap<String, Double> _inflow;

	/**
	 * Dilution rate in units of time<sup>-1</sup>.
	 */
	protected double _dilution;


	protected LinkedList<ChemostatConnection> _inConnections = 
			new LinkedList<ChemostatConnection>();

	protected LinkedList<ChemostatConnection> _outConnections = 
			new LinkedList<ChemostatConnection>();

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
	
	
	public void init()
	{
		init((String[]) reg().getValue(this, "soluteNames"));
	}

	/**
	 * \brief TODO
	 * 
	 * @param soluteNames
	 */
	public void init(String[] soluteNames)
	{
		this._soluteNames = soluteNames;
		if(Helper.setIfNone(getString("solver"),"rosenbrock").equals("heun"))
			this._solver = new ODEheunsmethod(this._soluteNames, false, 
					Helper.setIfNone(getDouble("hMax"), 1.0e-6));
		else
			this._solver = new ODErosenbrock(this._soluteNames, false, 
					Helper.setIfNone(getDouble("tolerance"), 1.0e-6), 
					Helper.setIfNone(getDouble("hMax"), 1.0e-6));
		this._inflow = new HashMap<String, Double>();
		for ( String sName : this._soluteNames )
			this._inflow.put(sName, 0.0);
		this._dilution = 0.0;
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

	/**
	 * \brief TODO
	 * 
	 * @param boundaries
	 */
	@Override
	public void showBoundaries(Collection<Boundary> boundaries)
	{
		ChemostatConnection aChemoConnect;
		for ( Boundary aBoundary : boundaries )
			if ( aBoundary instanceof ChemostatConnection )
			{
				aChemoConnect = (ChemostatConnection) aBoundary;
				if ( aChemoConnect.getFlowRate() > 0.0 )
					this._inConnections.add(aChemoConnect);
				else
					this._outConnections.add(aChemoConnect);
			}
		/*
		 * Update the dilution rate now to check that the outflow matches the
		 * inflow.
		 */
		this.updateDilutionInflow();
	}



	/*************************************************************************
	 * STEPPING
	 ************************************************************************/

	/**
	 * \brief TODO
	 */
	protected void updateDilutionInflow()
	{
		double inRate = 0.0, outRate = 0.0;
		for ( String sName : this._soluteNames )
			this._inflow.put(sName, 0.0);
		for ( ChemostatConnection aChemoConnect : this._inConnections )
		{
			inRate += aChemoConnect.getFlowRate();
			for ( String sName : this._soluteNames )
			{
				double temp = aChemoConnect.getConcentrations().get(sName);
				this._inflow.put(sName, this._inflow.get(sName)+temp);
			}
		}
		for ( ChemostatConnection aChemoConnect : this._outConnections )
			outRate -= aChemoConnect.getFlowRate();
		if ( inRate != outRate )
		{
			throw new IllegalArgumentException(
					"Chemostat inflow and outflow rates must match!");
		}
		this._dilution = inRate;
	}

	@Override
	protected void internalStep(EnvironmentContainer environment,
			AgentContainer agents)
	{
		this.updateDilutionInflow();
		/*
		 * Update the solver's 1st derivative function (dY/dT).
		 */
		Derivatives deriv = new Derivatives()
		{
			@Override
			public double[] firstDeriv(double[] y)
			{
				/*
				 * First deal with inflow and dilution: dYdT = D(Sin - S)
				 */
				double[] dYdT = Vector.reverse(Vector.copy(y));
				HashMap<String,Double> concns = new HashMap<String,Double>();
				for ( int i = 0; i < _soluteNames.length; i++ )
				{
					dYdT[i] += _inflow.get(_soluteNames[i]);
					concns.put(_soluteNames[i], y[i]);
				}
				Vector.timesEquals(dYdT, _dilution);
				/*
				 * Apply agent reactions. Note that any agents without
				 * reactions will return null, and so will be skipped.
				 */
				for ( Agent agent : agents.getAllAgents() )
				{
					@SuppressWarnings("unchecked")
					List<Reaction> reactions = 
									(List<Reaction>) agent.get("reactions");
					if ( reactions == null )
						continue;
					for (Reaction aReac : reactions)
						applyReactionFluxes(aReac, concns, dYdT);
					// TODO tell the agent about the rates of production of its
					// biomass types?
				}
				/*
				 * Apply extracellular reactions.
				 */
				for ( Reaction aReac : environment.getReactions() )
					applyReactionFluxes(aReac, concns, dYdT);
				return dYdT;
			}
			@Override
			public double[] secondDeriv(double[] y)
			{
				double[] dFdT = Vector.copy(y);
				for ( int i = 0; i < _soluteNames.length; i++ )
					dFdT[i] -= _inflow.get(_soluteNames[i]);
				Vector.timesEquals(dFdT, ExtraMath.sq(_dilution));
				/*
				 * TODO Apply agent reactions
				 */
				//for ( Agent agent : agents.getAllAgents() )
				//	agent.
				/*
				 * TODO Apply extracellular reactions.
				 */
				return dFdT;
			}
			@Override
			public double[][] jacobian(double[] y)
			{
				/*
				 * First deal with dilution: dYdY = -D
				 */
				double[][] jac = Matrix.identityDbl(y.length);
				Matrix.timesEquals(jac, -_dilution);
				/*
				 * TODO Apply agent reactions
				 */
				//for ( Agent agent : agents.getAllAgents() )
				//	agent.
				/*
				 * TODO Apply extracellular reactions.
				 */

				return jac;
			}
		};
		this._solver.setDerivatives(deriv);
		/*
		 * Finally, solve the system.
		 */
		double[] y = getY(environment);
		try { y = this._solver.solve(y, this._timeStepSize); }
		catch ( Exception e) { e.printStackTrace();}
		updateSolutes(environment, y);
	}

	/**
	 * \brief TODO
	 * 
	 * @param solutes
	 * @return
	 */
	protected double[] getY(EnvironmentContainer environment)
	{
		double[] y = Vector.zerosDbl(this._soluteNames.length);
		SpatialGrid sg;
		for ( int i = 0; i < y.length; i++ )
		{
			sg = environment.getSoluteGrid(this._soluteNames[i]);
			//TODO Use average?
			y[i] = sg.getMax(ArrayType.CONCN);
		}
		return y;
	}

	/**
	 * \brief TODO
	 * 
	 * TODO This may need to be updated now that solutes belong to the
	 * environment container
	 * 
	 * @param solutes
	 * @param y
	 */
	protected void updateSolutes(EnvironmentContainer environment, double[] y)
	{
		for ( int i = 0; i < y.length; i++ )
		{
			environment.getSoluteGrid(this._soluteNames[i])
										.setAllTo(ArrayType.CONCN, y[i]);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param aReac
	 * @param concns
	 * @param dYdT
	 */
	protected void applyReactionFluxes(Reaction aReac,
								HashMap<String, Double> concns, double[] dYdT)
	{
		for ( int i = 0; i < this._soluteNames.length; i++ )
			dYdT[i] += aReac.getProductionRate(this._soluteNames[i], concns);
	}
}

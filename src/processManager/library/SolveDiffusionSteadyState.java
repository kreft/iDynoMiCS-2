package processManager.library;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.diffusivitySetter.AllSameDiffuse;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessDiffusion;
import reaction.Reaction;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.subvoxel.CoordinateMap;
import solver.PDEgaussseidel;
import solver.PDEupdater;
import utility.Helper;

/**
 * \brief Simulate the diffusion of solutes and their production/consumption by
 * reactions in a steady-state manner, in a spatial {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionSteadyState extends ProcessDiffusion
{
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * 
	 * Initiation from protocol file: 
	 * 
	 * TODO verify and finalise
	 */
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);

		// TODO Do you need to get anything else from xml?

		/* gets specific solutes from process manager aspect registry if they
		 * are defined, if not, solve for all solutes.
		 */
		this._soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(this._environment.getSoluteNames()));

		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEgaussseidel();
		this._solver.init(this._soluteNames, false);
		this._solver.setUpdater(this.standardUpdater());
		
		/* Plug any gaps (FIXME temporary measure). */
		for ( String sName : this._soluteNames )
			if ( ! this._diffusivity.containsKey(sName) )
			{
				Log.out(Tier.CRITICAL, 
						"WARNING: Using default diffusivity for solute "+sName);
				this._diffusivity.put(sName, new AllSameDiffuse(1.0));
			}
		
		String msg = "SolveDiffusionSteadyState responsible for solutes: ";
		for ( String s : this._soluteNames )
			msg += s + ", ";
		Log.out(Tier.EXPRESSIVE, msg);
	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		/*
		 * Do the generic set up and solving.
		 */
		super.internalStep();
		/*
		 * Estimate the steady-state mass flows in or out of the well-mixed
		 * region, and distribute it among the relevant boundaries.
		 */
		//TODO
		/*
		 * Estimate agent growth based on the steady-state solute 
		 * concentrations.
		 */
		for ( Agent agent : this._agents.getAllLocatedAgents() )
			this.applyAgentGrowth(agent);

		/* perform final clean-up and update agents to represent updated 
		 * situation. */
		this.postStep();
	}
	
	/* ***********************************************************************
	 * INTERNAL METHODS
	 * **********************************************************************/
	
	/**
	 * \brief The standard PDE updater method resets the solute
	 * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
	 * {@code Agent}s to grow.
	 * 
	 * @return PDE updater method.
	 */
	private PDEupdater standardUpdater()
	{
		return new PDEupdater()
		{
			/*
			 * This is the updater method that the PDEsolver will use before
			 * each mini-timestep.
			 */
			@Override
			public void prestep(Collection<SpatialGrid> variables, double dt)
			{
				for ( SpatialGrid var : variables )
					var.newArray(PRODUCTIONRATE);
				applyEnvReactions();
				for ( Agent agent : _agents.getAllLocatedAgents() )
					applyAgentReactions(agent);
			}
		};
	}
	
	/**
	 * \brief Apply the reactions for a single agent.
	 * 
	 * <p><b>Note</b>: this method assumes that the volume distribution map
	 * of this agent has already been calculated. This is typically done just
	 * once per process manager step, rather than at every PDE solver
	 * relaxation.</p>
	 * 
	 * @param agent Agent assumed to have reactions (biomass will be altered by
	 * this method).
	 */
	private void applyAgentReactions(Agent agent)
	{
		/*
		 * Get the agent's reactions: if it has none, then there is nothing
		 * more to do.
		 */
		@SuppressWarnings("unchecked")
		List<Reaction> reactions = 
				(List<Reaction>) agent.getValue(XmlRef.reactions);
		if ( reactions == null )
			return;
		/*
		 * Get the distribution map and scale it so that its contents sum up to
		 * one.
		 */
		CoordinateMap distributionMap = 
				(CoordinateMap) agent.getValue(VOLUME_DISTRIBUTION_MAP);
		distributionMap.scale();
		/*
		 * Get the agent biomass kinds as a map. Copy it now so that we can
		 * use this copy to store the changes.
		 */
		Map<String,Double> biomass = AgentContainer.getAgentMassMap(agent);
		/*
		 * Now look at all the voxels this agent covers.
		 */
		Map<String,Double> concns = new HashMap<String,Double>();
		Map<String,Double> stoichiometry;
		SpatialGrid solute;
		Shape shape = this._agents.getShape();
		double concn, rate, productRate, volume, perVolume;
		for ( int[] coord : distributionMap.keySet() )
		{
			volume = shape.getVoxelVolume(coord);
			perVolume = Math.pow(volume, -1.0);
			for ( Reaction r : reactions )
			{
				/* 
				 * Build the dictionary of variable values. Note that these 
				 * will likely overlap with the names in the reaction 
				 * stoichiometry (handled after the reaction rate), but will 
				 * not always be the same. Here we are interested in those that
				 * affect the reaction, and not those that are affected by it.
				 */
				concns.clear();
				for ( String varName : r.getVariableNames() )
				{
					if ( this._environment.isSoluteName(varName) )
					{
						solute = this._environment.getSoluteGrid(varName);
						concn = solute.getValueAt(CONCN, coord);
					}
					else if ( biomass.containsKey(varName) )
					{
						concn = biomass.get(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else if ( agent.isAspect(varName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						concn = agent.getDouble(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else
					{
						// TODO safety?
						concn = 0.0;
					}
					concns.put(varName, concn);
				}
				/*
				 * Calculate the reaction rate based on the variables just 
				 * retrieved.
				 */
				rate = r.getRate(concns);
				/* 
				 * Now that we have the reaction rate, we can distribute the 
				 * effects of the reaction. Note again that the names in the 
				 * stoichiometry may not be the same as those in the reaction
				 * variables (although there is likely to be a large overlap).
				 */
				stoichiometry = r.getStoichiometry();
				for ( String productName : stoichiometry.keySet() )
				{
					productRate = rate * stoichiometry.get(productName);
					if ( this._environment.isSoluteName(productName) )
					{
						solute = this._environment.getSoluteGrid(productName);
						solute.addValueAt(PRODUCTIONRATE, coord, productRate);
					}
					/* 
					 * Unlike in a transient solver, we do not update the agent
					 * mass here.
					 */
				}
			}
		}
	}
	
	private void applyAgentGrowth(Agent agent)
	{
		/*
		 * Get the agent's reactions: if it has none, then there is nothing
		 * more to do.
		 */
		@SuppressWarnings("unchecked")
		List<Reaction> reactions = 
				(List<Reaction>) agent.getValue(XmlRef.reactions);
		if ( reactions == null )
			return;
		/*
		 * Get the distribution map and scale it so that its contents sum up to
		 * one.
		 */
		CoordinateMap distributionMap = 
				(CoordinateMap) agent.getValue(VOLUME_DISTRIBUTION_MAP);
		distributionMap.scale();
		/*
		 * Get the agent biomass kinds as a map. Copy it now so that we can
		 * use this copy to store the changes.
		 */
		Map<String,Double> biomass = AgentContainer.getAgentMassMap(agent);
		@SuppressWarnings("unchecked")
		Map<String,Double> newBiomass = (HashMap<String,Double>)
				ObjectFactory.copy(biomass);
		/*
		 * Now look at all the voxels this agent covers.
		 */
		Map<String,Double> concns = new HashMap<String,Double>();
		Map<String,Double> stoichiometry;
		SpatialGrid solute;
		Shape shape = this._agents.getShape();
		double concn, rate, productRate, volume, perVolume;
		for ( int[] coord : distributionMap.keySet() )
		{
			volume = shape.getVoxelVolume(coord);
			perVolume = Math.pow(volume, -1.0);
			for ( Reaction r : reactions )
			{
				/* 
				 * Build the dictionary of variable values. Note that these 
				 * will likely overlap with the names in the reaction 
				 * stoichiometry (handled after the reaction rate), but will 
				 * not always be the same. Here we are interested in those that
				 * affect the reaction, and not those that are affected by it.
				 */
				concns.clear();
				for ( String varName : r.getVariableNames() )
				{
					if ( this._environment.isSoluteName(varName) )
					{
						solute = this._environment.getSoluteGrid(varName);
						concn = solute.getValueAt(CONCN, coord);
					}
					else if ( biomass.containsKey(varName) )
					{
						concn = biomass.get(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else if ( agent.isAspect(varName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						concn = agent.getDouble(varName) * 
								distributionMap.get(coord) * perVolume;
					}
					else
					{
						// TODO safety?
						concn = 0.0;
					}
					concns.put(varName, concn);
				}
				/*
				 * Calculate the reaction rate based on the variables just 
				 * retrieved.
				 */
				rate = r.getRate(concns);
				/* 
				 * Now that we have the reaction rate, we can distribute the 
				 * effects of the reaction. Note again that the names in the 
				 * stoichiometry may not be the same as those in the reaction
				 * variables (although there is likely to be a large overlap).
				 */
				stoichiometry = r.getStoichiometry();
				for ( String productName : stoichiometry.keySet() )
				{
					productRate = rate * stoichiometry.get(productName);
					if ( agent.isAspect(productName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						newBiomass.put(productName, agent.getDouble(productName)
								+ (productRate * this._timeStepSize * volume));
					}
				}
			}
		}
		AgentContainer.updateAgentMass(agent, newBiomass);
	}
}

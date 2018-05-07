/**
 * 
 */
package processManager.library;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import dataIO.ObjectFactory;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.RegularReaction;
import reaction.Reaction;
import referenceLibrary.XmlRef;
import shape.subvoxel.CoordinateMap;
import shape.Shape;
import solver.PDEexplicit;
import solver.PDEupdater;

/**
 * \brief Simulate the diffusion of solutes and their production/consumption by
 * reactions in a time-dependent manner, in a spatial {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionTransient extends ProcessDiffusion
{
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);

		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEexplicit();

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
		 * If any mass has flowed in or out of the well-mixed region,
		 * distribute it among the relevant boundaries.
		 */
		this._environment.distributeWellMixedFlows();

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
	protected PDEupdater standardUpdater()
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
				applyEnvReactions(variables);
				for ( Agent agent : _agents.getAllLocatedAgents() )
					applyAgentReactions(agent, dt);
			}
		};
	}
	
	/**
	 * \brief Apply the reactions for a single agent.
	 * 
	 * <p><b>Note</b>: this method assumes that the volume distribution map
	 * of this agent has already been calculated. This is typically done just
	 * once per process manager step, rather than at every PDE solver
	 * mini-timestep.</p>
	 * 
	 * <p>Note also that here the solute grids PRODUCTIONRATE arrays are 
	 * updated, and the agent's biomass is updated immediately after all
	 * relevant voxels have been visited. This is a different approach to the
	 * one taken in SolveChemostat, where applyAgentReactions is split into two
	 * methods.</p>
	 * 
	 * @param agent Agent assumed to have reactions (biomass will be altered by
	 * this method).
	 * @param dt Length of the mini time-step to use.
	 */
	private void applyAgentReactions(Agent agent, double dt)
	{
		/*
		 * Get the agent's reactions: if it has none, then there is nothing
		 * more to do.
		 */
		@SuppressWarnings("unchecked")
		List<RegularReaction> reactions = 
				(List<RegularReaction>) agent.getValue(XmlRef.reactions);
		if ( reactions == null )
			return;
		/*
		 * Get the distribution map and scale it so that its contents sum up to
		 * one.
		 */
		@SuppressWarnings("unchecked")
		Map<Shape, CoordinateMap> mapOfMaps = (Map<Shape, CoordinateMap>)
						agent.getValue(VOLUME_DISTRIBUTION_MAP);
		CoordinateMap distributionMap = 
				mapOfMaps.get(agent.getCompartment().getShape());
		distributionMap.scale();
		/*
		 * Get the agent biomass kinds as a map. Copy it now so that we can
		 * use this copy to store the changes.
		 */
		Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
		@SuppressWarnings("unchecked")
		Map<String,Double> newBiomass = (HashMap<String,Double>)
				ObjectFactory.copy(biomass);
		/*
		 * Now look at all the voxels this agent covers.
		 */
		Map<String,Double> concns = new HashMap<String,Double>();

		SpatialGrid solute;
		Shape shape = this._agents.getShape();
		double concn, productRate, volume, perVolume;
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
				for ( String varName : r.getConstituentNames() )
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
				 * Now that we have the reaction rate, we can distribute the 
				 * effects of the reaction. Note again that the names in the 
				 * stoichiometry may not be the same as those in the reaction
				 * variables (although there is likely to be a large overlap).
				 */
				for ( String productName : r.getReactantNames() )
				{
					productRate = r.getProductionRate(concns,productName);
					if ( this._environment.isSoluteName(productName) )
					{
						solute = this._environment.getSoluteGrid(productName);
						solute.addValueAt(PRODUCTIONRATE, coord, productRate);
					}
					else if ( newBiomass.containsKey(productName) )
					{
						newBiomass.put(productName, newBiomass.get(productName)
								+ (productRate * dt * volume));
					}
					else if ( agent.isAspect(productName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						newBiomass.put(productName, agent.getDouble(productName)
								+ (productRate * dt * volume));
					}
					else
					{
						//TODO quick fix If not defined elsewhere add it to the map
						newBiomass.put(productName, (productRate * dt * volume));
						System.out.println("agent reaction catched " + 
								productName);
						// TODO safety?
					}
				}
			}
		}
		ProcessMethods.updateAgentMass(agent, newBiomass);
	}
}
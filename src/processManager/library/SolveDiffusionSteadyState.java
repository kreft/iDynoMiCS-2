package processManager.library;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import agent.Agent;
import bookkeeper.KeeperEntry.EventType;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.ObjectFactory;
import grid.SpatialGrid;
import idynomics.Global;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.Reaction;
import reaction.RegularReaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.subvoxel.IntegerArray;
import solver.PDEmultigrid;

/**
 * \brief Simulate the diffusion of solutes and their production/consumption by
 * reactions in a steady-state manner, in a spatial {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionSteadyState extends ProcessDiffusion
{
	public static String ABS_TOLERANCE = AspectRef.solverAbsTolerance;
	
	public static String REL_TOLERANCE = AspectRef.solverRelTolerance;
	
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
		
		double absTol = (double) this.getOr(ABS_TOLERANCE, 1.0e-9);
		
		double relTol = (double) this.getOr(REL_TOLERANCE, 1.0e-3);

		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEmultigrid(
				(int) this.getOr(AspectRef.vCycles, 0), 
				(int) this.getOr(AspectRef.preSteps, 0), 
				(int) this.getOr(AspectRef.coarseSteps, 0), 
				(int) this.getOr(AspectRef.postSteps, 0));

		this._solver.setUpdater(this);
		
		this._solver.setAbsoluteTolerance(absTol);
		
		this._solver.setRelativeTolerance(relTol);

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
		
		for ( SpatialGrid var : this._environment.getSolutes() )
		{
			var.reset(PRODUCTIONRATE);
		}
		/*
		 * Estimate agent growth based on the steady-state solute 
		 * concentrations.
		 */
		for ( Agent agent : this._agents.getAllLocatedAgents() )
			this.applyAgentGrowth(agent);
		
//		MultigridLayer currentLayer;
		for ( SpatialGrid var : this._environment.getSolutes() )
		{
			double massMove = var.getTotal(PRODUCTIONRATE);
			var.increaseWellMixedMassFlow(massMove);
		}
		/*
		 * Estimate the steady-state mass flows in or out of the well-mixed
		 * region, and distribute it among the relevant boundaries.
		 */
		this._environment.distributeWellMixedFlows(this._timeStepSize);


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
	public void prestep(Collection<SpatialGrid> variables, double dt)
	{
		for ( SpatialGrid var : variables )
			var.newArray(PRODUCTIONRATE);
		applyEnvReactions(variables);
		for ( Agent agent : _agents.getAllLocatedAgents() )
			applyAgentReactions(agent, variables);
	}

	/**
	 * \brief Apply the reactions for a single agent.
	 * 
	 * <p><b>Note</b>: this method assumes that the volume distribution map
	 * of this agent has already been calculated. This is typically done just
	 * once per process manager step, rather than at every PDE solver
	 * relaxation.</p>
	 * 
	 * @param agent Agent assumed to have reactions (biomass will not be
	 * altered by this method).
	 * @param variables Collection of spatial grids assumed to be the solutes.
	 */
	private void applyAgentReactions(
			Agent agent, Collection<SpatialGrid> variables)
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
		Shape shape = variables.iterator().next().getShape();
		@SuppressWarnings("unchecked")
		Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps = 
				(Map<Shape, HashMap<IntegerArray,Double>>)
				agent.getValue(VOLUME_DISTRIBUTION_MAP);
		
		HashMap<IntegerArray,Double> distributionMap = mapOfMaps.get(shape);
		/*
		 * Get the agent biomass kinds as a map. Copy it now so that we can
		 * use this copy to store the changes.
		 */
		Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
		/*
		 * Now look at all the voxels this agent covers.
		 */
		Map<String,Double> concns = new HashMap<String,Double>();
		SpatialGrid solute;
		double concn, productRate, volume, perVolume;
		for ( IntegerArray coord : distributionMap.keySet() )
		{
			volume = shape.getVoxelVolume(coord.get());
			perVolume = 1.0/volume;
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
					solute = FindGrid(variables, varName);
					if ( solute != null )
						concn = solute.getValueAt(CONCN, coord.get());
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
					solute = FindGrid(variables, productName);
					if ( solute != null )
					{
						productRate = r.getProductionRate(concns, productName);
						solute.addValueAt( PRODUCTIONRATE, coord.get(), volume * 
								productRate );
					}
					/* 
					 * Unlike in a transient solver, we do not update the agent
					 * mass here.
					 */
				}
			}
		}	
		/* debugging */
//		Log.out(Tier.NORMAL , " -- " +
//		this._environment.getSoluteGrid("glucose").getAverage(PRODUCTIONRATE));
	}
	
	private SpatialGrid FindGrid(Collection<SpatialGrid> grids, String name)
	{
		for ( SpatialGrid grid : grids )
			if ( grid.getName().equals(name) )
				return grid;
		return null;
	}
	
	private void applyAgentGrowth(Agent agent)
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
		Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps = 
				( Map<Shape, HashMap<IntegerArray,Double>> )
				agent.getValue(VOLUME_DISTRIBUTION_MAP);
		HashMap<IntegerArray,Double> distributionMap = 
				mapOfMaps.get(agent.getCompartment().getShape());
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
		double concn, productRate, volume, perVolume;
		for ( IntegerArray coord : distributionMap.keySet() )
		{
			volume = this._agents.getShape().getVoxelVolume( coord.get() );
			perVolume = 1.0 / volume;
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
					if ( this._environment.isSoluteName( varName ) )
					{
						solute = this._environment.getSoluteGrid( varName );
						concn = solute.getValueAt( CONCN, coord.get() );
					}
					else if ( biomass.containsKey( varName ) )
					{
						concn = biomass.get( varName ) * 
								distributionMap.get( coord ) * perVolume;

					}
					else if ( agent.isAspect( varName ) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						concn = agent.getDouble( varName ) * 
								distributionMap.get( coord ) * perVolume;
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
					/* FIXME: it is probably faster if we get the reaction rate
					 * once and then calculate the rate per product from that
					 * for each individual product
					 */
					productRate = r.getProductionRate(concns,productName);
					double quantity;
					
					if ( this._environment.isSoluteName(productName) )
					{
						solute = this._environment.getSoluteGrid(productName);
						quantity = 
								productRate * volume * this.getTimeStepSize();
						solute.addValueAt(PRODUCTIONRATE, coord.get(), quantity
								);
					}
					else if ( newBiomass.containsKey(productName) )
					{
						quantity = 
								productRate * this.getTimeStepSize() * volume;
						newBiomass.put(productName, newBiomass.get(productName)
								+ quantity );
					}
					/* FIXME this can create conflicts if users try to mix mass-
					 * maps and simple mass aspects	 */
					else if ( agent.isAspect(productName) )
					{
						/*
						 * Check if the agent has other mass-like aspects
						 * (e.g. EPS).
						 */
						quantity = 
								productRate * this.getTimeStepSize() * volume;
						newBiomass.put(productName, agent.getDouble(productName)
								+ quantity);
					}
					else
					{
						quantity = 
								productRate * this.getTimeStepSize() * volume;
						//TODO quick fix If not defined elsewhere add it to the map
						newBiomass.put(productName, quantity);
						System.out.println("agent reaction catched " + 
								productName);
						// TODO safety?

					}
					if( Global.bookkeeping )
						agent.getCompartment().registerBook(
								EventType.REACTION, 
								productName, 
								String.valueOf( agent.identity() ), 
								String.valueOf( quantity ), null );
				}
			}
		}
		ProcessMethods.updateAgentMass(agent, newBiomass);
	}
}

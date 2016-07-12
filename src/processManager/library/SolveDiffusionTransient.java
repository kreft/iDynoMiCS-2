/**
 * 
 */
package processManager.library;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.DIFFUSIVITY;
import static grid.ArrayType.PRODUCTIONRATE;
import static dataIO.Log.Tier.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.w3c.dom.Element;

import agent.Agent;
import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.subvoxel.CoordinateMap;
import shape.Shape;
import solver.PDEexplicit;
import solver.PDEsolver;
import solver.PDEupdater;
import utility.Helper;

/**
 * \brief Simulate the diffusion of solutes and their production/consumption by
 * reactions in a time-dependent manner, in a spatial {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SolveDiffusionTransient extends ProcessManager
{
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have reactions.
	 */
	protected final static Predicate<Agent> NO_REAC_FILTER = 
			(a -> ! a.isAspect(AspectRef.agentReactions));
	/**
	 * Helper method for filtering local agent lists, so that they only
	 * include those that have relevant components of a body.
	 */
	protected final static Predicate<Agent> NO_BODY_FILTER = 
			(a -> (! a.isAspect(AspectRef.surfaceList)) ||
					( ! a.isAspect(AspectRef.bodyRadius)));
	/**
	 * Aspect name for the {@code coordinateMap} used for establishing which
	 * voxels a located {@code Agent} covers.
	 */
	private static String VOLUME_DISTRIBUTION_MAP = AspectRef.agentVolumeDistributionMap;
	/**
	 * Aspect name for the TODO
	 */
	private static String INTERNAL_PRODUCTION_RATE = AspectRef.internalProductionRate;
	/**
	 * Aspect name for the TODO
	 */
	private static String GROWTH_RATE = AspectRef.growthRate;
	/**
	 * Instance of a subclass of {@code PDEsolver}, e.g. {@code PDEexplicit}.
	 */
	protected PDEsolver _solver;
	/**
	 * The names of all solutes this solver is responsible for.
	 */
	protected String[] _soluteNames;
	/**
	 * TODO 
	 */
	// TODO replace with diffusivitySetter
	protected HashMap<String,Double> _diffusivity;
	
	/**
	 * TODO
	 */
	public String SOLUTES = AspectRef.soluteNames;

	
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		this.init(environment, agents, compartmentName);
	}
	
	/**
	 * \brief Initialise this diffusion-reaction process manager with a list of
	 * solutes it is responsible for.
	 * 
	 * @param soluteNames The list of solutes this is responsible for.
	 */
	public void init(EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(environment, agents, compartmentName);
		String[] soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(
				this._environment.getSoluteNames()));
		init( soluteNames, environment, 
				agents, compartmentName );
	}
	
	public void init( String[] soluteNames, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		/* This super call is only required for the unit tests. */
		super.init(environment, agents, compartmentName);
		this._soluteNames = soluteNames;
		// TODO Let the user choose which ODEsolver to use.
		this._solver = new PDEexplicit();
		this._solver.init(this._soluteNames, false);
		this._solver.setUpdater(this.standardUpdater());
		// TODO enter a diffusivity other than one!
		this._diffusivity = new HashMap<String,Double>();
		for ( String sName : this._soluteNames )
			this._diffusivity.put(sName, 1.0);
		String msg = "SolveDiffusionTransient responsible for solutes: ";
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
		 * Set up the agent mass distribution maps, to ensure that agent
		 * reactions are spread over voxels appropriately.
		 */
		this._agents.setupAgentDistributionMaps();
		/*
		 * Get the environment to update its well-mixed array by querying all
		 * spatial boundaries.
		 */
		this._environment.updateWellMixed();
		/*
		 * Set up the relevant arrays in each of our solute grids: diffusivity 
		 * & well-mixed need only be done once each process manager time step,
		 * but production rate must be reset every time the PDE updater method
		 * is called.
		 */
		for ( String soluteName : this._soluteNames )
		{
			SpatialGrid solute = this._environment.getSoluteGrid(soluteName);
			// TODO use diffusivitySetter
			solute.newArray(DIFFUSIVITY, this._diffusivity.get(soluteName));
		}
		/*
		 * Solve the PDEs of diffusion and reaction.
		 */
		this._solver.solve(this._environment.getSolutes(),
				this._environment.getCommonGrid(), this._timeStepSize);
		/*
		 * If any mass has flowed in or out of the well-mixed region,
		 * distribute it among the relevant boundaries.
		 */
		this._environment.distributeWellMixedFlows();
		/*
		 * Clear agent mass distribution maps: this prevents unneeded clutter
		 * in XML output.
		 */
		for ( Agent a : this._agents.getAllLocatedAgents() )
			a.reg().remove(VOLUME_DISTRIBUTION_MAP);
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
			public void prestep(Collection<SpatialGrid> variables, 
					double dt)
			{
				for ( SpatialGrid var : variables )
					var.newArray(PRODUCTIONRATE);
				applyEnvReactions();
				for ( Agent agent : _agents.getAllLocatedAgents() )
					applyAgentReactions(agent, dt);
			}
		};
	}
	
	/**
	 * \brief Iterate over all solute grids, applying any reactions that occur
	 * in the environment to the grids' {@code PRODUCTIONRATE} arrays.
	 * 
	 * @param environment The environment container of a {@code Compartment}.
	 */
	private void applyEnvReactions()
	{
		Tier level = BULK;
		if ( Log.shouldWrite(level) )
			Log.out(level, "Applying environmental reactions");
		Collection<Reaction> reactions = this._environment.getReactions();
		if ( reactions.isEmpty() )
		{
			Log.out(level, "No reactions to apply, skipping");
			return;
		}
		/*
		 * Construct the "concns" dictionary once, so that we don't have to
		 * re-enter the solute names for every voxel coordinate.
		 */
		Collection<String> soluteNames = this._environment.getSoluteNames();
		HashMap<String,Double> concns = new HashMap<String,Double>();
		for ( String soluteName : soluteNames )
			concns.put(soluteName, 0.0);
		/*
		 * The "totals" dictionary is for reporting only.
		 */
		HashMap<String,Double> totals = new HashMap<String,Double>();
		for ( String name : soluteNames )
			totals.put(name, 0.0);
		/*
		 * Iterate over the spatial discretization of the environment, applying
		 * extracellular reactions as required.
		 */
		Shape shape = this._environment.getShape();
		SpatialGrid solute;
		Set<String> productNames;
		double rate, productRate;
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext() )
		{
			/* Get the solute concentrations in this grid voxel. */
			for ( String soluteName : soluteNames )
			{
				solute = this._environment.getSoluteGrid(soluteName);
				concns.put(soluteName, solute.getValueAt(CONCN, coord));
			}	
			/* Iterate over each compartment reactions. */
			for ( Reaction r : reactions )
			{
				rate = r.getRate(concns);
				productNames = r.getStoichiometry().keySet();
				/* Write rate for each product to grid. */
				// TODO verify that all environmental reactions have
				// only solute products, so we don't have to check here.
				for ( String product : productNames )
					if ( this._environment.isSoluteName(product) )
					{
						productRate = rate * r.getStoichiometry(product);
						solute = this._environment.getSoluteGrid(product);
						solute.addValueAt(PRODUCTIONRATE, coord, productRate);
						totals.put(product, totals.get(product) + productRate);
					}
			}
		}
		if ( Log.shouldWrite(level) )
		{
			for ( String name : soluteNames )
				Log.out(level, "  total "+name+" produced: "+totals.get(name));
			Log.out(level, "Finished applying environmental reactions");
		}
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
			AgentContainer.updateAgentMass(agent, newBiomass);
		}
	}
}
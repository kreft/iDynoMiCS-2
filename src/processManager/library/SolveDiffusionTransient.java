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
import java.util.Set;
import java.util.function.Predicate;
import org.w3c.dom.Element;

import agent.Agent;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlRef;
import grid.SpatialGrid;
import grid.wellmixedSetter.AllSameMixing;
import grid.wellmixedSetter.IsWellmixedSetter;
import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import processManager.ProcessManager;
import reaction.Reaction;
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
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
	 * Dictionary of well-mixed setters: one for each solute.
	 */
	// TODO this is probably the wrong approach: we should have the same setter
	// for all solutes
	protected HashMap<String,IsWellmixedSetter> _wellmixed;
	/**
	 * TODO 
	 */
	// TODO replace with diffusivitySetter
	protected HashMap<String,Double> _diffusivity;
	
	/**
	 * TODO
	 */
	private Compartment _compartment;
	
	public String SOLUTES = AspectRef.soluteNames;

	
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	@Override
	public void init(Element xmlElem, Compartment compartment)
	{
		super.init(xmlElem, compartment);
		this._compartment = compartment;
		this.init();
	}
	
	/**
	 * \brief Initialise this diffusion-reaction process manager with a list of
	 * solutes it is responsible for.
	 * 
	 * @param soluteNames The list of solutes this is responsible for.
	 */
	public void init()
	{
		this._soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(this._compartment.environment.getSoluteNames()));
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
		this._diffusivity = new HashMap<String,Double>();
		for ( String sName : _soluteNames )
			this._diffusivity.put(sName, 1.0);
		String msg = "SolveDiffusionTransient responsible for solutes: ";
		for ( String s : this._soluteNames )
			msg += s + ", ";
		Log.out(Tier.EXPRESSIVE, msg);
	}
	
	/*************************************************************************
	 * STEPPING
	 ************************************************************************/
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		/*
		 * Set up the agent mass distribution maps, to ensure that agent
		 * reactions are spread over voxels appropriately.
		 */
		agents.setupAgentDistributionMaps();
		/*
		 * Set up the relevant arrays in each of our solute grids: diffusivity 
		 * & well-mixed need only be done once each process manager time step,
		 * but production rate must be reset every time the PDE updater method
		 * is called.
		 */
		for ( String soluteName : this._soluteNames )
		{
			SpatialGrid solute = environment.getSoluteGrid(soluteName);
			// TODO use diffusivitySetter
			solute.newArray(DIFFUSIVITY, this._diffusivity.get(soluteName));
			this._wellmixed.get(soluteName).updateWellmixed(solute, agents);
		}
		/*
		 * Set the updater method and solve.
		 */
		this._solver.setUpdater(standardUpdater(environment, agents));
		this._solver.solve(environment.getSolutes(), this._timeStepSize);
		
		/*
		 * clear distribution maps, prevent unneeded clutter in xml output
		 */
		for ( Agent a : agents.getAllLocatedAgents() )
			a.reg().remove(VOLUME_DISTRIBUTION_MAP);
	}
	
	/*************************************************************************
	 * INTERNAL METHODS
	 ************************************************************************/
	
	/**
	 * \brief The standard PDE updater method resets the solute
	 * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
	 * {@code Agent}s to grow.
	 * 
	 * @param environment The environment of a {@code Compartment}.
	 * @param agents The agents of a {@code Compartment}.
	 * @return PDE updater method.
	 */
	private static PDEupdater standardUpdater(
					EnvironmentContainer environment, AgentContainer agents)
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
				applyEnvReactions(environment);
				applyAgentReactions(environment, agents);
				/* Ask all agents to grow. */
				for ( Agent a : agents.getAllLocatedAgents() )
				{
					a.event(AspectRef.growth, dt);
					a.event(AspectRef.internalProduction, dt);
				}
			}
		};
	}
	
	/**
	 * \brief Iterate over all solute grids, applying any reactions that occur
	 * in the environment to the grids' {@code PRODUCTIONRATE} arrays.
	 * 
	 * @param environment The environment container of a {@code Compartment}.
	 */
	private static void applyEnvReactions(EnvironmentContainer environment)
	{
		Tier level = BULK;
		Log.out(level, "Applying environmental reactions");
		Collection<Reaction> reactions = environment.getReactions();
		if ( reactions.isEmpty() )
		{
			Log.out(level, "No reactions to apply, skipping");
			return;
		}
		/*
		 * Construct the "concns" dictionary once, so that we don't have to
		 * re-enter the solute names for every voxel coordinate.
		 */
		Collection<String> soluteNames = environment.getSoluteNames();
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
		Shape shape = environment.getShape();
		SpatialGrid solute;
		Set<String> productNames;
		double rate, productRate;
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext() )
		{
			/* Get the solute concentrations in this grid voxel. */
			for ( String soluteName : soluteNames )
			{
				solute = environment.getSoluteGrid(soluteName);
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
					if ( environment.isSoluteName(product) )
					{
						productRate = rate * r.getStoichiometry(product);
						solute = environment.getSoluteGrid(product);
						solute.addValueAt(PRODUCTIONRATE, coord, productRate);
						totals.put(product, totals.get(product) + productRate);
					}
			}
		}
		for ( String name : soluteNames )
			Log.out(level, "  total "+name+" produced: "+totals.get(name));
		Log.out(level, "Finished applying environmental reactions");
	}
	
	/**
	 * \brief 
	 * 
	 * <p><b>Note</b>: this method assumes that the volume distribution maps
	 * of all relevant agents have already been calculated. This is typically
	 * done just once per process manager step, rather than at every PDE solver
	 * mini-timestep.</p>
	 * 
	 * @param environment
	 * @param agents
	 */
	@SuppressWarnings("unchecked")
	private static void applyAgentReactions(
			EnvironmentContainer environment, AgentContainer agents)
	{
		Tier level = BULK;
		Log.out(level, "Applying agent reactions");
		SpatialGrid solute;
		HashMap<String,Double> concns = new HashMap<String,Double>();
		HashMap<String,Double> totals = new HashMap<String,Double>();
		/*
		 * Loop over all agents, applying their reactions to the
		 * relevant solute grids, in the voxels calculated before the 
		 * updater method was set.
		 */
		List<Reaction> reactions;
		CoordinateMap distributionMap;
		List<Agent> agentList = agents.getAllLocatedAgents();
		agentList.removeIf(NO_REAC_FILTER);
		HashMap<String,Double> internalProdctn;
		for ( Agent a : agentList )
		{
			reactions = (List<Reaction>) a.getValue(XmlRef.reactions);
			distributionMap = (CoordinateMap) a.getValue(VOLUME_DISTRIBUTION_MAP);
			a.set(GROWTH_RATE, 0.0);
			if ( a.isAspect(INTERNAL_PRODUCTION_RATE) )
			{
				internalProdctn = (HashMap<String,Double>) a.getValue(INTERNAL_PRODUCTION_RATE);
				for (String key : internalProdctn.keySet())
					internalProdctn.put(key, 0.0);
			}
			/*
			 * Scale the distribution map so that its contents sum up to one.
			 */
			distributionMap.scale();
			/*
			 * Now look at all the voxels this agent covers.
			 */
			double concn, rate, productionRate;
			for ( int[] coord : distributionMap.keySet() )
			{
				for ( Reaction r : reactions )
				{
					/* 
					 * Build the dictionary of variable values. Note
					 * that these will likely overlap with the names in
					 * the reaction stoichiometry (handled after the
					 * reaction rate), but will not always be the same.
					 * Here we are interested in those that affect the
					 * reaction, and not those that are affected by it.
					 */
					concns.clear();
					for ( String varName : r.getVariableNames() )
					{
						if ( environment.isSoluteName(varName) )
						{
							solute = environment.getSoluteGrid(varName);
							concn = solute.getValueAt(CONCN, coord);
						}
						else if ( a.isAspect(varName) )
						{
							// TODO divide by the voxel volume here?
							concn = a.getDouble(varName); 
							concn *= distributionMap.get(coord);
						}
						else
						{
							// TODO safety?
							concn = 0.0;
						}
						concns.put(varName, concn);
					}
					/*
					 * Calculate the reaction rate based on the 
					 * variables just retrieved.
					 */
					rate = r.getRate(concns);
					/* 
					 * Now that we have the reaction rate, we can 
					 * distribute the effects of the reaction. Note
					 * again that the names in the stoichiometry may
					 * not be the same as those in the reaction
					 * variables (although there is likely to be a
					 * large overlap).
					 */
					for ( String productName : r.getStoichiometry().keySet())
					{
						productionRate = rate * r.getStoichiometry(productName);
						if ( ! totals.containsKey(productName) )
							totals.put(productName, 0.0);
						totals.put(productName, totals.get(productName) + productionRate);
						if ( environment.isSoluteName(productName) )
						{
							solute = environment.getSoluteGrid(productName);
							solute.addValueAt(PRODUCTIONRATE, 
									coord, productionRate);
						}
						else if ( 
							a.getString(XmlRef.species).equals(productName) )
						{
							double curRate = a.getDouble(GROWTH_RATE);
							a.set(GROWTH_RATE, curRate + productionRate * 
									distributionMap.get(coord));
						}
						else if ( a.isAspect(INTERNAL_PRODUCTION_RATE) )
						{
							internalProdctn = 
									(HashMap<String,Double>) a.getValue(INTERNAL_PRODUCTION_RATE);
							double curRate = productionRate * 
													distributionMap.get(coord);
							if ( internalProdctn.containsKey(productName) )
								curRate += internalProdctn.get(productName);
							internalProdctn.put(productName, curRate);
						} 
						else
						{
							System.out.println("agent reaction catched " + 
									productName);
							// TODO safety?
						}
					}
				}
			// TODO agent do event "internal production"?
			}
		}
		for ( String name : totals.keySet() )
			Log.out(level, "   total \""+name+"\" produced: "+totals.get(name));
		Log.out(level, "Finished applying agent reactions");
	}
}
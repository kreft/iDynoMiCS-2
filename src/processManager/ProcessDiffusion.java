package processManager;

import static dataIO.Log.Tier.BULK;
import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.w3c.dom.Element;

import agent.Agent;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import generalInterfaces.Instantiatable;
import grid.SpatialGrid;
import grid.diffusivitySetter.IsDiffusivitySetter;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import solver.PDEsolver;
import utility.Helper;

/**
 * \brief Abstract superclass for process managers solving diffusion-reaction
 * systems in a spatial Compartment.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class ProcessDiffusion extends ProcessManager
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
	protected static String VOLUME_DISTRIBUTION_MAP =
			AspectRef.agentVolumeDistributionMap;
	/**
	 * Instance of a subclass of {@code PDEsolver}, e.g. {@code PDEexplicit}.
	 */
	protected PDEsolver _solver;
	/**
	 * The names of all solutes this solver is responsible for.
	 */
	protected String[] _soluteNames;
	/**
	 * Diffusivity setter for each solute present.
	 */
	protected Map<String,IsDiffusivitySetter> _diffusivity= new HashMap<>();
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
		/*
		 * Now look for diffusivity setters.
		 */
		Collection<Element> diffusivityElements =
				XmlHandler.getElements(xmlElem, XmlRef.diffusivitySetter);
		for ( Element dElem : diffusivityElements )
		{
			String soluteName = dElem.getAttribute(XmlRef.solute);
			String className = dElem.getAttribute(XmlRef.classAttribute);
			IsDiffusivitySetter diffusivity = (IsDiffusivitySetter)
					Instantiatable.getNewInstance(className, dElem, this);
			this._diffusivity.put(soluteName, diffusivity);
		}
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
		this.init( soluteNames, environment, agents, compartmentName );
	}
	
	public abstract void init( String[] soluteNames,
			EnvironmentContainer environment, 
			AgentContainer agents,
			String compartmentName);

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
			IsDiffusivitySetter setter = this._diffusivity.get(soluteName);
			setter.updateDiffusivity(solute, this._environment, this._agents);
		}
		/*
		 * Solve the PDEs of diffusion and reaction.
		 */
		this._solver.solve(this._environment.getSolutes(),
				this._environment.getCommonGrid(), this._timeStepSize);
		/*
		 * Note that sub-classes may have methods after to allocate mass and
		 * tidy up 
		 */
	}
	
	/* ***********************************************************************
	 * INTERNAL METHODS
	 * **********************************************************************/
	
	/**
	 * \brief Iterate over all solute grids, applying any reactions that occur
	 * in the environment to the grids' {@code PRODUCTIONRATE} arrays.
	 * 
	 * @param environment The environment container of a {@code Compartment}.
	 */
	protected void applyEnvReactions()
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
}

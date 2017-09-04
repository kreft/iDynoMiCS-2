package processManager;

import static dataIO.Log.Tier.BULK;
import static dataIO.Log.Tier.DEBUG;
import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.w3c.dom.Element;

import agent.Agent;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.diffusivitySetter.IsDiffusivitySetter;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import instantiable.Instance;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.CartesianShape;
import shape.Shape;
import shape.subvoxel.CoordinateMap;
import shape.subvoxel.SubvoxelPoint;
import solver.PDEsolver;
import surface.Collision;
import surface.Surface;

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

	public String DIVIDE = AspectRef.agentDivision;
	
	public String UPDATE_BODY = AspectRef.bodyUpdate;
	public String EXCRETE_EPS = AspectRef.agentExcreteEps;
	/**
	 * Aspect name for the {@code coordinateMap} used for establishing which
	 * voxels a located {@code Agent} covers.
	 */
	private static final String VD_TAG = AspectRef.agentVolumeDistributionMap;
	/**
	 * When choosing an appropriate sub-voxel resolution for building agents'
	 * {@code coordinateMap}s, the smallest agent radius is multiplied by this
	 * factor to ensure it is fine enough.
	 */
	// NOTE the value of a quarter is chosen arbitrarily
	private static double SUBGRID_FACTOR = 0.25; //TODO set from aspect?
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	

	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
		
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
					Instance.getNew(dElem, this, className);
			this._diffusivity.put(soluteName, diffusivity);
		}
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
		this.setupAgentDistributionMaps();
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
	
	/**
	 * perform final clean-up and update agents to represent updated situation.
	 */
	protected void postStep()
	{
		/*
		 * Clear agent mass distribution maps.
		 */
		this.removeAgentDistibutionMaps();
		/**
		 * act upon new agent situations
		 */
		for(Agent agent: this._agents.getAllAgents()) 
		{
			agent.event(DIVIDE);
			agent.event(EXCRETE_EPS);
			agent.event(UPDATE_BODY);
		}
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
	protected void applyEnvReactions(Collection<SpatialGrid> solutes)
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
		 * Iterate over the spatial discretization of the environment,
		 * applying extracellular reactions as required.
		 */
		Shape shape = solutes.iterator().next().getShape();
		Set<String> productNames;
		double rate, productRate;
		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext() )
		{
			/* Get the solute concentrations in this grid voxel. */
			for ( SpatialGrid soluteGrid : solutes )
			{
				concns.put(soluteGrid.getName(),
						soluteGrid.getValueAt(CONCN, coord));
			}
			/* Iterate over each compartment reactions. */
			for ( Reaction r : reactions )
			{
				rate = r.getRate(concns);
				productNames = r.getStoichiometry().keySet();
				/* Write rate for each product to grid. */
				for ( String product : productNames )
					for ( SpatialGrid soluteGrid : solutes )
						if ( product.equals(soluteGrid.getName()) )
						{
							productRate = rate * r.getStoichiometry(product);
							soluteGrid.addValueAt(PRODUCTIONRATE,
									coord, productRate);
							totals.put(product,
									totals.get(product) + productRate);
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
	

	/* ***********************************************************************
	 * AGENT MASS DISTRIBUTION
	 * **********************************************************************/

	/**
	 * \brief Loop through all located {@code Agent}s with reactions,
	 * estimating how much of their body overlaps with nearby grid voxels.
	 * 
	 * @see #removeAgentDistibutionMaps()
	 */
	@SuppressWarnings("unchecked")
	public void setupAgentDistributionMaps()
	{
		Tier level = BULK;
		if (Log.shouldWrite(level))
			Log.out(level, "Setting up agent distribution maps");
		
		Shape shape = this._agents.getShape();
		int nDim = this._agents.getNumDims();
		
		/*
		 * Reset the agent biomass distribution maps.
		 */
		Map<Shape, CoordinateMap> mapOfMaps;
		for ( Agent a : this._agents.getAllLocatedAgents() )
		{
			if ( a.isAspect(VD_TAG) )
				mapOfMaps = (Map<Shape, CoordinateMap>)a.get(VD_TAG);
			else
				mapOfMaps = new HashMap<Shape, CoordinateMap>();
			mapOfMaps.put(shape, new CoordinateMap());
			a.set(VD_TAG, mapOfMaps);
		}
		/*
		 * Now fill these agent biomass distribution maps.
		 */
		double[] location;
		double[] dimension = new double[3];
		double[] sides;
		Collection<SubvoxelPoint> svPoints;
		List<Agent> nhbs;
		List<Surface> surfaces;
		double[] pLoc;
		Collision collision = new Collision(null, shape);
		CoordinateMap distributionMap;

		for ( int[] coord = shape.resetIterator(); 
				shape.isIteratorValid(); coord = shape.iteratorNext())
		{
			double minRad;
			
			if( shape instanceof CartesianShape)
			{
				/* Find all agents that overlap with this voxel. */
				// TODO a method for getting a voxel's bounding box directly?
				location = Vector.subset(shape.getVoxelOrigin(coord), nDim);
				shape.getVoxelSideLengthsTo(dimension, coord); //FIXME returns arc lengths with polar coords
				// FIXME create a bounding box that always captures at least the complete voxel
				sides = Vector.subset(dimension, nDim);
				/* NOTE the agent tree is always the amount of actual dimension */
				nhbs = this._agents.treeSearch(location, sides);
				
				/* used later to find subgridpoint scale */
				minRad = Vector.min(sides);
			}
			else
			{
				/* TODO since the previous does not work at all for polar */
				nhbs = this._agents.getAllLocatedAgents();
				
				/* FIXME total mess, trying to get towards something that at
				 * least makes some sence
				 */
				shape.getVoxelSideLengthsTo(dimension, coord); //FIXME returns arc lengths with polar coords
				// FIXME create a bounding box that always captures at least the complete voxel
				sides = Vector.subset(dimension, nDim);
				// FIXME because it does not make any sence to use the ark, try the biggest (probably the R dimension) and half that to be safe.
				minRad = Vector.max(sides) / 2.0; 
			}
			/* Filter the agents for those with reactions, radius & surface. */
			nhbs.removeIf(NO_REAC_FILTER);
			nhbs.removeIf(NO_BODY_FILTER);
			/* If there are none, move onto the next voxel. */
			if ( nhbs.isEmpty() )
				continue;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  "+nhbs.size()+" agents overlap with coord "+
					Vector.toString(coord));
			}
			
			/* 
			 * Find the sub-voxel resolution from the smallest agent, and
			 * get the list of sub-voxel points.
			 */
			
			double radius;
			for ( Agent a : nhbs )
			{
				radius = a.getDouble(AspectRef.bodyRadius);
				Log.out(level, "   agent "+a.identity()+" has radius "+radius);
				minRad = Math.min(radius, minRad);
			}
			minRad *= SUBGRID_FACTOR;
			svPoints = shape.getCurrentSubvoxelPoints(minRad);
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "  using a min radius of "+minRad);
				Log.out(level, "  gives "+svPoints.size()+" sub-voxel points");
			}
			/* Get the sub-voxel points and query the agents. */
			for ( Agent a : nhbs )
			{
				/* Should have been removed, but doesn't hurt to check. */
				if ( ! a.isAspect(AspectRef.agentReactions) )
					continue;
				if ( ! a.isAspect(AspectRef.surfaceList) )
					continue;
				surfaces = (List<Surface>) a.get(AspectRef.surfaceList);
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, "  "+"   agent "+a.identity()+" has "+
						surfaces.size()+" surfaces");
				}
				mapOfMaps = (Map<Shape, CoordinateMap>) a.getValue(VD_TAG);
				distributionMap = mapOfMaps.get(shape);
				sgLoop: for ( SubvoxelPoint p : svPoints )
				{
					/* Only give location in significant dimensions. */
					pLoc = p.getRealLocation(nDim);
					for ( Surface s : surfaces )
						if ( collision.distance(s, pLoc) < 0.0 )
						{
							distributionMap.increase(coord, p.volume);
							/*
							 * We only want to count this point once, even
							 * if other surfaces of the same agent hit it.
							 */
							continue sgLoop;
						}
				}
			}
		}
		Log.out(level, "Finished setting up agent distribution maps");
	}
	
	/**
	 * \brief Loop through all located {@code Agents}, removing their mass
	 * distribution maps.
	 * 
	 * <p>This prevents unneeded clutter in XML output.</p>
	 * 
	 * @see #setupAgentDistributionMaps()
	 */
	public void removeAgentDistibutionMaps()
	{
		for ( Agent a : this._agents.getAllLocatedAgents() )
			a.reg().remove(VD_TAG);
	}
}

package processManager;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import boundary.Boundary;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import idynomics.Global;
import linearAlgebra.Vector;
import reaction.Reaction;
import reaction.RegularReaction;
import referenceLibrary.AspectRef;
import shape.CartesianShape;
import shape.Shape;
import shape.subvoxel.IntegerArray;
import shape.subvoxel.SubvoxelPoint;
import solver.PDEsolver;
import solver.PDEupdater;
import surface.Surface;
import surface.Voxel;
import surface.collision.Collision;
import surface.collision.CollisionUtilities;
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
	 * TODO
	 */
	public String SOLUTES = AspectRef.soluteNames;

	public String DIVIDE = AspectRef.agentDivision;
	
	public String UPDATE_BODY = AspectRef.bodyUpdate;
	public String EXCRETE_EPS = AspectRef.agentExcreteEps;
	
	public String DIFFERENTIATE = AspectRef.agentDifferentiation;
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
	
	/**
	 * enable fast agent distribution
	 */
	boolean _fastDistribution = Global.fastAgentDistribution;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	

	@Override
	public void init(Element xmlElem, EnvironmentContainer environment, 
			AgentContainer agents, String compartmentName)
	{
		super.init(xmlElem, environment, agents, compartmentName);
	}
	
	/* ***********************************************************************
	 * STEPPING
	 * **********************************************************************/
	
	@Override
	protected void internalStep()
	{
		
		for ( Boundary b : this._environment.getShape().getAllBoundaries() )
		{
			b.resetMassFlowRates();
		}
		/* gets specific solutes from process manager aspect registry if they
		 * are defined, if not, solve for all solutes.
		 */
		this._soluteNames = (String[]) this.getOr(SOLUTES, 
				Helper.collectionToArray(this._environment.getSoluteNames()));

		/* NOTE we want to include solutes that have been added by the user
		 * after the protocol was loaded.
		 */
		this._solver.init(this._soluteNames, false);
		this._solver.setUpdater(this.standardUpdater());		
		/*
		 * Set up the agent mass distribution maps, to ensure that agent
		 * reactions are spread over voxels appropriately.
		 */
		Collection<Shape> shapes = 
				this._solver.getShapesForAgentMassDistributionMaps(
						this._environment.getCommonGrid());
		Shape shape = shapes.iterator().next();
		this.setupAgentDistributionMaps(shape);
		this.copyAgentDistributionMaps(shapes, shape);
		
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
			solute.updateDiffusivity(this._environment, this._agents);
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

	protected abstract PDEupdater standardUpdater();
	
	 /*
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
			agent.event(DIFFERENTIATE);
			
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
		Collection<RegularReaction> reactions = this._environment.getReactions();
		if ( reactions.isEmpty() )
		{
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
		double productRate;
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
				/* Write rate for each product to grid. */
				for ( String product : r.getReactantNames() )
					for ( SpatialGrid soluteGrid : solutes )
						if ( product.equals(soluteGrid.getName()) )
						{
							productRate = r.getProductionRate( concns, product);
							soluteGrid.addValueAt(PRODUCTIONRATE,
									coord, productRate);
							totals.put(product,
									totals.get(product) + productRate);
						}
			}
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
	public void setupAgentDistributionMaps(Shape shape)
	{
		int nDim = this._agents.getNumDims();
		
		/*
		 * Reset the agent biomass distribution maps.
		 */
		Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps;
		for ( Agent a : this._agents.getAllLocatedAgents() )
		{
			if ( a.isAspect(VD_TAG) )
				mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>)a.get(VD_TAG);
			else
				mapOfMaps = new HashMap<Shape, HashMap<IntegerArray,Double>>();
			mapOfMaps.put(shape, new HashMap<IntegerArray,Double>());
			a.set(VD_TAG, mapOfMaps);
		}

		HashMap<IntegerArray,Double> distributionMap;
		
		if ( !this._fastDistribution )
		{
			/*
			 * Now fill these agent biomass distribution maps.
			 */
			double[] location;
			double[] dimension = new double[3];
			double[] sides;
			double[] upper;
			Collection<SubvoxelPoint> svPoints;
			List<Agent> nhbs;
			List<Surface> surfaces;
			double[] pLoc;
			Collision collision = new Collision(null, null, shape);

			
			for ( int[] coord = shape.resetIterator(); 
					shape.isIteratorValid(); coord = shape.iteratorNext())
			{
				double minRad;
				IntegerArray coordArray = new IntegerArray(new int[]{coord[0],coord[1],coord[2]});
				
				if( shape instanceof CartesianShape)
				{
					/* Find all agents that overlap with this voxel. */
					// TODO a method for getting a voxel's bounding box directly?
					location = Vector.subset(shape.getVoxelOrigin(coord), nDim);
					shape.getVoxelSideLengthsTo(dimension, coord); //FIXME returns arc lengths with polar coords
					// FIXME create a bounding box that always captures at least the complete voxel
					sides = Vector.subset(dimension, nDim);
					upper = Vector.add(location, sides);
					
					Voxel vox = new Voxel(location, upper);
					vox.init(shape.getCollision());
					/* NOTE the agent tree is always the amount of actual dimension */
					nhbs = CollisionUtilities.getCollidingAgents(
							vox, this._agents.treeSearch( location, upper ) );
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
				/* 
				 * Find the sub-voxel resolution from the smallest agent, and
				 * get the list of sub-voxel points.
				 */
				
				double radius;
				for ( Agent a : nhbs )
				{
					radius = a.getDouble(AspectRef.bodyRadius);
					minRad = Math.min(radius, minRad);
				}
				minRad *= SUBGRID_FACTOR;
				svPoints = shape.getCurrentSubvoxelPoints(minRad);
				if ( Log.shouldWrite(Tier.DEBUG) )
				{
					Log.out(Tier.DEBUG, "using a min radius of "+minRad);
					Log.out(Tier.DEBUG, "gives "+svPoints.size()+" sub-voxel points");
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
				mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>) a.getValue(VD_TAG);
				distributionMap = mapOfMaps.get(shape);
					/*
					 * FIXME this should really only evaluate collisions with local
					 * subgridpoints rather than all subgrid points in the domain.
					 * With a rib length of 0.25 * radius_smallest_agent this can
					 * result in 10^8 - 10^10 or more evaluations per agent!!
					 */
					sgLoop: for ( SubvoxelPoint p : svPoints )
					{
						/* Only give location in significant dimensions. */
						pLoc = p.getRealLocation(nDim);
						for ( Surface s : surfaces )
							if ( collision.distance(s, pLoc) < 0.0 )
							{
								if( distributionMap.containsKey(coordArray))
									distributionMap.put(coordArray, distributionMap.get(coordArray)+p.volume);
								else
									distributionMap.put(coordArray, p.volume);
								/*
								 * We only want to count this point once, even
								 * if other surfaces of the same agent hit it.
								 */
								continue sgLoop;
							}
					}
				
				}
			}
			for ( Agent a : this._agents.getAllLocatedAgents() )
			{
				if ( a.isAspect(VD_TAG) )
				{
					mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>) a.getValue(VD_TAG);
					distributionMap = mapOfMaps.get(shape);
					ProcessDiffusion.scale(distributionMap, 1.0);
				}
			}
		}
		else
		{
			for ( Agent a : this._agents.getAllLocatedAgents() )
			{
				IntegerArray coordArray = new IntegerArray( 
						shape.getCoords(((Body) a.get(AspectRef.agentBody)).getCenter()));
				mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>) a.getValue(VD_TAG);
				distributionMap = mapOfMaps.get(shape);
				distributionMap.put(coordArray, 1.0);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void copyAgentDistributionMaps(Collection<Shape> shapes, Shape finest)
	{
		// Skip for transient solver
		if (shapes.size() == 1)
			return;
		for (Shape shape : shapes)
		{
			// Already solved for finest so skip
			if (shape.equals(finest))
				continue;
			Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps;
			HashMap<IntegerArray,Double> distributionMap, finestDistributionMap;
			for ( Agent a : this._agents.getAllLocatedAgents() )
			{
				// For agents with no reactions or body, skip
				if ( ! a.isAspect(AspectRef.agentReactions) )
					continue;
				if ( ! a.isAspect(AspectRef.surfaceList) )
					continue;
				// Should have this set, but doesn't hurt to check
				if ( a.isAspect(VD_TAG) )
					mapOfMaps = (Map<Shape, HashMap<IntegerArray,Double>>)a.get(VD_TAG);
				else
					continue;
				mapOfMaps.put(shape, new HashMap<IntegerArray,Double>());
				a.set(VD_TAG, mapOfMaps);
				// distribution map for the current shape with nVoxels > finest
				distributionMap = mapOfMaps.get(shape);
				// distribution map of the finest grid. 
				// Should have values, which we will use to update the coarser grids.
				finestDistributionMap = mapOfMaps.get(finest);
				Collection<IntegerArray> coordsWithValues = finestDistributionMap.keySet();
				for ( IntegerArray coord : coordsWithValues )
				{
					// Calculate the global location of the coordinates in the distribution map of finest grid
					double[] globalLocVoxel = finest.getGlobalLocation(finest.getVoxelOrigin(coord.get()));
					// Get the coordinates for the current grid
					IntegerArray coordInCurrentShape = new IntegerArray(shape.getCoords(shape.getLocalPosition(globalLocVoxel)));
					// Increase the volume of the current coordinates using the volume from the finest grid.
					// This should ensure that each point on the finest grid inside the current voxel, gets added to the voxel origin.
					if( distributionMap.containsKey(coordInCurrentShape))
						distributionMap.put(coordInCurrentShape, distributionMap.get(coordInCurrentShape)+finestDistributionMap.get(coord));
					else
						distributionMap.put(coordInCurrentShape, finestDistributionMap.get(coord));
				}
			}
		}
	}
	
	
	public static void scale(HashMap<IntegerArray,Double> coordMap, double newTotal)
	{
		/* Find the multiplier, taking care not to divide by zero. */
		double multiplier = 0.0;
		for ( Double value : coordMap.values() )
			multiplier += value;
		if ( multiplier == 0.0 )
			return;
		multiplier = newTotal / multiplier;
		/* Now apply the multiplier. */
		for ( IntegerArray key : coordMap.keySet() )
			coordMap.put(key, coordMap.get(key) * multiplier);
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

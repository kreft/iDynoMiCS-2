package compartment;

import static grid.ArrayType.CONCN;
import static grid.ArrayType.WELLMIXED;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import boundary.Boundary;
import boundary.SpatialBoundary;
import boundary.WellMixedBoundary;
import dataIO.Log;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import grid.SpatialGrid;
import grid.WellMixedConstants;
import idynomics.Idynomics;
import instantiable.object.InstantiableList;
import reaction.Reaction;
import reaction.RegularReaction;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import shape.Shape;

/**
 * \brief Manages the solutes in a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class EnvironmentContainer implements CanPrelaunchCheck, Settable
{
	/**
	 * This dictates both geometry and size, and it inherited from the
	 * {@code Compartment} this {@code EnvrionmentContainer} belongs to.
	 */
	protected Shape _shape;
	/**
	 * Collection of solutes (each SpatialGrid knows its own name).
	 */
	protected Collection<SpatialGrid> _solutes = new LinkedList<SpatialGrid>();
	/**
	 * Collection of extracellular reactions specific to this compartment
	 * (each Reaction knows its own name).
	 */
	protected InstantiableList<RegularReaction> _reactions = new InstantiableList<RegularReaction>(RegularReaction.class, null, XmlRef.reactions, XmlRef.reaction);
	/**
	 * Name of the common grid.
	 */
	public final static String COMMON_GRID_NAME = "common";
	/**
	 * Grid of common attributes, such as well-mixed.
	 */
	protected SpatialGrid _commonGrid;
	private Settable _parentNode;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * \brief Construct an {@code EnvironmentContainer} from a {@code Shape}.
	 * 
	 * @param aShape {@code Shape} object (see shape.ShapeLibrary).
	 */
	public EnvironmentContainer(Shape aShape)
	{
		this._shape = aShape;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param solute
	 */
	public void addSolute(SpatialGrid solute)
	{
		if (this.getSoluteNames().contains(solute.getName()))
		{
			if( Log.shouldWrite(Tier.CRITICAL))
				Log.out(Tier.CRITICAL, 
						"Warning: Two or more solute grids with same name \""+
								solute.getName()+"\"");
		}
		this._solutes.add(solute);
		if( Log.shouldWrite(Tier.EXPRESSIVE))
			Log.out(Tier.EXPRESSIVE, 
					"Added solute \""+ solute.getName() +"\" to environment");
	}
	
	/**
	 * \brief Add a new solute to the environment. This method is intended only
	 * for testing.
	 * 
	 * @param soluteName Name of the new solute.
	 * @param initialConcn Initial value for the concentration of this solute.
	 */
	public void addSolute(String soluteName, double initialConcn)
	{
		SpatialGrid sg = new SpatialGrid(this._shape, soluteName, this);
		sg.newArray(CONCN, initialConcn);
		this.addSolute(sg);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param spatialGrid
	 */
	public void deleteSolute(Object spatialGrid)
	{
		this._solutes.remove(spatialGrid);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param Reaction
	 */
	public void deleteReaction(Object Reaction)
	{
		this._reactions.remove(Reaction);
	}

	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	/**
	 * @return The shape of this compartment.
	 */
	public Shape getShape()
	{
		return this._shape;
	}
	
	/**
	 * @return The names of all solutes present in this compartment.
	 */
	public Collection<String> getSoluteNames()
	{
		Collection<String> out = new LinkedList<String>();
		for ( SpatialGrid sg : this._solutes )
			out.add(sg.getName());
		return out;
	}
	
	/**
	 * \brief Get the spatial grid for a solute.
	 * 
	 * <p>Returns null if this solute is not present.</p>
	 * 
	 * @param soluteName Name of a solute.
	 * @return The spatial grid object representing this solute.
	 */
	public SpatialGrid getSoluteGrid(String soluteName)
	{
		for ( SpatialGrid sg : this._solutes )
			if ( sg.getName().equals(soluteName) )
				return sg;
		Log.out(Tier.CRITICAL,
				"EnvironmentContainer can't find grid for \""+soluteName+"\"");
		return null;
	}
	
	/**
	 * \brief Checks if this compartment has a solute with the name given.
	 * 
	 * @param name Name of a potential solute.
	 * @return {@code true} if there is a solute with this name in this
	 * compartment, {@code false} if there is not.
	 */
	public boolean isSoluteName(String name)
	{
		for ( SpatialGrid sg : this._solutes )
			if ( sg.getName().equals(name) )
				return true;
		return false;
	}
	
	/**
	 * @return The SpatialGrid representation for every solute present in this
	 * compartment.
	 */
	public Collection<SpatialGrid> getSolutes()
	{
		return this._solutes;
	}
	
	/**
	 * @return All of this {@code Compartment}'s extracellular reactions.
	 */
	public Collection<RegularReaction> getReactions()
	{
		Collection<RegularReaction> out = new LinkedList<RegularReaction>();
		out.addAll(this._reactions);
		return this._reactions;
	}
	
	/**
	 * \brief Get a specific reaction by name.
	 * 
	 * <p>Returns null if this reaction is unknown here.</p>
	 * 
	 * @param name Name of the reaction required.
	 * @return {@code true} if there is an extracellular reaction with this
	 * name in this compartment, {@code false} if there is not.
	 */
	public Reaction getReaction(String name)
	{
		for ( Reaction reac : this._reactions )
			if ( reac.getName().equals(name) )
				return reac;
		return null;
	}
	
	/**
	 * \brief Give this compartment an extracellular reaction.
	 * 
	 * @param reaction Reaction to add to this compartment.
	 */
	public void addReaction(RegularReaction reaction)
	{
		// TODO Safety: check this reaction is not already present?
		this._reactions.add(reaction);
	}
	
	/**
	 * \brief Get the average concentration of a solute.
	 * 
	 * @param soluteName Name of the solute to use.
	 * @return Average (arithmetic mean) concentration of this solute.
	 */
	public double getAverageConcentration(String soluteName)
	{
		return this.getSoluteGrid(soluteName).getAverage(CONCN);
	}
	
	/**
	 * \brief Get the local concentration of a solute.
	 * 
	 * @param soluteName
	 * @param location
	 * @return concentration
	 */
	public double getLocalConcentration(String soluteName, double[] location)
	{
		return this.getSoluteGrid(soluteName).getValueAt(CONCN, location);
	}
	
	/**
	 * @return Average concentrations of all solutes in this compartment.
	 */
	public Map<String,Double> getAverageConcentrations()
	{
		Map<String,Double> out = new HashMap<String,Double>();
		for ( String name : this.getSoluteNames() )
			out.put(name, this.getAverageConcentration(name));
		return out;
	}
	
	/**
	 * \brief Set the concentration of a solute over the entire compartment.
	 * 
	 * <p>All old concentrations of this solute will be lost.</p>
	 * 
	 * @param soluteName Name of the solute to set.
	 * @param newConcn Set all voxels to this value.
	 */
	public void setAllConcentration(String soluteName, double newConcn)
	{
		this.getSoluteGrid(soluteName).setAllTo(CONCN, newConcn);
	}
	
	/* ***********************************************************************
	 * SOLUTE BOUNDARIES
	 * **********************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<Boundary> getNonSpatialBoundaries()
	{
		return this._shape.getNonSpatialBoundaries();
	}
	
	/**
	 * \brief TODO
	 *
	 */
	public void updateSoluteBoundaries()
	{
		Tier level = Tier.DEBUG;
		if ( Log.shouldWrite(level) )
			Log.out(level, "Updating solute boundaries...");
		for ( Boundary b : this._shape.getAllBoundaries() )
		{
			b.updateMassFlowRates();
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, " All solute boundaries now updated");
		updateWellMixed();
	}
	
	/**
	 * \brief Mass flows to/from the well-mixed region (if it exists)
	 * accumulate during diffusion methods when solving PDEs. Here, we
	 * distribute these flows among the relevant spatial boundaries.
	 */
	public void distributeWellMixedFlows(double dt)
	{
		/* Find all relevant boundaries. */
		Collection<WellMixedBoundary> boundaries = 
				this._shape.getWellMixedBoundaries();
		/* If there are none, then we have nothing more to do. */
		if ( boundaries.isEmpty() )
			return;
		Compartment thisComp = (Compartment) this.getParent();

		/*
		 * If there is just one well-mixed boundary, then simply transfer the
		 * flow over from each grid to the boundary. Once this is done, finish.
		 */
		if ( boundaries.size() == 1 )
		{

			Boundary b = boundaries.iterator().next();			

			String partnerCompName = b.getPartnerCompartmentName();
			Compartment partnerComp = Idynomics.simulator.getCompartment(partnerCompName);
			double scFac = ( partnerComp == null ? 1.0 : 
				thisComp.getScalingFactor() / partnerComp.getScalingFactor());
			for ( SpatialGrid solute : this._solutes )
			{
				double solMassFlow = -solute.getWellMixedMassFlow();
				b.increaseMassFlowRate(solute.getName(), 
						solMassFlow/dt * scFac);
				solute.resetWellMixedMassFlow();
			}
			return;
		}
		/*
		 * If there are multiple well-mixed boundaries, then distribute the
		 * mass flows according to their share of the total surface area.
		 */
		double totalArea = 0.0;
		for ( SpatialBoundary boundary : boundaries )
			totalArea += boundary.getTotalSurfaceArea();
		double scaleFactor;
		for ( SpatialBoundary boundary : boundaries )
		{
			scaleFactor = boundary.getTotalSurfaceArea()/totalArea;
			String partnerCompName = boundary.getPartnerCompartmentName();
			Compartment partnerComp = Idynomics.simulator.getCompartment(partnerCompName);
			double scFac = thisComp.getScalingFactor() / partnerComp.getScalingFactor();
			for ( SpatialGrid solute : this._solutes )
			{
				double solMassFlow = solute.getWellMixedMassFlow();
				boundary.increaseMassFlowRate(solute.getName(), 
						solMassFlow * scaleFactor * scFac);
			}
		}
		for ( SpatialGrid solute : this._solutes )
			solute.resetWellMixedMassFlow();
	}
	
	/* ***********************************************************************
	 * COMMON GRID METHODS
	 * **********************************************************************/

	/**
	 * @return The unique spatial grid that is common to all solutes in this
	 * compartment's shape. Hold information like well-mixed.
	 */
	public SpatialGrid getCommonGrid()
	{
		if ( this._commonGrid == null )
		{
			this._commonGrid = 
					new SpatialGrid(this._shape, COMMON_GRID_NAME, this);
		}
		return this._commonGrid;
	}
	
	/**
	 *\brief Calculate which region of the compartment shape is assumed to be
	 * well-mixed, using the boundaries.
	 */
	public void updateWellMixed()
	{
		String name;
		/*
		 * Reset the well-mixed array for this shape. If none of the
		 * boundaries need it to be updated, it will be full of zeros (i.e.
		 * nowhere is well-mixed).
		 */
		SpatialGrid commonGrid = this.getCommonGrid();
		commonGrid.newArray(WELLMIXED);
		/*
		 * Check if any of the boundaries need to update the well-mixed array.
		 * If none do, then there is nothing more to do.
		 */
		Collection<WellMixedBoundary> bndrs = 
				this.getShape().getWellMixedBoundaries();
		if ( bndrs.isEmpty() )
		{
			commonGrid.setAllTo(WELLMIXED, WellMixedConstants.NOT_MIXED);
			return;
		}
		/*
		 * Otherwise, we assume that every voxel is mixed until the boundaries
		 * say that it is not.
		 */
		commonGrid.setAllTo(WELLMIXED, WellMixedConstants.COMPLETELY_MIXED);
		/*
		 * We will eventually need to set the concentration of each solute in 
		 * the well-mixed region, so prepare to collect the appropriate values.
		 */
		Map<String,Double> wellMixedConcns = new HashMap<String,Double>();
		for ( SpatialGrid solute : this._solutes )
			wellMixedConcns.put(solute.getName(), 0.0);
		/*
		 * The weighting of each well-mixed boundary is given by its surface
		 * area: find the total surface area of all well-mixed boundaries.
		 * 
		 * Note that multiplication is computationally cheaper than division,
		 * so divide one by this number now. We then multiply each well-mixed
		 * boundary's surface area by this scale factor to calculate the weight
		 * of its influence over the concentration.
		 */
		double scaleFactor = 0.0;
		double boundarySurface;
		for ( WellMixedBoundary b : bndrs )
		{
			boundarySurface = b.getTotalSurfaceArea();
			scaleFactor += boundarySurface;
		}
		scaleFactor = 1.0 / scaleFactor;
		/*
		 * At least one of the boundaries need to update the well-mixed array,
		 * so loop through all of them.
		 */
		for ( WellMixedBoundary b: bndrs )
		{
			b.updateWellMixedArray();
			if ( b.needsToUpdateWellMixed() )
			{
				double sAreaFactor = b.getTotalSurfaceArea() * scaleFactor;
				for ( SpatialGrid solute : this._solutes )
				{
					name = solute.getName();
					double concn = wellMixedConcns.get(name);
					concn += b.getConcentration(name) * sAreaFactor;
					wellMixedConcns.put(name, concn);
				}
			}
		}
		/*
		 * Now apply the well-mixed boundary concentrations to the voxels of 
		 * the well-mixed region.
		 */
		for ( int[] coord = this._shape.resetIterator();
				this._shape.isIteratorValid();
				coord = this._shape.iteratorNext() )
		{
			if ( WellMixedConstants.isWellMixed(commonGrid, coord) )
				for ( SpatialGrid solute : this._solutes )
				{
					name = solute.getName();
					solute.setValueAt(CONCN, coord, wellMixedConcns.get(name));
				}
		}
	}
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/
	
	public void printSolute(String soluteName)
	{
		Log.out(Tier.DEBUG, soluteName+":");
		Log.out(Tier.DEBUG, 
				this.getSoluteGrid(soluteName).arrayAsText(CONCN));
	}
	
	public void printAllSolutes()
	{
		for(String s : this.getSoluteNames())
			this.printSolute(s);
	}
	
	/* ***********************************************************************
	 * PRE-LAUNCH CHECK
	 * **********************************************************************/
	
	@Override
	public boolean isReadyForLaunch()
	{
		// TODO
		return true;
	}

	@Override
	public Module getModule() {
		/* The compartment node. */
		Module modelNode = new Module(XmlRef.environment, this);
		modelNode.setRequirements(Requirements.IMMUTABLE);
		/* Set title for GUI. */
		modelNode.setTitle(XmlRef.environment);
		/* Add the name attribute. */
		modelNode.add( this.getSolutesNode() );
		/* Add the reactions node. */
		modelNode.add( this.getReactionNode() );
		return modelNode;	
	}
	
	/**
	 * \brief Helper method for {@link #getModule()}.
	 * 
	 * @return Model node for the <b>solutes</b>.
	 */
	private Module getSolutesNode()
	{
		/* The solutes node. */
		Module modelNode = new Module(XmlRef.solutes, this);
		modelNode.setTitle(XmlRef.solutes);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		/* 
		 * add solute nodes, yet only if the environment has been initiated, when
		 * creating a new compartment solutes can be added later 
		 */
		for ( String sol : this.getSoluteNames() )
			modelNode.add( this.getSoluteGrid(sol).getModule() );
		
		modelNode.addChildSpec( ClassRef.spatialGrid, 
				null, Module.Requirements.ZERO_TO_MANY );
		
		return modelNode;
	}
	
	private Module getReactionNode() 
	{
		return this._reactions.getModule();
		//TODO Check whether chicldspec is properly set as before
//		/* The reactions node. */
//		Module modelNode = new Module(XmlRef.reactions, this);
//		modelNode.setTitle(XmlRef.reactions);
//		modelNode.setRequirements(Requirements.EXACTLY_ONE);
//		/* 
//		 * add solute nodes, yet only if the environment has been initiated, when
//		 * creating a new compartment solutes can be added later 
//		 */
//		for ( Reaction react : this.getReactions() )
//			modelNode.add( react.getModule() );
//		
//		modelNode.addChildSpec( ClassRef.reaction, 
//				null, Module.Requirements.ZERO_TO_MANY );
//		
//		return modelNode;
	}
	
	public void setModule(Module node)
	{
		/* 
		 * Set the child nodes.
		 * Agents, process managers and solutes are container nodes: only
		 * child nodes need to be set here.
		 */
		Settable.super.setModule(node);
	}
	
	public void removeSolute(SpatialGrid solute)
	{

		this._solutes.remove(solute);
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return XmlRef.environment;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}

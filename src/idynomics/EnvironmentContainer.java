package idynomics;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.Boundary;
import boundary.SpatialBoundary;
import dataIO.Log;
import dataIO.XmlRef;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import grid.ArrayType;
import grid.SpatialGrid;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import reaction.Reaction;
import shape.Shape;

/**
 * \brief Manages the solutes in a {@code Compartment}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class EnvironmentContainer implements CanPrelaunchCheck, NodeConstructor
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
	 * Collection of extracellular reactions (each Reaction knows its own name).
	 */
	protected Collection<Reaction> _reactions = new LinkedList<Reaction>();
	/**
	 * Name of the common grid.
	 */
	public final static String COMMON_GRID_NAME = "common";
	/**
	 * Grid of common attributes, such as well-mixed.
	 */
	protected SpatialGrid _commonGrid;
	
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
	
	public void addSolute(SpatialGrid solute)
	{
		this._solutes.add(solute);
		Log.out(Tier.DEBUG, "Added solute \""+ solute.getName() +"\" to environment");
	}
	
	public void deleteSolute(Object spatialGrid)
	{
		_solutes.remove(spatialGrid);
	}
	
	public void deleteReaction(Object Reaction)
	{
		_reactions.remove(Reaction);
	}
	
	
	/**
	 * \brief TODO
	 * 
	 * NOTE Rob[26Feb2016]: not yet used, work in progress
	 * 
	 * TODO Get general reactions from Param?
	 * 
	 * @param reactionNodes
	 */
	public void readReactions(NodeList reactionNodes)
	{
		Element elem;
		Reaction reac;
		for ( int i = 0; i < reactionNodes.getLength(); i++)
		{
			elem = (Element) reactionNodes.item(i);
			/* Construct and intialise the reaction. */
			reac = (Reaction) Reaction.getNewInstance(elem);
			// reac.setCompartment(compartment.getName());
			reac.init(elem);
			/* Add it to the environment. */
			this.addReaction(reac);
		}
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
	public Collection<Reaction> getReactions()
	{
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
	public void addReaction(Reaction reaction)
	{
		// TODO Safety: check this reaction is not already present?
		this._reactions.add(reaction);
	}
	
	public void setReactions(Collection<Reaction> reactions)
	{
		this._reactions = reactions;
	}
	
	/**
	 * \brief Get the average concentration of a solute.
	 * 
	 * @param soluteName Name of the solute to use.
	 * @return Average (arithmetic mean) concentration of this solute.
	 */
	public double getAverageConcentration(String soluteName)
	{
		return this.getSoluteGrid(soluteName).getAverage(ArrayType.CONCN);
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
		this.getSoluteGrid(soluteName).setAllTo(ArrayType.CONCN, newConcn);
	}
	
	/* ***********************************************************************
	 * SOLUTE BOUNDARIES
	 * **********************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public Collection<Boundary> getOtherBoundaries()
	{
		return this._shape.getOtherBoundaries();
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
			b.updateMassFlowRates();
		if ( Log.shouldWrite(level) )
			Log.out(level, " All solute boundaries now updated");
	}
	
	/**
	 * \brief Mass flows to/from the well-mixed region (if it exists)
	 * accumulate during diffusion methods when solving PDEs. Here, we
	 * distribute these flows among the relevant spatial boundaries.
	 */
	public void distributeWellMixedFlows()
	{
		/* Find all relevant boundaries. */
		Collection<SpatialBoundary> boundaries = 
				this._shape.getWellMixedBoundaries();
		/* If there are none, then we have nothing more to do. */
		if ( boundaries.isEmpty() )
			return;
		/*
		 * If there is just one well-mixed boundary, then simply transfer the
		 * flow over from each grid to the boundary. Once this is done, finish.
		 */
		if ( boundaries.size() == 1 )
		{
			Boundary b = boundaries.iterator().next();
			for ( SpatialGrid solute : this._solutes )
			{
				b.increaseMassFlowRate(solute.getName(), 
						solute.getWellMixedMassFlow());
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
			for ( SpatialGrid solute : this._solutes )
			{
				boundary.increaseMassFlowRate(solute.getName(), 
						solute.getWellMixedMassFlow() * scaleFactor);
			}
		}
		for ( SpatialGrid solute : this._solutes )
			solute.resetWellMixedMassFlow();
	}
	
	/* ***********************************************************************
	 * COMMON GRID METHODS
	 * **********************************************************************/

	/**
	 * @return
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
	 *\brief TODO
	 */
	public void updateWellMixed()
	{
		/*
		 * Reset the well-mixed array for this shape. If none of the
		 * boundaries need it to be updated, it will be full of zeros (i.e.
		 * nowhere is well-mixed).
		 */
		this.getCommonGrid().newArray(ArrayType.WELLMIXED);
		/*
		 * Check if any of the boundaries need to update the well-mixed array.
		 * If none do, then there is nothing more to do.
		 */
		Collection<SpatialBoundary> bndrs = 
				this.getShape().getWellMixedBoundaries();
		if ( bndrs.isEmpty() )
			return;
		/*
		 * At least one of the boundaries need to update the well-mixed array,
		 * so loop through all of them.
		 */
		for ( SpatialBoundary b: bndrs )
			b.updateWellMixedArray();
	}
	
	/* ***********************************************************************
	 * REPORTING
	 * **********************************************************************/
	
	public void printSolute(String soluteName)
	{
		Log.out(Tier.QUIET, soluteName+":");
		Log.out(Tier.QUIET, 
				this.getSoluteGrid(soluteName).arrayAsText(ArrayType.CONCN));
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
	public ModelNode getNode() {
		/* The compartment node. */
		ModelNode modelNode = new ModelNode(XmlRef.environment, this);
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
	 * \brief Helper method for {@link #getNode()}.
	 * 
	 * @return Model node for the <b>solutes</b>.
	 */
	private ModelNode getSolutesNode()
	{
		/* The solutes node. */
		ModelNode modelNode = new ModelNode(XmlRef.solutes, this);
		modelNode.setTitle(XmlRef.solutes);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		/* 
		 * add solute nodes, yet only if the environment has been initiated, when
		 * creating a new compartment solutes can be added later 
		 */
		for ( String sol : this.getSoluteNames() )
			modelNode.add( this.getSoluteGrid(sol).getNode() );
		return modelNode;
	}
	
	private ModelNode getReactionNode() 
	{
		/* The reactions node. */
		ModelNode modelNode = new ModelNode(XmlRef.reactions, this);
		modelNode.setTitle(XmlRef.reactions);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		/* 
		 * add solute nodes, yet only if the environment has been initiated, when
		 * creating a new compartment solutes can be added later 
		 */
		for ( Reaction react : this.getReactions() )
			modelNode.add( react.getNode() );
		return modelNode;
	}
	
	public void setNode(ModelNode node)
	{
		/* 
		 * Set the child nodes.
		 * Agents, process managers and solutes are container nodes: only
		 * child nodes need to be set here.
		 */
		NodeConstructor.super.setNode(node);
	}
	
	public void removeChildNode(NodeConstructor child)
	{
		if (child instanceof SpatialGrid)
			this._solutes.remove((SpatialGrid) child);
		if (child instanceof Reaction)
			this._reactions.remove((Reaction) child);
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return XmlRef.environment;
	}
}

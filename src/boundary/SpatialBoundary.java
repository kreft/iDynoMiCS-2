package boundary;

import static grid.ArrayType.WELLMIXED;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import grid.ArrayType;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.Dimension;
import shape.Dimension.DimName;

/**
 * \brief Abstract class of boundary that has a location in space.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public abstract class SpatialBoundary extends Boundary
{
	/**
	 * This boundary is at one extreme of a dimension: this is the name of that
	 * dimension.
	 */
	protected DimName _dim;
	/**
	 * This boundary is at one extreme of a dimension: this is the index of
	 * that extreme (0 for minimum, 1 for maximum).
	 */
	protected int _extreme;
	/**
	 * Boundary layer thickness.
	 */
	// TODO set this from protocol file for the whole compartment
	protected double _layerThickness;
	
	protected double _detachability;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	/**
	 * FIXME essential for instantiation
	 */
	public SpatialBoundary()
	{
		
	}
	
	/**
	 * FIXME essential for instantiation
	 */
	public void instantiate(Element xmlElement, NodeConstructor parent) 
	{
		this._dim = DimName.valueOf(XmlHandler.obtainAttribute(
				xmlElement, XmlRef.shapeDimension, XmlRef.dimensionBoundary));
		this._extreme = Integer.valueOf(XmlHandler.obtainAttribute(
				xmlElement, XmlRef.extreme, XmlRef.dimensionBoundary)); // shape and this are inconsistent
	}
	
	/**
	 * \brief Construct a spatial boundary by giving it the information it
	 * needs about its location.
	 * 
	 * @param dim This boundary is at one extreme of a dimension: this is the
	 * name of that dimension.
	 * @param extreme This boundary is at one extreme of a dimension: this is
	 * the index of that extreme (0 for minimum, 1 for maximum).
	 */
	public SpatialBoundary(DimName dim, int extreme)
	{
		this._dim = dim;
		this._extreme = extreme;
	}
	
	/* ***********************************************************************
	 * BASIC SETTERS & GETTERS
	 * **********************************************************************/
	
	/**
	 * @return The name of the dimension this is on an extreme of.
	 */
	public DimName getDimName()
	{
		return this._dim;
	}
	
	/**
	 * @return The index of the extreme (of a shape dimension) this is on.
	 */
	public int getExtreme()
	{
		return this._extreme;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param thickness
	 */
	public void setLayerThickness(double thickness)
	{
		this._layerThickness = thickness;
	}
	
	public double getTotalSurfaceArea()
	{
		// TODO it may be best to store this locally, updating it at each
		// global timestep
		return this._agents.getShape()
				.getBoundarySurfaceArea(this._dim, this._extreme);
	}
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	/**
	 * \brief Get the diffusive flow across this boundary, into the grid's
	 * current iterator voxel.
	 * 
	 * <p>Flux has units of mass or mole per unit area per unit time, so we
	 * here multiply by the shared surface area to calculate the mass flow 
	 * (units of mass/mole per unit time). Divide by the volume of the current
	 * iterator voxel to calculate the rate of change of concentration due to
	 * diffusive flow.</p>
	 * 
	 * <p>Note that we get the name of the variable from the grid itself.</p>
	 * 
	 * <p>Note that we ask the boundary to report the flow, rather than its 
	 * concentration, as we may wish to introduce some kind of Neumann boundary
	 * condition in future.</p>
	 * 
	 * @param grid Spatial grid representing a variable with a {@code CONCN}
	 * array, typically a solute.
	 * @return The rate of diffusive flow across this boundary, in units of
	 * mass or mole per time.
	 */
	public double getDiffusiveFlow(SpatialGrid grid)
	{
		// NOTE Rob [27June2016]: Tried this approach and am not happy with it.
		if ( grid.getName().equals(AgentContainer.DETACHABILITY) )
			return this.calcDiffusiveFlowFixed(grid,  this.getDetachability());
		return this.calcDiffusiveFlow(grid);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param grid
	 * @return
	 */
	protected abstract double calcDiffusiveFlow(SpatialGrid grid);
	
	/**
	 * \brief TODO
	 * 
	 * @param grid
	 * @param type
	 * @param bndrConcn
	 * @return
	 */
	protected double calcDiffusiveFlowFixed(SpatialGrid grid, double bndrConcn)
	{
		Tier level = Tier.BULK;
		double valueDiff = bndrConcn - grid.getValueAtCurrent(ArrayType.CONCN);
		/* The diffusivity comes only from the current voxel. */
		double diffusivity = grid.getValueAtCurrent(ArrayType.DIFFUSIVITY);
		/* Shape handles the shared surface area on a boundary. */
		double sArea = grid.getShape().nhbCurrSharedArea();
		/* Shape handles the centre-centre distance on a boundary. */
		double dist = grid.getShape().nhbCurrDistance();
		/* Calculate flux and flow in the same way as in SpatialGrid. */
		double flux = valueDiff * diffusivity / dist ;
		double flow = flux * sArea;
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, this.getName()+" flow for "+grid.getName()+" :");
			Log.out(level, "  value diff is "+valueDiff);
			Log.out(level, "  diffusivity is "+diffusivity);
			Log.out(level, "  distance is "+dist);
			Log.out(level, "  => flux = "+flux);
			Log.out(level, "  surface area is "+sArea);
			Log.out(level, "  => flow = "+flow);
		}
		return flow;
	}
	
	/**
	 * \brief Ask if this boundary needs to update the well-mixed array of
	 * the compartment it belong to.
	 * 
	 * <p>This method will be called in all spatial boundaries of the
	 * compartment. If none do, then {@link #updateWellMixedArray()}
	 * will be skipped. If at least one does, then 
	 * {@link #updateWellMixedArray()} will be called in all.</p>
	 * 
	 * <p>If you want to create a new class of boundary that is well-mixed,
	 * do not override this method but instead have your new class extend
	 * WellMixedBoundary. We have used this approach instead of
	 * {@code instanceof} as it each much easier to search for uses through
	 * Eclipse's search hierarchy feature.</p>
	 * 
	 * @return Whether this boundary needs to update the well-mixed array of
	 * the compartment it belong to.
	 */
	public boolean needsToUpdateWellMixed()
	{
		return false;
	}
	
	/**
	 * \brief Update the common grid's well-mixed array.
	 * 
	 * <p>All spatial grids must be able to this, even if they do not need a
	 * well-mixed region by themselves.</p>
	 */
	public abstract void updateWellMixedArray();
	
	/**
	 * \brief Helper method for {@link #updateWellMixedArray()}.
	 * 
	 * <p>Loops over all voxels in the grid, setting the well-mixed value to
	 * zero if the distance from the centre of the voxel to this boundary is
	 * less than or equal to the boundary layer thickness.</p>
	 */
	protected void setWellMixedByDistance()
	{
		Shape aShape = this._environment.getShape();
		SpatialGrid grid = this._environment.getCommonGrid();
		aShape.resetIterator();
		while ( aShape.isIteratorValid() )
		{
			double distance = aShape
					.currentDistanceFromBoundary(this._dim, this._extreme);
			if ( distance <= this._layerThickness )
				grid.setValueAt(WELLMIXED, aShape.iteratorCurrent(), 0.0);
			aShape.iteratorNext();
		}
	}
	
	/* ***********************************************************************
	 * AGENT TRANSFERS
	 * **********************************************************************/
	
	/**
	 * 
	 * @return
	 */
	protected abstract double getDetachability();
	
	/**
	 * \brief Helper method for placing agents in the arrivals lounge at random
	 * locations along the boundary surface.
	 * 
	 * <p>Non-located agents are added to the agent container and removed from
	 * the arrivals lounge, so that all remaining agents are located.</p>
	 * 
	 * @param agentCont The {@code AgentContainer} that should accept the 
	 * {@code Agent}s.
	 */
	protected void placeAgentsRandom()
	{
		Tier level = Tier.DEBUG;
		Shape aShape = this._agents.getShape();
		double[] newLoc;
		Body body;
		for ( Agent anAgent : this._arrivalsLounge )
		{
			if ( AgentContainer.isLocated(anAgent) )
			{
				newLoc = aShape.getRandomLocationOnBoundary(
						this._dim, this._extreme);
				if ( Log.shouldWrite(level) )
				{
					Log.out(level, "Placing agent (UID: "+anAgent.identity()+
							") at random location: "+Vector.toString(newLoc));
				}
				body = (Body) anAgent.get(AspectRef.agentBody);
				body.relocate(newLoc);
			}
			else
			{
				this._arrivalsLounge.remove(anAgent);
			}
			this._agents.addAgent(anAgent);
		}
	}
	
	/* ************************************************************************
	 * MODEL NODE
	 * ***********************************************************************/
	
	public void init(Node xmlNode)
	{
		Element xmlElem = (Element) xmlNode;
		/* Get the minimum or maximum. */
		String str = xmlElem.getAttribute("extreme");
		this._extreme = Dimension.extremeToInt(str);
	}
	
	@Override
	public ModelNode getNode()
	{
		ModelNode modelNode = super.getNode();
		/* Which dimension? */
		modelNode.add(new ModelAttribute(XmlRef.dimensionNamesAttribute,
				this._dim.toString(),
				null, true));
		/* Minimum or maximum extreme of this dimension? */
		modelNode.add(new ModelAttribute("extreme", 
				Dimension.extremeToString(this._extreme),
				new String[]{XmlRef.min, XmlRef.max}, true));
		return modelNode;
	}
	
	@Override
	public boolean isReadyForLaunch()
	{
		if ( ! super.isReadyForLaunch() )
			return false;
		return true;
	}

}

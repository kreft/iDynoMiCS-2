package boundary;

import static grid.ArrayType.WELLMIXED;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlRef;
import generalInterfaces.Instantiatable;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import shape.Shape;
import shape.Dimension;
import shape.Dimension.DimName;

/**
 * \brief Abstract class of boundary that has a location in space.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
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
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
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
	
	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	/**
	 * \brief Get the diffusive flux across this boundary, into the grid's
	 * current iterator voxel.
	 * 
	 * <p>Note that we get the name of the variable from the grid itself.</p>
	 * 
	 * @param grid Spatial grid representing a variable with a {@code CONCN}
	 * array, most likely a solute.
	 * @return The rate of diffusive flux across this boundary, in units of
	 * concentration per time.
	 */
	public abstract double getFlux(SpatialGrid grid);
	
	/**
	 * \brief Ask if this boundary needs to update the well-mixed array of
	 * the compartment it belong to.
	 * 
	 * <p>This method will be called in all spatial boundaries of the
	 * compartment. If none do, then {@link #updateWellMixedArray(SpatialGrid, AgentContainer)}
	 * will be skipped. If at least one does, then 
	 * {@link #updateWellMixedArray(SpatialGrid, AgentContainer)} will be called in all.</p>
	 * 
	 * @return Whether this boundary needs to update the well-mixed array of
	 * the compartment it belong to.
	 */
	public abstract boolean needsToUpdateWellMixed();
	
	/**
	 * \brief TODO
	 * 
	 * @param grid
	 * @param agents TODO
	 */
	public abstract void updateWellMixedArray(
			SpatialGrid grid, AgentContainer agents);
	
	/**
	 * \brief Helper method for {@link #updateWellMixedArray(SpatialGrid, AgentContainer)}.
	 * 
	 * <p>Loops over all voxels in the grid, setting the well-mixed value to
	 * zero if the distance from the centre of the voxel to this boundary is
	 * less than or equal to the boundary layer thickness.</p>
	 * 
	 * @param grid The compartment's default grid.
	 */
	protected void setWellMixedByDistance(SpatialGrid grid)
	{
		Shape aShape = grid.getShape();
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
	 * \brief Helper method for placing agents in the arrivals lounge at random
	 * locations along the boundary surface.
	 * 
	 * <p>Non-located agents are added to the agent container and removed from
	 * the arrivals lounge, so that all remaining agents are located.</p>
	 * 
	 * @param agentCont The {@code AgentContainer} that should accept the 
	 * {@code Agent}s.
	 */
	protected void placeAgentsRandom(AgentContainer agentCont)
	{
		Shape aShape = agentCont.getShape();
		double[] newLoc;
		Body body;
		for ( Agent anAgent : this._arrivalsLounge )
		{
			if ( AgentContainer.isLocated(anAgent) )
			{
				newLoc = aShape.getRandomLocationOnBoundary(
						this._dim, this._extreme);
				Log.out(Tier.DEBUG, "Placing agent (UID: "+anAgent.identity()+
						") at random location: "+Vector.toString(newLoc));
				body = (Body) anAgent.get(AspectRef.agentBody);
				body.relocate(newLoc);
			}
			else
			{
				this._arrivalsLounge.remove(anAgent);
			}
			agentCont.addAgent(anAgent);
		}
	}
	
	/* ************************************************************************
	 * MODEL NODE
	 * ***********************************************************************/
	
	// TODO delete once nodeFactory has made this redundant
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
	
	
	// TODO!
	
	public static SpatialBoundary getNewInstance(String className)
	{
		return (SpatialBoundary) 
				Instantiatable.getNewInstance(className, "boundary.spatialLibrary.");
	}
	
	@Override
	public boolean isReadyForLaunch()
	{
		if ( ! super.isReadyForLaunch() )
			return false;
		return true;
	}
}

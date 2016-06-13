/**
 * 
 */
package boundary;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlRef;
import generalInterfaces.XMLable;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import shape.Shape;
import shape.Dimension;
import shape.Dimension.DimName;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class SpatialBoundary extends Boundary
{
	// TODO move this to XmlLabel?
	public final static String DEFAULT_GM = "defaultGridMethod";
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
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief TODO
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
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
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
	
	/*************************************************************************
	 * SOLUTE TRANSFERS
	 ************************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param grid
	 * @return
	 */
	public abstract double getFlux(SpatialGrid grid);
	
	/*************************************************************************
	 * AGENT TRANSFERS
	 ************************************************************************/
	
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
	
	/**************************************************************************
	 * MODEL NODE
	 *************************************************************************/
	
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
				XMLable.getNewInstance(className, "boundary.spatialLibrary.");
	}
	
	@Override
	public boolean isReadyForLaunch()
	{
		if ( ! super.isReadyForLaunch() )
			return false;
		return true;
	}
}

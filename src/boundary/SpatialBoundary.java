/**
 * 
 */
package boundary;

import java.util.HashMap;

import agent.Agent;
import agent.Body;
import aspect.AspectRef;
import boundary.grid.GridMethod;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import linearAlgebra.Vector;
import shape.Shape;
import shape.Dimension.DimName;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
	 * The grid method this boundary should use for any variable that is not
	 * named in the dictionary {@link #_gridMethods}. 
	 */
	protected GridMethod _defaultGridMethod;
	/**
	 * Dictionary of grid methods that this boundary should use for each
	 * variable (e.g. a solute). If a variable is not in this list, use the
	 * default, {@link #_defaultGridMethod}, instead.
	 */
	protected HashMap<String,GridMethod> _gridMethods = 
										new HashMap<String,GridMethod>();
	
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
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @param aMethod
	 */
	public void setGridMethod(String soluteName, GridMethod aMethod)
	{
		if ( soluteName.equals(DEFAULT_GM) )
		{
			if ( this._defaultGridMethod == null )
			{
				Log.out(Tier.EXPRESSIVE,
						"Boundary setting default grid method");
			}
			else
			{
				// TODO Change to warning if overwriting the default?
				Log.out(Tier.EXPRESSIVE,
						"Boundary overwriting default grid method");
			}
			this._defaultGridMethod = aMethod;
		}
		else
		{
			Log.out(Tier.EXPRESSIVE,
					"Boundary setting grid method for "+soluteName);
			this._gridMethods.put(soluteName, aMethod);
		}
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param soluteName
	 * @return
	 */
	public GridMethod getGridMethod(String soluteName)
	{
		Tier level = Tier.BULK;
		if ( this._gridMethods.containsKey(soluteName) )
		{
			Log.out(level, "Boundary found grid method for "+soluteName);
			return this._gridMethods.get(soluteName);
		}
		else
		{
			Log.out(level, "Boundary using default grid method for "+soluteName);
			return this._defaultGridMethod;
		}
	}
	
	
	
	
	
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
}

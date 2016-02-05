/**
 * 
 */
package shape;

import org.w3c.dom.Node;

import boundary.Boundary;
import grid.GridBoundary.GridMethod;
import grid.SpatialGrid;

/**
 * @author cleggrj
 *
 */
public final class ShapeConventions
{
	/**
	 * 
	 * 
	 */
	public enum DimName
	{
		X,
		Y,
		Z,
		R,
		THETA,
		PHI;
	}
	
	
	/**
	 * \brief Dummy class for cyclic dimensions.
	 * 
	 * Should only be initialised by Dimension and never from protocol file.
	 */
	public static class BoundaryCyclic extends Boundary
	{
		public BoundaryCyclic()
		{
			_defaultGridMethod = new CyclicGrid();
		}
	}
	
	public static class CyclicGrid implements GridMethod
	{
		@Override
		public void init(Node xmlNode)
		{
			/* Do nothing here. */ 
		}
		
		@Override
		public double getBoundaryFlux(SpatialGrid grid)
		{
			// TODO
			return 0;
		}
		
	}
}

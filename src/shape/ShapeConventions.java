/**
 * 
 */
package shape;

import org.w3c.dom.Element;

import boundary.Boundary;
import boundary.grid.GridMethod;
import grid.SpatialGrid;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
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
	
	public static class CyclicGrid extends GridMethod
	{
		@Override
		public void init(Element xmlNode)
		{
			/* Do nothing here. */ 
		}
		
		@Override
		public double getBoundaryFlux(SpatialGrid grid)
		{
			// TODO
			return 0;
		}
		
		@Override
		public String getXml()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	
}

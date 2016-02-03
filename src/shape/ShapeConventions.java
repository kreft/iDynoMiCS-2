/**
 * 
 */
package shape;

import boundary.Boundary;

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
			
		}
	}
}

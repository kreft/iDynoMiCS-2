package generalInterfaces;

import surface.BoundingBox;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public interface HasBoundingBox
{
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public BoundingBox boundingBox();
	
	/**
	 * \brief TODO
	 * 
	 * @param margin
	 * @return
	 */
	public BoundingBox boundingBox(double margin);
}

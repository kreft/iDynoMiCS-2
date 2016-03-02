package spatialRegistry;

import java.util.List;

import surface.BoundingBox;

/**
 * \brief TODO
 * 
 * @param <T>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU.
 */
public abstract class SpatialRegistry<T>
{	
	/**
	 * \brief TODO
	 * 
	 * @param coords
	 * @param dimension
	 * @return
	 */
	public abstract List<T> search(double[] coords, double[] dimension);
	
	/**
	 * \brief TODO
	 * 
	 * @param coords
	 * @param dimension
	 * @return
	 */
	public abstract List<T> cyclicsearch(double[] coords, double[] dimension);
	
	/**
	 * 
	 * @param boundingBox
	 * @return
	 */
	public abstract List<T> cyclicsearch(BoundingBox boundingBox);
	
	/**
	 * 
	 * @param boundingBox
	 * @return
	 */
	public abstract List<T> cyclicsearch(List<BoundingBox> boundingBoxes);
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract List<T> all();
	
	/**
	 * \brief TODO
	 * 
	 * @param coords
	 * @param dimensions
	 * @param entry
	 */
	public abstract void insert(double[] coords, double[] dimensions, T entry);

	/**
	 * 
	 * @param boundingBox
	 * @param entry
	 */
	public abstract void insert(BoundingBox boundingBox, T entry);
	
	/**
	 * 
	 * @return
	 */
	public abstract T getRandom();
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public abstract boolean delete(T entry);
}

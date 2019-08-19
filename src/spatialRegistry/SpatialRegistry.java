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
public interface SpatialRegistry<T>
{	
	/**
	 * \brief TODO
	 * 
	 * @param coords
	 * @param dimension
	 * @return
	 */
	public abstract List<T> search(double[] lower, double[] higher);
	
	/**
	 * 
	 * @param boundingBox
	 * @return
	 */
	public abstract List<T> search(Area area);
	
	/**
	 * 
	 * @param boundingBox
	 * @return
	 */
	public abstract List<T> search(List<BoundingBox> boundingBoxes);
	
	public default List<T> search(double[] pointLocation)
	{
		return this.search(pointLocation, pointLocation);
	}

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
	
	public abstract boolean delete(T entry);
	
	public abstract void clear();
}

package spatialRegistry;

import java.util.List;

import linearAlgebra.Vector;
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
	 * \brief perform local search, ignore periodic boundaries.
	 * 
	 * @param coords
	 * @param dimension
	 * @return
	 */
	public abstract List<T> localSearch(double[] coords, double[] dimension);
	
	/**
	 * \brief TODO
	 * 
	 * @param coords
	 * @param dimension
	 * @return
	 */
	public abstract List<T> search(double[] coords, double[] dimension);
	
	/**
	 * 
	 * @param boundingBox
	 * @return
	 */
	public abstract List<T> search(BoundingBox boundingBox);
	
	/**
	 * 
	 * @param boundingBox
	 * @return
	 */
	public abstract List<T> search(List<BoundingBox> boundingBoxes);
	
	public default List<T> search(double[] pointLocation)
	{
		return this.search(pointLocation, Vector.zeros(pointLocation));
	}

	/**
	 * \brief TODO
	 * 
	 * @return
	 * @deprecated
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

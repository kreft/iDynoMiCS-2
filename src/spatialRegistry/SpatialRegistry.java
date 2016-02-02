package spatialRegistry;

import java.util.HashMap;
import java.util.List;

import boundary.PeriodicAgentBoundary;

/**
 * 
 * 
 * @param <T>
 * 
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


}

/**
 * 
 */
package generalInterfaces;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public interface CanPrelaunchCheck
{
	/**
	 * \brief Check if the object is ready for a simulation launch.
	 * 
	 * @return {@code boolean} describing if the object is ready (true) or not
	 * ready (false).
	 */
	public boolean isReadyForLaunch();
}
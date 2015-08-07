/**
 * 
 */
package reaction;

import java.util.HashMap;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk)
 */
public abstract class Kinetic
{

	/**
	 * \brief TODO
	 * 
	 */
	public Kinetic()
	{
		
	}
	
	
	public abstract double getRate(HashMap<String, Double> concentrations);
	
	
}

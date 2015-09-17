/**
 * 
 */
package shape;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public abstract class Shape
{
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**\
	 * brief TODO
	 * 
	 */
	public Shape() 
	{
		
	}
	
	
	
	public abstract boolean isOutside(double[] position);
	
	public abstract double distance(double[] position);
}

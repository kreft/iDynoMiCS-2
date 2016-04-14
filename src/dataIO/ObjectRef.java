package dataIO;

/**
 * \brief Collection of common object tags.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class ObjectRef
{
	/**
	 * Boolean, i.e. true or false.
	 */
	public final static String BOOL = "Boolean";
	/**
	 * String, i.e. text.
	 */
	public final static String STR = "String";
	/**
	 * String array, i.e. a list of text items.
	 */
	public final static String STR_VECT = STR+"[]";
	/**
	 * Integer, i.e. a whole number.
	 */
	public final static String INT = "Integer";
	/**
	 * Integer array, i.e. a list of whole numbers.
	 */
	public final static String INT_VECT = INT+"[]";
	/**
	 * Double, i.e. a real number.
	 */
	public final static String DBL = "Double";
	/**
	 * Double array, i.e. a list of real numbers.
	 */
	public final static String DBL_VECT = DBL+"[]";
	
	// TODO HashMap, LinkedList, etc?
}

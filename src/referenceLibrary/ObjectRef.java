package referenceLibrary;

import java.lang.reflect.Field;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * \brief Collection of common object tags.
 * 
 * FIXME: structure this the same way as class ref
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public final class ObjectRef
{
	public static String[] getAllOptions()
	{
		Field[] fields = ObjectRef.class.getFields();
		String[] options = new String[fields.length];
		int i = 0;

		for ( Field f : fields )
			try {
				options[i++] = (String) f.get(new String());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Log.out(Tier.CRITICAL, "problem in ObjectRef field declaretion"
						+ "\n can not obtain all options");
				e.printStackTrace();
			}
		return options;
	}
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
	 * Integer vector, i.e. a list of whole numbers.
	 */
	public final static String INT_VECT = INT+"[]";
	/**
	 * Integer matrix, i.e. a 2-dimensional collection of whole numbers.
	 */
	public final static String INT_MATR = INT_VECT+"[]";
	/**
	 * Integer array, i.e. a 3-dimensional collection of whole numbers.
	 */
	public final static String INT_ARRY = INT_MATR+"[]";
	/**
	 * Double, i.e. a real number.
	 */
	public final static String DBL = "Double";
	/**
	 * Double vector, i.e. a list of real numbers.
	 */
	public final static String DBL_VECT = DBL+"[]";
	/**
	 * Double matrix, i.e. a 2-dimensional collection of real numbers.
	 */
	public final static String DBL_MATR = DBL_VECT+"[]";
	/**
	 * Double array, i.e. a 3-dimensional collection of real numbers.
	 */
	public final static String DBL_ARRY = DBL_MATR+"[]";
	/**
	 * InstantiableList, Instantiable version of java LinkedList
	 */
	public static final String INSTANTIABLELIST = "InstantiableList";
	/**
	 * InstantiableMap, Instantiable version of java HashMap
	 */
	public static final String INSTANTIABLEMAP = "InstantiableMap";
	/**
	 * Java LinkedList
	 */
	public static final String LINKEDLIST = "LinkedList";
	/**
	 * Java HashMap
	 */
	public static final String HASHMAP = "HashMap";
	/**
	 * iDynoMiCS Reaction Object
	 */
	public static final String REACTION = "Reaction";
	/**
	 * iDynoMiCS Body Object
	 */
	public static final String BODY = "Body";
	/**
	 * iDynoMiCS Body Expression
	 */
	public static final String EXPRESSION = "Expression";
	
	
	
}

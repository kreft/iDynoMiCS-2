package generalInterfaces;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public interface Copyable
{
	/**
	 * Return a deep copy (Exact duplicate) all fields in the resulting copy
	 * all fields must be dereferenced, new copy objects are created for any 
	 * referenced objects inside the parent object. With the exception of
	 * immutable objects.
	 * @return
	 */
	public Object copy();

}

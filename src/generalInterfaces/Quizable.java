package generalInterfaces;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public interface Quizable
{
	/**
	 * Implemented by implementing the get("string") paradigm, answering with
	 * an Object.
	 * Used by graphical output classes to iterate over objects than can be
	 * displayed in graphical output.
	 */
	
	public Object get(String string);
}

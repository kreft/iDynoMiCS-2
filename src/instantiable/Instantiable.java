package instantiable;

import org.w3c.dom.Element;

import settable.Settable;

public interface Instantiable
{
	/*************************************************************************
	 * CLASS INSTANTIATION
	 * @param parent 
	 ************************************************************************/
	/**
	 * \brief method for Instantiable object initiation.
	 * 
	 * Overwrite this method in the implementing class. Note this method needs
	 * to be robust and should work with null input which is the default for
	 * creating a new object in the GUI. When one or multiple (when null is 
	 * given) essential parameters are not set, either set the default or query 
	 * the user.
	 * 
	 * @param xmlElement
	 * @param parent
	 */
	public void instantiate(Element xmlElement, Settable parent);
}

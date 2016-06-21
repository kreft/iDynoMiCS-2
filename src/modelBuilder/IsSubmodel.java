package modelBuilder;

import java.util.List;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public interface IsSubmodel
{
	public String getName();
	
	/**
	 * \brief Get a list of setter actions for all input arguments needed by
	 * this model component.
	 * 
	 * <p>This is ordered (i.e. a list, not a set), although this is not
	 * expected to be relevant in most cases. Examples of inputs include a
	 * name, parameter values, and sub-model components.</p>
	 * 
	 * @return List of all relevant input setters.
	 */
	public List<InputSetter> getRequiredInputs();
	
	/**
	 * \brief Accept an {@code Object} as input for a named parameter,
	 * sub-model, etc.
	 * 
	 * <p>Expected inputs are those listed in {@link #getRequiredInputs()}:
	 * implementers of this interface are strongly advised to keep these two
	 * methods adjacent in the code.</p>
	 * 
	 * @param name Name of a parameter, often from the {@code XmlLabel} class.
	 * @param input Input {@code Object}.
	 */
	public void acceptInput(String name, Object input);
}

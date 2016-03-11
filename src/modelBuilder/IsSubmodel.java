package modelBuilder;

import java.util.List;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public interface IsSubmodel
{
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	
	public List<InputSetter> getRequiredInputs();
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param input
	 */
	public void acceptInput(String name, Object input);
}

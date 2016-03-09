package modelBuilder;

import java.util.List;
import java.util.Map;

/**
 * \brief TODO
 * 
 * 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public interface IsSubmodel
{
	/**
	 * \brief TODO
	 * 
	 * <p>Note: make {@code Map}s instances of {@code LinkedHashMap} if the
	 * order is important. Otherwise, {@code HashMap} will suffice.</p>
	 * 
	 * @return
	 */
	public Map<String, Class<?>> getParameters();
	
	/**
	 * \brief TODO
	 * 
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value);
	
	/**
	 * 
	 * @return
	 */
	public List<SubmodelMaker> getSubmodelMakers();
	
	// TODO getAspects?
}

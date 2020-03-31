/**
 * 
 */
package agent.predicate;

import java.util.function.Predicate;

import agent.Agent;
import referenceLibrary.XmlRef;

/**
 * \brief Predicate for filtering lists of Agents according to species.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class IsNotSpecies implements Predicate<Agent>
{
	private String _speciesTag = XmlRef.species;
	
	private String _speciesName;
	
	/**
	 * \brief Construct a species test with both the species name, assuming the
	 * tag name for species to be the default.
	 * 
	 * @param speciesName Name of the species to filter for.
	 */
	public IsNotSpecies(String speciesName)
	{
		this._speciesName = speciesName;
	}
	
	/**
	 * \brief Construct a species test with both the species name, and the tag
	 * name for species.
	 * 
	 * @param speciesName Name of the species to filter for.
	 * @param speciesTag Aspect tag for species.
	 */
	public IsNotSpecies(String speciesName, String speciesTag)
	{
		this(speciesName);
		this._speciesTag = speciesTag;
	}

	@Override
	public boolean test(Agent agent)
	{
		String agentSpecies = agent.getString(this._speciesTag);
		return ! agentSpecies.equals(this._speciesName);
	}
	
	/**
	 * \brief return minimal description of the predicate
	 */
	@Override
	public String toString()
	{
		return "!" + _speciesName;
	}
	
}

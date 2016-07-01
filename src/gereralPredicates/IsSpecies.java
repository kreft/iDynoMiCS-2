/**
 * 
 */
package gereralPredicates;

import java.util.function.Predicate;

import agent.Agent;
import dataIO.XmlRef;

/**
 * \brief Predicate for filtering lists of Agents according to species.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class IsSpecies implements Predicate<Agent>
{
	private String _speciesTag = XmlRef.species;
	
	private String _speciesName;
	
	/**
	 * \brief Construct a species test with both the species name, assuming the
	 * tag name for species to be the default.
	 * 
	 * @param speciesName Name of the species to filter for.
	 */
	public IsSpecies(String speciesName)
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
	public IsSpecies(String speciesName, String speciesTag)
	{
		this(speciesName);
		this._speciesTag = speciesTag;
	}

	@Override
	public boolean test(Agent agent)
	{
		String agentSpecies = agent.getString(this._speciesTag);
		return agentSpecies.equals(this._speciesName);
	}
}
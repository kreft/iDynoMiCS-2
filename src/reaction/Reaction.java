package reaction;

import aspect.AspectInterface;

import java.util.Collection;
import java.util.Map;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public interface Reaction {

	/**
	 * @return Name of this reaction.
	 */
	String getName();

	/**
	 * @return Names of all variables in the rate of this reaction.
	 */
	Collection<String> getVariableNames();

	/**
	 * @return reactants involved in this reaction
	 */
	Collection<String> getReactantNames();

	/**
	 * @return all constituants (reactants and variables) involved in rate and
	 * stochiometry of reaction.
	 */
	Collection<String> getConstituentNames();

	/**
	 * @return the production rate of a given chemical species.
	 */
	double getProductionRate(Map<String, Double> concentrations,
							 String reactantName, AspectInterface subject);

}
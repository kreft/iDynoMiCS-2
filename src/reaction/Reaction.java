package reaction;

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
	 * @return This reaction's whole stoichiometric dictionary (given standard 
	 * concentrations of 1 mol/L at pH 7.0).
	 */
	Map<String, Double> getStoichiometryAtStdConcentration();

	/**
	 * @return the production rate of a given chemical species.
	 */
	double getProductionRate(Map<String, Double> concentrations, 
			String reactantName);

}
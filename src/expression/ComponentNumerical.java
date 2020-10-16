/**
 * 
 */
package expression;

import expression.Component;

/**
 * \brief Abstract class for any component of a mathematical expression.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public abstract class ComponentNumerical extends Component
{
	/**
	 * \brief Differentiate this mathematical expression with respect to the
	 * given component, be it a variable or a constant.
	 * 
	 * @param withRespectTo {@code String} name of the component that this
	 * mathematical expression should be differentiated with respect to.
	 * @return New {@code Component} describing the differential of this
	 * mathematical expression with respect to the given component name.
	 */
	public abstract ComponentNumerical differentiate(String withRespectTo);
}
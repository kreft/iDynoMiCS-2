/**
 * 
 */
package grid;

/**
 * Label for an array. 
 */
public enum ArrayType
{
	/**
	 * The concentration of, e.g., a solute.
	 */
	CONCN,
	/**
	 * The diffusion coefficient of a solute. For example, this may be
	 * lower inside a biofilm than in the surrounding water.
	 */
	DIFFUSIVITY,
	/**
	 * A measure of how well-mixed a solute is. A diffusion-reaction should
	 * ignore where this is above a certain threshold.
	 */
	WELLMIXED,
	/**
	 * The rate of production of this solute. Consumption is described by
	 * negative production.
	 */
	PRODUCTIONRATE,
	/**
	 * Laplacian operator.
	 */
	LOPERATOR,
	/**
	 * Dummy array for estimating how exposed agents are to detachment.
	 */
	DETACHABILITY,
}
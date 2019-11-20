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
	 * The concentration of, e.g., a solute. This is always in terms of mass
	 * per volume (mass<sup>1</sup> length<sup>-3</sup>).
	 */
	CONCN,
	/**
	 * The diffusion coefficient of a solute. For example, this may be
	 * lower inside a biofilm than in the surrounding water. This is always in
	 * terms of area per time (length<sup>2</sup> time<sup>-1</sup>).
	 */
	DIFFUSIVITY,
	/**
	 * A measure of how well-mixed a solute is. A diffusion-reaction should
	 * ignore where this is above a certain threshold. This is an arbitrary
	 * scale with no relation to physical units.
	 */
	WELLMIXED,
	/**
	 * length<sup>-3</sup>.<br><br>
	 * The rate of production of this solute. Consumption is described by
	 * negative production. This is always in terms of mass per time 
	 * (mass<sup>1</sup> time<sup>-1</sup>).
	 */
	PRODUCTIONRATE,
	/**
	 * Overall rate of change. Generally used internally within a PDE solver
	 * and not elsewhere.
	 */
	CHANGERATE,
	/**
	 * The local truncation error is a measure of the error remaining in a 
	 * numerical approximation to a solution. Generally used only internally
	 * within a PDE solver.
	 * 
	 * <p>In <i>Numerical Recipes in C</i> Chapter 19.6, this is referred to by
	 * the symbol <b>τ</b>.</p>
	 */
	LOCALERROR,
	/**
	 * The relative error is the correction needed to counteract errors caused
	 * by nonlinear source terms (e.g. nonlinear reaction kinetics). Generally
	 * used only internally within a PDE solver.
	 * 
	 * <p>In <i>Numerical Recipes in C</i> Chapter 19.6, this is referred to by
	 * the symbol <b>τ<sub>h</sub></b>.</p>
	 */
	RELATIVEERROR,
	/**
	 * The extra error caused by a non-linear source term: typically non-linear
	 * reaction kinetics. Generally used only internally within a PDE solver.
	 * 
	 * <p>In <i>Numerical Recipes in C</i> Chapter 19.6, this is referred to by
	 * the symbol <b>f</b> and the variable {@code rhs}.</p>
	 */
	NONLINEARITY,
}
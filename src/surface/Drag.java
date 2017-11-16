/**
 * 
 */
package surface;

import idynomics.Global;

/**
 * \brief Standard methods for drag forces under low Reynold's number.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public final class Drag
{
	/**
	 * \brief Pre-calculated factor for drag forces, equals <i>3*pi*2</i>.
	 * 
	 * <p>Assuming low particle Reynold's number (i.e. viscous forces dominate), the 
	 * drag on a spherical particle is <i>3 * pi * diameter * viscosity</i>. 
	 * Since we divide by this in {@link #dxdt(double)} and there we use radius
	 * (rather than diameter), use of this pre-calculated factor should improve
	 * efficiency a little.</p>
	 */
	private final static double DRAG_FACTOR = 6.0 * Math.PI;
	
	/**
	 * \brief Calculate the (dynamic) viscosity of water at the given
	 * temperature.
	 * 
	 * FIXME this does not account for/ allow for the use of other unit systems
	 * then SI.
	 * 
	 * <p>This formula and constants are taken from <a
	 * href="https://en.wikipedia.org/wiki/Viscosity#Water">Wikipedia</a>.</p>
	 * 
	 * <p>TODO Is standard pressure assumed here? <a
	 * href="http://www.nist.gov/data/PDFfiles/jpcrd243.pdf">This paper</a>
	 * goes into a lot of detail; probably more than we need here.</p>
	 * 
	 * @param temperature Temperature of the water, in Kelvin.
	 * @return Dynamic viscosity, in units of Pa s.
	 */
	public static double viscosityWater(double temperature)
	{
		return 2.414E-5 * Math.pow(10, 247.8/(temperature - 140.0));
	}
	
	/**
	 * \brief get the dynamic viscosity ( currently a global parameter )
	 * 
	 * TODO this is a quickfix to allow for non SI unit systems
	 * @return
	 */
	public static double dynamicViscosity()
	{
		if ( Global.dynamic_viscosity == 0.0 )
			return viscosityWater( 298.0 );
		return Global.dynamic_viscosity;
	}
	
	/**
	 * \brief Calculate the magnitude of the viscous drag on a sphere.
	 * 
	 * <p>The drag on a sphere from the surrounding fluid is calculated using
	 * Stoke's Law:</p>
	 * <p><i>v = sum(forces) / ( 3 * pi * diameter * viscosity)</i></p>
	 * 
	 * <p>See<ul>
	 * <li>Berg HC. Random walks in biology (Expanded edition). Princeton
	 * University Press; 1993. Pages 75-77</li>
	 * <li>Purcell EM. Life at low Reynolds number. <i>American Journal of
	 * Physics</i>. 1977;45: 3–11.</li>
	 * </ul></p>
	 * 
	 * @param radius Radius of the sphere, in microns.
	 * @param viscosity Dynamic viscosity of the surrounding fluid, in Pa s.
	 * @return Magnitude of the drag force on this sphere, in TODO units?
	 */
	public static double dragOnSphere(double radius, double viscosity)
	{
		return DRAG_FACTOR * radius * viscosity;
	}
}

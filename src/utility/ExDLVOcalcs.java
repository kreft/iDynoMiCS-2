package utility;

import aspect.AspectInterface;
import aspect.Event;
import idynomics.Global;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;

/**
 * TEST CODE: DO NOT USE OTHER THAN FOR SOME TEST CALCULATIONS!
 *
 */
public class ExDLVOcalcs
{
    public String BODY = AspectRef.agentBody;
    public String RADIUS = AspectRef.bodyRadius;
    public String GAMMA_LW = "gammaLW";
    public String GAMMA_ACID ="gammaAcid";
    public String GAMMA_BASE ="gammaBase";
    public String PSI = "psi";
    public String ZETA = "zeta";
    public String SCALED_FORCE = "scaledForce";
    public String CURRENT_DIST = "currentDistance";
    public String EXDLVO_CONSTANTS = "dlvoMap";
    public String SPECIES = XmlRef.species;

    static final double eps_r = 80.0;
    static final double eps_0 = 8.854e-12;
    static final double k = 1.38e-23;
    static final double Temp = 298.0;

    /* charge of the electron (e = 4.8 × 10–10 e.s.u.) */
    static final double e = 4.8e-10;

    /* minimum equilibrium distance */
    static final double lo = .157e-9;

    /* van der Waals surface tension compenent of water:
     * Good et al. J. Adhesion Sci. Technol. 4, 602 1990 */
    static final double sqrtgLWW = Math.sqrt( 21.8e-3 );
    static final double sqrtgAcidW = Math.sqrt( 25.5e-3 );
    static final double sqrtgBaseW = Math.sqrt( 25.5e-3 );

    static final double ionic_strength = 0.05;

    /* Inverse debye length assuming water at room temperature and an Ionic
     * strength of 85.47 mol/m3, for now we assume constant debye length. */
    static final double kap = 1 / ( Math.sqrt( ( eps_r * k * Temp ) /
            ( 4 * Math.PI* e * e * ionic_strength ) ) * 1.0e-9 );

    /* Bjerrum length for water at room temperature */
    static final double lamb = 6e-10;


    /** input */
    static final double distance = 0.01;
    static final double radius = Math.cbrt(3* (1.0E-18/0.2) / (4*Math.PI) );





    /** mJ m-2 */
    static final double base1 = 20.0;
    static final double base2 = 20.0;
    static final double acid1 = 0.5;
    static final double acid2 = 0.5;

    static final double lw1 = 40.0;
    static final double lw2 = 40.0;
    static final double psi1 = -0.023;
    static final double psi2 = -0.023;

    static double sqrtlw1 = Math.sqrt(lw1);
    static double sqrtlw2 = Math.sqrt(lw2);

    public static void main(String[] args)
    {
        /* get ionic strength from the compartment */
		/* Compartment c = ((Agent) initiator).getCompartment();
		if (c.environment.isSoluteName("ionic"))
			ionic_strength = c.environment.getAverageConcentration("ionic"); */

        /* cell-cell distance */
        double h = 1.0e-6 * 0.0;
        /* effective distance */
        if (h < lo)
            h = lo;

        /* radius (formulas for equal radii) */
        double r = 1.0e-6 * radius;
		/* effective radius (when using unequal radii formula set)
		double r_eff = 1e-6 / ( ( 1 / initiator.getDouble(RADIUS) ) +
				( 1 / compliant.getDouble(RADIUS) ) ); */

        /* van der Waals component */
        /* van Oss et al. Langmuir 4, 884 1988 /
         * van Oss Interfacial Forces in Aqueous Media, 2006 pp24
         * NOTE: (-12 * -2)/12 = 2   */
        double fvdw = ( r * 2.0 * Math.PI * Math.pow( lo , 2.0  ) *
                ( Math.sqrt( lw1 ) - sqrtgLWW ) *
                ( Math.sqrt( lw2 ) - sqrtgLWW ) ) /
                Math.pow( h, 2.0 ) ;

        /* electric double layer component */
        /* psi square since we can use the geometric mean for interacting
         * values of Psi ( sprt( psia * psib ) ) */

        /* For moderate values of ζ (i.e., not higher than ≈ 25 to 50 mV)
         * vanOss2006 pp53 */
        double fel = -.5 * eps_r * eps_0 * kap * r *
                psi1 * psi2 *
                Math.log( 1 + Math.exp( -kap*h ) );

        double dgel = -.5 * eps_r * eps_0 * r *
                psi1 * psi2 *
                Math.log( 1 + Math.exp( -kap*h ) );

        /* acid-base interaction component */
        /* square root of acid and base surface tension component of water,
         * both agent types involved  */
        double sqrtgAcidA = Math.sqrt( acid1 );
        double sqrtgBaseA = Math.sqrt( base1 );
        double sqrtgAcidB = Math.sqrt( acid2 );
        double sqrtgBaseB = Math.sqrt( base2 );

        /* parallel interaction energy at minimum equilibrium distance:
         * van Oss Interfacial forces in aqueous media 2006 pp44 */
        double dGAB = 2 * r * (
                ( sqrtgAcidA - sqrtgAcidB ) * ( sqrtgBaseA - sqrtgBaseB ) -
                        ( sqrtgAcidA - sqrtgAcidW ) * ( sqrtgBaseA - sqrtgBaseW ) -
                        ( sqrtgAcidB - sqrtgAcidW ) * ( sqrtgBaseB - sqrtgBaseW ) );

        double dGLW = -( r * 24.0 * Math.PI * Math.pow( lo , 2.0  ) *
                ( Math.sqrt( lw1 ) - sqrtgLWW ) *
                ( Math.sqrt( lw2 ) - sqrtgLWW ) ) /
                h;

        /* van Oss Interfacial forces in aqueous media 2006 pp83 */
        double fab = - Math.PI * dGAB * Math.exp( ( lo-h ) / lamb );

        /* total force converted back to simulation units (1 N = 1 kg⋅m⋅s−2) */
        double out = (fvdw + fel + fab) * 1e21;

        /* filter out short-range repulsion (handled by soft repulsion model).*/
        if( out < 0.0 && h < 1.0e-7 )
            out = 0.0;
        System.out.println("dGAB: " + dGAB );
        System.out.println("dGLW: " + dGLW );
        System.out.println("dGEL: " + dgel );
        System.out.println("dGtot: " + (dGAB+dGLW+dgel) );
        System.out.println("force: " + out );
        System.out.println("Fvdw: " + fvdw );
        System.out.println("Fab: " + fab );
        System.out.println("Fel: " + fel );

        System.out.println("zeta: " + getZeta(psi1,radius,kap));
        /* optional curve smoothing for solver stability at larger t-step */
        /* Math.tanh( ho / 0.157e-9 ) */
    }

    static double lw12(double sqrt1, double sqrt2) {
        return Math.pow(sqrt1-sqrt2,2.0);
    }

    static double getPsi(double zeta, double radius, double kappa)
    {

            double z = 5.0e-10;
            return zeta *
                    ( 1.0 + z/radius ) * Math.exp(kappa*z);

    }

    static double getZeta(double psi, double radius, double kappa) {
        double z = 5.0e-10;
        return psi / (( 1.0 + z/radius ) * Math.exp(kappa*z));
    }
}
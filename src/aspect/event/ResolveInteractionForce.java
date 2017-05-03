package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Event;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;

/**
 * Simple Extended DLVO based interaction energy calculation for coccoid agents
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ResolveInteractionForce extends Event
{
	
	public String BODY = AspectRef.agentBody;
	public String RADIUS = AspectRef.bodyRadius;
	public String GAMMA_LW = "gammaLW";
	public String GAMMA_ACID ="gammaAcid";
	public String GAMMA_BASE ="gammaBase";
	public String PSI = "psi";
	public String SCALED_FORCE = "scaledForce";
	public String CURRENT_DIST = "currentDistance";
	
	@Override
	public void start(AspectInterface initiator, AspectInterface compliant, 
			Double timeStep) {

		/* cell-cell distance */
		double h = 1e-6 * initiator.getDouble(CURRENT_DIST);
		
		if (h < 2*0.157e-9)
		{
			initiator.set(SCALED_FORCE, 0.0 );
			return;
		}
		
		/* effective radius */
		double r = 2e-6 / ( ( 1 / initiator.getDouble(RADIUS) ) + 
				( 1 / compliant.getDouble(RADIUS) ) );
		
		/* van der Waals component */
		/* van der Waals surface tension compenent of water: 
		 * Good et al. J. Adhesion Sci. Technol. 4, 602 1990 */
		double sqrtgLWW = Math.sqrt( 21.8e-3 ); 
		double lo = .157e-9; /* minimum equilibrium distance */
		/* van Oss et al. Langmuir 4, 884 1988 / 
		 * van Oss Interfacial Forces in Aqueous Media, 2006 pp24 
		 * NOTE: -12 * -2 = 24  
		 * NOTE: there was an error in deltaG in the first batch, misread equation*/
		double effectiveHamaker = 24.0 * Math.PI * Math.pow( lo , 2  ) * 
				( Math.sqrt( initiator.getDouble(GAMMA_LW)*1e-3 ) - sqrtgLWW ) *
				( Math.sqrt( compliant.getDouble(GAMMA_LW)*1e-3 ) - sqrtgLWW );
		double fvdw = ( effectiveHamaker * r ) / ( 12 * Math.pow( h, 2 ) );

		/* electric doulbe layer component */
		/* dielectric constant water / relative permittivity water */
		double eps = 80 * 8.854e-12;
		/* Inverse debye length assuming water at room temperature and 
		 * an Ionic strength of 85.47 mol/m3, for now we assume constant debye 
		 * length */
		double kap = 1e9;
		double e = 4.8e-10;
		double psi = Math.sqrt(initiator.getDouble(PSI) * compliant.getDouble(PSI));
		double kT = 1.38e-23 * 298;
		/* NOTE: for (semi) identical radius
		 * double fel = ( -.5 * eps * r * kap * initiator.getDouble(PSI) * 
				compliant.getDouble(PSI) * Math.log( 1 + Math.exp( -kap*h ) ) ); */
		
		double fel = - kap * r * 32 * Math.PI * eps * 
				Math.pow(( kT / e), 2) *
				Math.pow( Math.tanh((e*psi)/(4*kT)), 2) * Math.exp( -kap * h );

		/* acid-base interaction component */
		double lamb = 7e-10; /* Bjerrum length for water at room temperature */
		
		/* square root of acid and base surface tension component of water,
		 * both agent types involved  */
		double sqrtgAcidW = Math.sqrt( 25.5e-3 );
		double sqrtgBaseW = Math.sqrt( 25.5e-3 );
		double sqrtgAcidA = Math.sqrt( initiator.getDouble( GAMMA_ACID )*1e-3 );
		double sqrtgBaseA = Math.sqrt( initiator.getDouble( GAMMA_BASE )*1e-3 );
		double sqrtgAcidB = Math.sqrt( compliant.getDouble( GAMMA_ACID )*1e-3 );
		double sqrtgBaseB = Math.sqrt( compliant.getDouble( GAMMA_BASE )*1e-3 );

		/* parallel interaction energy at minimum equilibrium distance:
		 * van Oss Interfacial forces in aqueous media 2006 pp44 */
		double dGAB = 2 * ( 
				( sqrtgAcidA - sqrtgAcidB ) * ( sqrtgBaseA - sqrtgBaseB ) -
				( sqrtgAcidA - sqrtgAcidW ) * ( sqrtgBaseA - sqrtgBaseW ) -
				( sqrtgAcidB - sqrtgAcidW ) * ( sqrtgBaseB - sqrtgBaseW ) );

		/* van Oss Interfacial forces in aqueous media 2006 pp83 */
		double fab = - Math.PI * r * dGAB * Math.exp( ( lo-h ) / lamb );
		
		initiator.set(SCALED_FORCE, (fvdw + fel + fab) * 3.6e24 );
	}
}

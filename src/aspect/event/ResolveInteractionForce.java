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
	
	@Override
	public void start(AspectInterface initiator, AspectInterface compliant, 
			Double timeStep) {
		
		Shape shape = (Shape) ( (Agent) initiator ).getCompartment().getShape();

		/* cell-cell distance */
		double h = Vector.normEuclid(shape.getMinDifferenceVector(
				( (Body) initiator.getValue(BODY) ).getPosition(0), 
				( (Body) compliant.getValue(BODY) ).getPosition(0) ) );
		
		/* effective radius */
		double r = 1 / ( ( 1 / initiator.getDouble(RADIUS) ) + 
				( 1 / compliant.getDouble(RADIUS) ) );
		
		/* van der Waals component */
		/* van der Waals surface tension compenent of water: 
		 * Good et al. J. Adhesion Sci. Technol. 4, 602 1990 */
		double sqrtgLWW = Math.sqrt( 21.8e-3 ); 
		/* van Oss et al. Langmuir 4, 884 1988 / 
		 * van Oss Interfacial Forces in Aqueous Media, 2006 */
		double effectiveHamaker = 24.0 * Math.PI * 0.157e-9 * 
				( Math.sqrt( initiator.getDouble(GAMMA_LW) ) - sqrtgLWW ) *
				( Math.sqrt( compliant.getDouble(GAMMA_LW) ) - sqrtgLWW );
		double fvdw = - ( effectiveHamaker * r ) / ( 12 * Math.pow( h, 2 ) );

		/* electric doulbe layer component */
		/* dielectric constant water / relative permittivity water */
		double eps = 80 * 8.854e-12;
		/* Inverse debye length assuming water at room temperature and 
		 * an Ionic strength of 85.47 mol/m3, for now we assume constant debye 
		 * length */
		double kap = 1e-9;
		double fel = ( -.5 * eps * r * kap * initiator.getDouble(PSI) * 
				compliant.getDouble(PSI) * Math.log( 1 + Math.exp( -kap*h ) ) );

		/* acid-base interaction component */
		double lamb = 7e-10; /* Bjerrum length for water at room temperature */
		double lo = .157e-9; /* minimum equilibrium distance */
		
		/* square root of acid and base surface tension component of water,
		 * both agent types involved  */
		double sqrtgAcidW = Math.sqrt( 25.5 );
		double sqrtgBaseW = Math.sqrt( 25.5 );
		double sqrtgAcidA = Math.sqrt( initiator.getDouble( GAMMA_ACID ) );
		double sqrtgBaseA = Math.sqrt( initiator.getDouble( GAMMA_BASE ) );
		double sqrtgAcidB = Math.sqrt( compliant.getDouble( GAMMA_ACID ) );
		double sqrtgBaseB = Math.sqrt( compliant.getDouble( GAMMA_BASE ) );

		/* parallel interaction energy at minimum equilibrium distance:
		 * van Oss Interfacial forces in aqueous media 2006 */
		double dGAB = 2 * ( 
				( sqrtgAcidW - sqrtgAcidA ) * ( sqrtgBaseW - sqrtgBaseA ) -
				( sqrtgAcidW - sqrtgAcidB ) * ( sqrtgBaseW - sqrtgBaseB ) -
				( sqrtgAcidA - sqrtgAcidB ) * ( sqrtgBaseA - sqrtgBaseB ) );

		double fab = - ( Math.PI * r * dGAB * Math.exp( ( lo-h ) / lamb ) );
		
		initiator.set(SCALED_FORCE, fvdw + fel + fab );
	}
}

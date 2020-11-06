package aspect.event;

import agent.Agent;
import agent.Body;
import agent.Body.Morphology;
import aspect.AspectInterface;
import aspect.Event;
import aspect.methods.DivisionMethod;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import surface.Link;
import surface.Point;
import utility.ExtraMath;
import utility.Helper;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class FillialRodShift extends DivisionMethod
{
	
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		if ( ! this.shouldChange(initiator) )
			return;
		
		if( this.shouldDevide(initiator) )
		{
			/* Make one new agent, copied from the mother.*/
			compliant = new Agent((Agent) initiator);
			/* Transfer an appropriate amount of mass from mother to daughter. */
			DivisionMethod.transferMass(initiator, compliant);
			this.shiftBodies((Agent) initiator, (Agent) compliant);
			/* The bodies of both cells may now need updating. */
			updateAgents((Agent) initiator,(Agent) compliant);
		}
		else if ( initiator.isAspect(AspectRef.agentBody) && 
				((Body) initiator.getValue(AspectRef.agentBody)).getMorphology()
						!= Morphology.BACILLUS )
		{
			shiftMorphology((Agent) initiator);	
			/* The bodies of both cells may now need updating. */
			updateAgents((Agent) initiator,null);
		}
	}
	
	protected boolean shouldChange(AspectInterface initiator)
	{
		/* Find the agent-specific variable to test (mass, by default).	 */
		Object mumMass = initiator.getValue(AspectRef.agentMass);
		double variable = Helper.totalMass(mumMass);
		/* Find the threshold that triggers division. */
		double threshold = Double.MAX_VALUE;
		if ( initiator.isAspect(AspectRef.shiftMass) )
			threshold = initiator.getDouble(AspectRef.shiftMass);
		return (variable > threshold);
	}
	
	protected boolean shouldDevide(AspectInterface initiator)
	{
		/* Find the agent-specific variable to test (mass, by default).	 */
		Object mumMass = initiator.getValue(AspectRef.agentMass);
		double variable = Helper.totalMass(mumMass);
		/* Find the threshold that triggers division. */
		double threshold = Double.MAX_VALUE;
		if ( initiator.isAspect(AspectRef.divisionMass) )
			threshold = initiator.getDouble(AspectRef.divisionMass);
		return (variable > threshold);
	}
	/**
	 * \brief Shift the bodies of <b>mother</b> to <b>daughter</b> in space, so
	 * that they do not overlap.
	 * 
	 * @param initiator An agent.
	 * @param daughter Another agent, whose body overlaps a lot with that of
	 * <b>mother</b>.
	 */
	protected void shiftMorphology(Agent initiator)
	{
		Body momBody = (Body) initiator.get(AspectRef.agentBody);
		Point q = null, p = null;
		double[] originalPos, shift;
		
		if(  initiator.getBoolean(AspectRef.directionalDivision) &! 
				momBody.getLinks().isEmpty())
		{
			double[] direction = null;
			Link link = momBody.getLinks().get(0);
			for( Link l : momBody.getLinks() )
				if(l.getMembers().size() < 3)
					link = l;
			AspectInterface other = null;

			for( AspectInterface a : link.getMembers())
				if( a != initiator)
				{
					other = a;
					direction = ((Body) a.getValue(AspectRef.agentBody)).
							getClosePoint(momBody.getCenter()).getPosition();
					continue;
				}
			
			Body otherBody = (Body) other.getValue(AspectRef.agentBody);
			
			originalPos = momBody.getClosePoint(otherBody.getCenter()).getPosition();
			shift = initiator.getCompartment().getShape().getMinDifferenceVector(
							direction, originalPos);
			
			p = momBody.getClosePoint(otherBody.getCenter());

			
			for( Link l : momBody.getLinks() )
			{
				if( l.getMembers().size() < 3 && l.getMembers().contains(initiator)
						&& l.getMembers().contains(other) )
				{
				momBody.unLink(link);
				otherBody.unLink(link);
				continue;
				}
			}

			/* update torsion links */
			for(Link l : momBody.getLinks() )
				if(l.getMembers().size() > 2)
				{
					int i;
					i = l.getMembers().indexOf(other);
					l.update(i, q);
				}
			
			for(Link l :((Body) other.getValue(AspectRef.agentBody)).getLinks())
				if(l.getMembers().size() > 2)
				{
					int i;
					i = l.getMembers().indexOf(initiator);
					l.update(i, q);
				}
		}
		else
		{
			originalPos = momBody.getPosition(0);
			shift = Vector.randomPlusMinus(originalPos.length, 
					initiator.getDouble(AspectRef.bodyRadius));
			
			p = momBody.getPoints().get(0);
		}
		
		p.setPosition(Vector.minus(originalPos, Vector.times(shift,0.4)));
		
		q = new Point(Vector.add(originalPos, Vector.times(shift,0.4)));
		
		/* reshape */
		momBody.getPoints().add(new Point(q));
		momBody.getSurfaces().clear();
		momBody.assignMorphology(Morphology.BACILLUS.name());
		momBody.constructBody(1.0, 
				initiator.getDouble(AspectRef.transientRadius) );
	}
	
	public void shiftBodies(Agent mother, Agent daughter)
	{

		Body momBody = (Body) mother.get(AspectRef.agentBody);
		Body daughterBody = (Body) daughter.get(AspectRef.agentBody);
		Point p = null, q = null;
		boolean unlink = ExtraMath.getUniRandDbl() < 
				(double) mother.getOr(AspectRef.unlinkProbabillity, 0.0);
		
		daughterBody.clearLinks();
		
		if(  mother.getBoolean(AspectRef.directionalDivision) &! 
				momBody.getLinks().isEmpty())
		{
			AspectInterface otherA = null;
			AspectInterface otherB = null;
			
			for( Link l : momBody.getLinks() )
			{
				if(l.getMembers().size() < 3)
				for( AspectInterface a : l.getMembers())
					if( a != mother)
					{
						if( a != otherA)
							otherA = a;
						else
							otherB = a;
					}
			}
			
			Body otherABody = (Body) otherA.getValue(AspectRef.agentBody);
			p = momBody.getClosePoint(otherABody.getCenter());
			Body otherBBody = (Body) otherB.getValue(AspectRef.agentBody);
			q = daughterBody.getClosePoint(otherBBody.getCenter());
			
			for( Point w : daughterBody.getPoints() )
			{
				if(w != q)
					daughterBody.getPoints().remove(w);
			}
			for( Point w : momBody.getPoints() )
			{
				if(w != p)
					momBody.getPoints().remove(w);
			}
			
			for( Link l : momBody.getLinks() )
			{
				if( l.getMembers().size() < 3 && l.getMembers().contains(mother)
						&& l.getMembers().contains(otherB) )
				{
					momBody.unLink(l);
					otherBBody.unLink(l);
					continue;
				}
			}
			for( Link l : daughterBody.getLinks() )
			{
				if( l.getMembers().size() < 3 && l.getMembers().contains(mother)
						&& l.getMembers().contains(otherA) )
				{
					daughterBody.unLink(l);
					otherABody.unLink(l);
					continue;
				}
			}
			
			Link.linLink((Agent) otherB, daughter);
			
			/* update torsion links */
			for(Link l : momBody.getLinks() )
				if(l.getMembers().size() > 2)
				{
					int i = 0;
					if( l.getMembers().get(i) != mother )
						i = 2;
					l.addMember(i, daughter);
					l.update(i, daughterBody.getClosePoint(momBody.getCenter()));
				}
			
			for(Link l : otherBBody.getLinks())
				if(l.getMembers().size() > 2)
				{
					if( !unlink )
					{
						int i = 0;
						if( l.getMembers().get(i) != mother )
							i = 2;
						l.addMember(i, daughter);
						l.update(i, daughterBody.getClosePoint(otherBBody.getCenter()));
					}
					else
						otherBBody.unLink(l);
				}
			
			if( !unlink )
				Link.torLink((Agent) otherB, daughter, mother);
		}
		else
		{
			momBody.getPoints().remove(1);
			daughterBody.getPoints().remove(0);
		}
			/* reshape */
			momBody.getSurfaces().clear();
			momBody.assignMorphology(Morphology.COCCOID.name());
			momBody.constructBody(1.0, 
					mother.getDouble(AspectRef.bodyRadius) );
			
			daughterBody.getSurfaces().clear();
			daughterBody.assignMorphology(Morphology.COCCOID.name());
			daughterBody.constructBody(1.0, 
					daughter.getDouble(AspectRef.bodyRadius) );
			
			if( !unlink )
				Link.linLink((Agent) mother, daughter);
	}
}

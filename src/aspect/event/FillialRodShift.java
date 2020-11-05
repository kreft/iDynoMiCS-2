package aspect.event;

import agent.Agent;
import agent.Body;
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
		else
		{
			/* Update their bodies, if they have them. */
			if ( initiator.isAspect(AspectRef.agentBody) && 
					initiator.isAspect(AspectRef.bodyRadius) )
				shiftMorphology((Agent) initiator);	
			/* The bodies of both cells may now need updating. */
			updateAgents((Agent) initiator,(Agent) compliant);
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
		Point q;
		
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
			
			double[] originalPos = momBody.getClosePoint(otherBody.getCenter()).getPosition();
			double[] shift = initiator.getCompartment().getShape().getMinDifferenceVector(
							direction, originalPos);
			
			Point p = momBody.getClosePoint(otherBody.getCenter());
			p.setPosition(Vector.minus(originalPos, Vector.times(shift,0.4)));
			
			q = new Point(Vector.add(originalPos, Vector.times(shift,0.4)));
			
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
			double[] originalPos = momBody.getPosition(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.4*initiator.getDouble(AspectRef.bodyRadius));
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			q = new Point(Vector.add(originalPos, shift));
		}
		
		/* reshape */
		momBody.getPoints().add(q);
		momBody.getSurfaces().clear();
		momBody.assignMorphology("Rod");
		momBody.constructBody(0.0, 
				initiator.getDouble(AspectRef.transientRadius) );
	}
	
	public void shiftBodies(Agent mother, Agent daughter)
	{

		Body momBody = (Body) mother.get(AspectRef.agentBody);
		Body daughterBody = (Body) daughter.get(AspectRef.agentBody);
		
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
			Point p = momBody.getClosePoint(otherABody.getCenter());
			Body otherBBody = (Body) otherB.getValue(AspectRef.agentBody);
			Point q = daughterBody.getClosePoint(otherBBody.getCenter());
			
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
			
			if( !unlink )
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
	}

}

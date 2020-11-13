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
		
		if ( initiator.isAspect(AspectRef.agentBody) && 
				((Body) initiator.getValue(AspectRef.agentBody)).getMorphology()
						!= Morphology.BACILLUS )
		{
			shiftMorphology((Agent) initiator);
			updateAgents((Agent) initiator,null);
		}
		
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
		Body otherABody = null;
		Body otherBBody = null;
		Point q = null, p = null;
		double[] originalPos, shift;
		AspectInterface otherA = null;
		AspectInterface otherB = null;
		Link linkA = null;
		Link linkB = null;
		
		if(  initiator.getBoolean(AspectRef.directionalDivision) &! 
				momBody.getLinks().isEmpty())
		{
			double[] directionA = null;
			double[] directionB = null;
			
			for( Link l : momBody.getLinks() )
				if(l.getMembers().size() < 3)
					if( linkA == null )
						linkA = l;
					else
						linkB= l;

			for( AspectInterface a : linkA.getMembers())
				if( a != initiator && otherA == null )
				{
					otherA = a;
					directionA = ((Body) a.getValue(AspectRef.agentBody)).
							getClosePoint(momBody.getCenter()).getPosition();
				}
				
			if( linkB != null )
				for( AspectInterface a : linkB.getMembers())
					if( a != initiator && otherB == null )
					{
						otherB = a;
						directionB = ((Body) a.getValue(AspectRef.agentBody)).
								getClosePoint(momBody.getCenter()).getPosition();
	
						otherBBody = (Body) otherB.getValue(AspectRef.agentBody);
					}
			
			otherABody = (Body) otherA.getValue(AspectRef.agentBody);
			
			originalPos = momBody.getClosePoint(otherABody.getCenter()).getPosition();
			shift = initiator.getCompartment().getShape().getMinDifferenceVector(
							directionA, originalPos);
			
			p = momBody.getClosePoint(otherABody.getCenter());
			q = new Point(p);

			p.setPosition(Vector.minus(originalPos, Vector.times(shift,0.4)));
			q.setPosition(Vector.add(originalPos, Vector.times(shift,0.4)));
			
			/* update torsion links */
			for(Link l : momBody.getLinks() )
				if(l.getMembers().size() > 2)
				{
					int i;
					i = l.getMembers().indexOf(otherA);
					l.update(i, q);
				}
			
			if( otherA != null)
			{
				for(Link l : otherABody.getLinks() )
					if(l.getMembers().size() > 2)
					{
						int i;
						i = l.getMembers().indexOf(initiator);
						l.update(i, q);
					}
			}
			
			if ( otherB != null )
			{
				for(Link l : otherBBody.getLinks() )
					if(l.getMembers().size() > 2)
					{
						int i;
						i = l.getMembers().indexOf(initiator);
						l.update(i, p);
					}
			}
		}
		else
		{
		/* if we are not linked yet */
			originalPos = momBody.getPosition(0);
			shift = Vector.randomPlusMinus(originalPos.length, 
					initiator.getDouble(AspectRef.bodyRadius));
			
			p = momBody.getPoints().get(0);
			q = new Point(p);
			
			p.setPosition(Vector.minus(originalPos, Vector.times(shift,0.4)));
			q.setPosition(Vector.add(originalPos, Vector.times(shift,0.4)));
		}
		

		
		/* reshape */
		momBody.getPoints().add(new Point(q));
		momBody.getSurfaces().clear();
		momBody.assignMorphology(Morphology.BACILLUS.name());
		momBody.constructBody(1.0, 
				initiator.getDouble(AspectRef.transientRadius) );
		
		if(  initiator.getBoolean(AspectRef.directionalDivision) &! 
				momBody.getLinks().isEmpty())
		{
			for( Link l : momBody.getLinks() )
			{
				if( l.getMembers().size() < 3 && l.getMembers().contains(initiator)
						&& l.getMembers().contains(otherA) )
				{

				momBody.unLink(linkA);
				otherABody.unLink(linkA);
				Link.linLink((Agent) initiator, (Agent) otherA);
				continue;
				}
			}
			Link.torLink((Agent) otherA, initiator, initiator);
		}
	}
	
	public void shiftBodies(Agent mother, Agent daughter)
	{

		Body momBody = (Body) mother.get(AspectRef.agentBody);
		Body daughterBody = (Body) daughter.get(AspectRef.agentBody);
		Point p = null, q = null, ghost = null;
		boolean unlink = ExtraMath.getUniRandDbl() < 
				(double) mother.getOr(AspectRef.unlinkProbabillity, 0.0);
		unlink = false;
		
		daughterBody.clearLinks();
		
		if(  mother.getBoolean(AspectRef.directionalDivision) &! 
				momBody.getLinks().isEmpty())
		{
			AspectInterface otherA = null;
			AspectInterface otherB = null;
			Body otherBBody = null;
			Body otherABody = null;

			
			/* this is where it goes wrong */
			for( Link l : momBody.getLinks() )
			{
				if(l.getMembers().size() < 3)
				for( AspectInterface a : l.getMembers())
					if( a != mother)
					{
						if( otherA == null)
							otherA = a;
						else if ( otherB == null && a != otherA )
							otherB = a;
					}
			}
			if( otherA != null )
			{
				int i = 0;
				otherABody = (Body) otherA.getValue(AspectRef.agentBody);
				p = momBody.getClosePoint(otherABody.getCenter());
				q = daughterBody.getPoints().get(i);
				if( Vector.equals( p.getPosition(), q.getPosition() ))
				{
					i++;
					q = daughterBody.getPoints().get(i);
				}
				ghost = momBody.getPoints().get(i);
				momBody.getPoints().remove(i);
				daughterBody.getPoints().remove(1-i);
			}
			else
			{
				if(momBody.getPoints().size() > 1 )
				{
					ghost = momBody.getPoints().get(1);
					momBody.getPoints().remove(1);
					daughterBody.getPoints().remove(0);
				}
			}
			if( otherB != null )
			{
				otherBBody = (Body) otherB.getValue(AspectRef.agentBody);
			}
			
			if( otherB != null )
			{
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
			}
			if( otherA != null )
			{
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
			}
			
			if( otherB != null &! unlink)
			{
				Link.linLink((Agent) otherB, daughter);
			}
			
			int linkp = 0;
			int j = 0;
			/* update torsion links */
			for(Link l : momBody.getLinks() )
			{
				if(l.getMembers().size() > 2 && j == 0)
				{
					l.update(1, momBody.getPoints().get(0));
					int i = 0; 
					if( l.getMembers().get(i) != mother )
						i = 2;
					l.addMember(i, daughter);
					l.update(i, daughterBody.getPoints().get(0));
					j++;
				}
				if(l.getMembers().size() > 2 && j == 1)
				{
					j++;
					linkp = momBody.getLinks().indexOf(l);
				}
			}
			
			if (j == 2)
				momBody.getLinks().remove(linkp);
			
			if( otherB != null )
			{
				for(Link l : otherBBody.getLinks())
					if(l.getMembers().size() > 2)
					{
						if( !unlink )
						{
							l.update(1, otherBBody.getClosePoint(daughterBody.getCenter()));
							int i = 0;
							if( l.getMembers().get(i) != mother )
								i = 2;
							l.addMember(i, daughter);
							l.update(i, daughterBody.getPoints().get(0));
						}
						else
							otherBBody.unLink(l);
					}
			
				if( !unlink )
				{
					Link.torLink((Agent) otherB, daughter, mother);
				}
			}
		}
		else
		{
			if(momBody.getPoints().size() > 1 )
			{
				momBody.getPoints().remove(1);
				daughterBody.getPoints().remove(0);
			}
		}

		if(momBody.getPoints().size() > 1 )
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

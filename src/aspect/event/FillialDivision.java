package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.methods.DivisionMethod;
import expression.Expression;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import surface.LinearSpring;
import surface.Link;
import surface.Point;
import surface.Spring;
import surface.TorsionSpring;
import utility.ExtraMath;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class FillialDivision extends DivisionMethod
{
	/**
	 * \brief Shift the bodies of <b>mother</b> to <b>daughter</b> in space, so
	 * that they do not overlap.
	 * 
	 * @param mother An agent.
	 * @param daughter Another agent, whose body overlaps a lot with that of
	 * <b>mother</b>.
	 */
	protected void shiftBodies(Agent mother, Agent daughter)
	{
		Body momBody = (Body) mother.get(AspectRef.agentBody);
		Body daughterBody = (Body) daughter.get(AspectRef.agentBody);
		
		boolean unlink = ExtraMath.getUniRandDbl() < 
				(double) mother.getOr(AspectRef.unlinkProbabillity, 0.0);
		
		daughterBody.clearLinks();
		
		if(  mother.getBoolean(AspectRef.directionalDivision) &! 
				momBody.getLinks().isEmpty())
		{
			double[] direction = null;
			Link link = momBody.getLinks().get(0);
			for( Link l : momBody.getLinks() )
				if(l.getMembers().size() < 3)
					link = l;
			AspectInterface other = null;

			for( AspectInterface a : link.getMembers())
				if( a != mother)
				{
					other = a;
					direction = ((Body) a.getValue(AspectRef.agentBody)).
							getClosePoint(momBody.getCenter()).getPosition();
					continue;
				}
			
			Body otherBody = (Body) other.getValue(AspectRef.agentBody);
			
			double[] originalPos = momBody.getClosePoint(otherBody.getCenter()).getPosition();
			double[] shift = mother.getCompartment().getShape().getMinDifferenceVector(
							direction, originalPos);
			
			Point p = momBody.getClosePoint(otherBody.getCenter());
			p.setPosition(Vector.minus(originalPos, Vector.times(shift,0.4)));
			
			Point q = daughterBody.getClosePoint(momBody.getCenter());
			q.setPosition(Vector.add(originalPos, Vector.times(shift,0.4)));
			/* body has more points? */
			for( Point w : daughterBody.getPoints() )
			{
				if(w != q)
					q.setPosition(Vector.add(originalPos, Vector.times(shift, 0.6)));
			}
			
			for( Link l : momBody.getLinks() )
			{
				if( l.getMembers().size() < 3 && l.getMembers().contains(mother)
						&& l.getMembers().contains(other) )
				{
				momBody.unLink(link);
				otherBody.unLink(link);
				continue;
				}
			}
			
			if( !unlink )
				Link.linLink((Agent) other, daughter);
			
			/* update torsion links */
			for(Link l : momBody.getLinks() )
				if(l.getMembers().size() > 2)
				{
					int i;
					i = l.getMembers().indexOf(other);
					l.addMember(i, daughter);
					l.setPoint(i, daughterBody.getClosePoint(momBody.getCenter()), false);
				}
			
			for(Link l :((Body) other.getValue(AspectRef.agentBody)).getLinks())
				if(l.getMembers().size() > 2)
				{
					if( !unlink )
					{
						int i;
						i = l.getMembers().indexOf(mother);
						l.addMember(i, daughter);
						l.setPoint(i, daughterBody.getClosePoint(otherBody.getCenter()),false);
					}
					else
						otherBody.unLink(l);
				}
			if( !unlink )
				Link.torLink((Agent) other, daughter, mother);
		}
		else
		{
			double[] originalPos = momBody.getPosition(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.4*mother.getDouble(AspectRef.bodyRadius));
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.minus(originalPos, shift));
			/* body has more points? */
			for( Point w : daughterBody.getPoints() )
			{
				if(w != q)
					q.setPosition(Vector.add(originalPos, Vector.times(shift, 1.2)));
			}
		}
		Link.linLink(mother, daughter);
	}

}

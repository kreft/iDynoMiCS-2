package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.methods.DivisionMethod;
import expression.Expression;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
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
	private double shiftFrac = 0.25;
	/**
	 * \brief Shift the bodies of <b>mother</b> to <b>daughter</b> in space, so
	 * that they do not overlap.
	 * 
	 * @param initiator An agent.
	 * @param complient Another agent, whose body overlaps a lot with that of
	 * <b>mother</b>.
	 */
	protected void shiftBodies(Agent initiator, Agent complient)
	{

		Shape shape = initiator.getCompartment().getShape();
		Body iniBody = (Body) initiator.get( AspectRef.agentBody );
		Body comBody = (Body) complient.get( AspectRef.agentBody );
		double rShift = initiator.getDouble( AspectRef.bodyRadius ) * shiftFrac;
		boolean unlink = ExtraMath.getUniRandDbl() < 
				(double) initiator.getOr( AspectRef.unlinkProbabillity, 0.0 );
		
		comBody.clearLinks();
		
		if(  initiator.getBoolean( AspectRef.directionalDivision ) &! 
				iniBody.getLinks().isEmpty())
		{
			double[] direction = null;
			Link link = iniBody.getLinks().get(0);
			for( Link l : iniBody.getLinks() )
				if(l.getMembers().size() < 3)
					link = l;
			AspectInterface other = null;

			for( AspectInterface a : link.getMembers())
				if( a != initiator )
				{
					other = a;
					direction = ((Body) a.getValue( AspectRef.agentBody )).
							getClosePoint( iniBody.getCenter(shape), shape).
							getPosition();
					continue;
				}
			
			Body othBody = (Body) other.getValue( AspectRef.agentBody );
			
			double[] oriPos = iniBody.getClosePoint(
					othBody.getCenter( shape ), shape ).getPosition();
			double[] shift = initiator.getCompartment().getShape().
					getMinDifferenceVector(	direction, oriPos );
			
			Point p = iniBody.getClosePoint( othBody.getCenter(shape), shape);
			p.setPosition(Vector.minus( oriPos, Vector.times( shift, rShift )));
			
			Point q = comBody.getClosePoint( iniBody.getCenter(shape), shape);
			q.setPosition(Vector.add( oriPos, Vector.times( shift, rShift )));
			
			/* body has more points? */
			for( Point w : comBody.getPoints() )
			{
				if(w != q)
					q.setPosition( Vector.add( oriPos, 
							Vector.times( shift, rShift )));
			}
			
			for( Link l : iniBody.getLinks() )
			{
				if( l.getMembers().size() < 3 && 
						l.getMembers().contains( initiator )
						&& l.getMembers().contains( other ))
				{
					iniBody.unLink( link );
					othBody.unLink( link );
					continue;
				}
			}
			
			if( !unlink )
				Link.linLink( (Agent) other, complient );
			
			/* update torsion links */
			for( Link l : iniBody.getLinks() )
				if( l.getMembers().size() > 2 )
				{
					int i;
					i = l.getMembers().indexOf( other );
					l.addMember( i, complient );
					l.setPoint( i, comBody.getClosePoint(
							iniBody.getCenter( shape ), shape ), false );
				}
			
			for( Link l : othBody.getLinks() )
				if(l.getMembers().size() > 2)
				{
					if( !unlink )
					{
						int i;
						i = l.getMembers().indexOf( initiator );
						l.addMember( i, complient );
						l.setPoint(i, comBody.getClosePoint(
								othBody.getCenter( shape ), shape ), false );
					}
					else
						othBody.unLink(l);
				}
			
			if( !unlink )
				Link.torLink((Agent) other, complient, initiator);
		}
		else
		{
			double[] originalPos = iniBody.getPosition(0);
			double[] shift = Vector.randomPlusMinus( originalPos.length, 
					0.4*initiator.getDouble( AspectRef.bodyRadius ));
			
			Point p = iniBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			Point q = comBody.getPoints().get(0);
			q.setPosition(Vector.minus(originalPos, shift));
			/* body has more points? */
			for( Point w : comBody.getPoints() )
			{
				if(w != q)
					q.setPosition( Vector.add( originalPos, 
							Vector.times( shift, 1.2 )));
			}
		}
		Link.linLink( initiator, complient );
	}
}

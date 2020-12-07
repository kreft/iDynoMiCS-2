package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.methods.DivisionMethod;
import expression.Expression;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Point;
import surface.link.LinearSpring;
import surface.link.Link;
import surface.link.Spring;
import surface.link.TorsionSpring;
import utility.ExtraMath;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class FillialDivision extends DivisionMethod
{

	private boolean randomization = true;
	private double shiftFrac = 0.05;
	/**
	 * \brief Shift the bodies of <b>mother</b> to <b>daughter</b> in space, so
	 * that they do not overlap and assign filial links.
	 * 
	 * Note: class was designed for coccoid cells, rod-like cells will require
	 * some modifications
	 * 
	 * @param initiator An agent.
	 * @param compliant Another agent, whose body overlaps a lot with that of
	 * <b>mother</b>.
	 */
	protected void shiftBodies(Agent initiator, Agent compliant)
	{
		double rs = initiator.getDouble( AspectRef.bodyRadius ) * shiftFrac;
		Shape shape = initiator.getCompartment().getShape();
		Body iniBody = (Body) initiator.get( AspectRef.agentBody );
		Body comBody = (Body) compliant.get( AspectRef.agentBody );
		boolean unlink = ExtraMath.getUniRandDbl() < 
				(double) initiator.getOr( AspectRef.unlinkProbabillity, 0.0 );
		
		/* links for compliant will be constructed later */
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
			
			if( randomization )
				Vector.addEquals( shift, Vector.times( 
						Vector.randomPlusMinus( direction ) , rs ));
			
			Point p = iniBody.getClosePoint( othBody.getCenter(shape), shape);
			p.setPosition(Vector.minus( oriPos, Vector.times( shift, rs )));
			
			Point q = comBody.getClosePoint( iniBody.getCenter(shape), shape);
			q.setPosition(Vector.add( oriPos, Vector.times( shift, rs )));
			
			/* body has more points? */
			for( Point w : comBody.getPoints() )
			{
				if(w != q)
					q.setPosition( Vector.add( oriPos, 
							Vector.times( shift, rs )));
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
				Link.linear( (Agent) other, compliant );
			
			/* update torsion links */
			for( Link l : iniBody.getLinks() )
				if( l.getMembers().size() > 2 )
				{
					int i;
					i = l.getMembers().indexOf( other );
					l.putMember( i, compliant );
					l.setPoint( i, comBody.getClosePoint(
							iniBody.getCenter( shape ), shape ));
				}
			
			for( Link l : othBody.getLinks() )
				if(l.getMembers().size() > 2)
				{
					if( !unlink )
					{
						int i;
						i = l.getMembers().indexOf( initiator );
						l.putMember( i, compliant );
						l.setPoint(i, comBody.getClosePoint(
								othBody.getCenter( shape ), shape ));
					}
					else
						othBody.unLink(l);
				}
			
			if( !unlink )
				Link.torsion((Agent) other, compliant, initiator);
		}
		else
		{
			double[] originalPos = iniBody.getPosition(0);
			double[] shift = Vector.randomPlusMinus( originalPos.length, 
					0.4*initiator.getDouble( AspectRef.bodyRadius ));
			
			if( randomization )
				Vector.addEquals( shift, Vector.times( 
						Vector.randomPlusMinus( originalPos ) , rs ));
			
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
		Link.linear( initiator, compliant );
	}
}

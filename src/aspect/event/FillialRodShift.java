package aspect.event;

import agent.Agent;
import agent.Body;
import agent.Body.Morphology;
import aspect.AspectInterface;
import aspect.Event;
import aspect.methods.DivisionMethod;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.Shape;
import surface.Point;
import surface.link.Link;
import utility.ExtraMath;
import utility.Helper;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class FillialRodShift extends DivisionMethod
{
	private double shiftFrac = 0.05;
	private boolean randomization = true;
	
	public void start(AspectInterface initiator,
			AspectInterface compliant, Double timeStep)
	{
		if ( ! this.shouldChange( initiator ))
			return;
		
		if ( initiator.isAspect( AspectRef.agentBody ) && ((Body) 
				initiator.getValue( AspectRef.agentBody )).getMorphology()
				!= Morphology.BACILLUS )
		{
			shiftMorphology( (Agent) initiator );
			updateAgents( (Agent) initiator, null );
		}

		super.start(initiator, compliant, timeStep);
	}
	
	protected boolean shouldChange( AspectInterface initiator )
	{

		Object iniMass = initiator.getValue( AspectRef.agentMass );
		double variable = Helper.totalMass( iniMass );
		double threshold = Double.MAX_VALUE;
		if ( initiator.isAspect( AspectRef.shiftMass ))
			threshold = initiator.getDouble( AspectRef.shiftMass );
		return (variable > threshold);
	}
	
	protected boolean shouldDevide( AspectInterface initiator )
	{

		Object iniMass = initiator.getValue( AspectRef.agentMass );
		double variable = Helper.totalMass( iniMass );
		double threshold = Double.MAX_VALUE;
		if ( initiator.isAspect( AspectRef.divisionMass ))
			threshold = initiator.getDouble( AspectRef.divisionMass );
		return ( variable > threshold );
	}
	/**
	 * \brief change the initiator body from coccoid to rod-like and update 
	 * filial links
	 * 
	 * @param initiator An agent.
	 * <b>mother</b>.
	 */
	protected void shiftMorphology( Agent initiator )
	{
		double rs = initiator.getDouble( AspectRef.bodyRadius ) * shiftFrac;
		Shape shape = initiator.getCompartment().getShape();
		Body iniBody = (Body) initiator.get( AspectRef.agentBody );
		Body otABody = null;
		Body otBBody = null;
		Point q = null, p = null;
		double[] originalPos, shiftA, shiftB;
		AspectInterface otherA = null;
		AspectInterface otherB = null;
		Link linkA = null;
		Link linkB = null;
		
		if(  initiator.getBoolean( AspectRef.directionalDivision ) &! 
				iniBody.getLinks().isEmpty())
		{
			double[] directionA = null;
			double[] directionB = null;
			
			for( Link l : iniBody.getLinks() )
				if(l.getMembers().size() < 3)
					if( linkA == null )
						linkA = l;
					else
						linkB = l;

			for( AspectInterface a : linkA.getMembers())
				if( a != initiator && otherA == null )
				{
					otherA = a;
					directionA = ((Body) a.getValue(AspectRef.agentBody)).
							getClosePoint( iniBody.getCenter( shape ), shape ).
							getPosition();
					
					otABody = (Body) otherA.getValue( AspectRef.agentBody );
				}
				
			if( linkB != null )
				for( AspectInterface a : linkB.getMembers())
					if( a != initiator && otherB == null )
					{
						otherB = a;
						directionB = ((Body) a.getValue( AspectRef.agentBody )).
								getClosePoint(iniBody.getCenter(shape), shape ).
								getPosition();
	
						otBBody = (Body) otherB.getValue(
								AspectRef.agentBody );
					}
			
			
			originalPos = iniBody.getClosePoint( otABody.getCenter( shape ), 
					shape ).getPosition();
			shiftA = initiator.getCompartment().getShape().
					getMinDifferenceVector(	directionA, originalPos );
			
			if( randomization )
				Vector.addEquals( shiftA, Vector.times( 
						Vector.randomPlusMinus( directionA ) , rs ));

			if( linkB != null )
				shiftB = initiator.getCompartment().getShape().
						getMinDifferenceVector(	directionB, originalPos);
			else
				shiftB = Vector.times(shiftA, -1.0);
			
			if( randomization )
				Vector.addEquals( shiftB, Vector.times( 
						Vector.randomPlusMinus( directionA ) , rs ));
			
			p = iniBody.getClosePoint(otABody.getCenter(shape), shape);
			q = new Point(p);

			p.setPosition(Vector.add( originalPos, Vector.times( shiftA, rs )));
			q.setPosition(Vector.add( originalPos, Vector.times( shiftB, rs )));
			
			/* reshape */
			iniBody.getPoints().add( new Point( q ));
			iniBody.getSurfaces().clear();
			iniBody.assignMorphology( Morphology.BACILLUS.name() );
			iniBody.constructBody( 1.0, 
					initiator.getDouble( AspectRef.transientRadius ));
			
			for(Link l : iniBody.getLinks() )
			{
				if(l.getMembers().size() > 2)
				{
					iniBody.getLinks().remove(l);
				}
			}

			if( otherA != null)
			{
				for(Link l : otABody.getLinks() )
				{
					int i;
					i = l.getMembers().indexOf(initiator);
					l.setPoint(i, iniBody.getClosePoint( otABody.getCenter(
							shape ), shape ));
				}
			}
			
			if ( otherB != null )
			{
				for(Link l : otBBody.getLinks() )
				{
					int i;
					i = l.getMembers().indexOf(initiator);
					l.setPoint(i, iniBody.getClosePoint( otBBody.getCenter(
							shape ), shape ));
				}
			}
		}
		else
		{
		/* if we are not linked yet */
			originalPos = iniBody.getPosition(0);
			shiftA = Vector.randomPlusMinus(originalPos.length, 
					initiator.getDouble( AspectRef.bodyRadius ));
			
			if( randomization )
				Vector.addEquals( shiftA, Vector.times( 
						Vector.randomPlusMinus( originalPos ) , rs ));
			
			p = iniBody.getPoints().get(0);
			q = new Point(p);
			
			p.setPosition( Vector.add( originalPos, Vector.times( shiftA, rs)));
			q.setPosition(Vector.minus(originalPos, Vector.times( shiftA, rs)));
			
			/* reshape */
			iniBody.getPoints().add( new Point( q ));
			iniBody.getSurfaces().clear();
			iniBody.assignMorphology( Morphology.BACILLUS.name() );
			iniBody.constructBody( 0.1,
					initiator.getDouble( AspectRef.transientRadius ));
		}
		
		if( otherA != null)
			Link.torsion( (Agent) otherA, initiator, initiator );
		if( otherB != null)
			Link.torsion( (Agent) otherB, initiator, initiator );
	}
	
	public void shiftBodies(Agent initiator, Agent compliant)
	{
		Shape shape = initiator.getCompartment().getShape();
		Body iniBody = (Body) initiator.get( AspectRef.agentBody );
		Body comBody = (Body) compliant.get( AspectRef.agentBody );
		Point p = null, q = null;
		boolean unlink = ExtraMath.getUniRandDbl() < 
				(double) initiator.getOr( AspectRef.unlinkProbabillity, 0.0 );
		
		comBody.clearLinks();
		
		if(  initiator.getBoolean( AspectRef.directionalDivision ) &! 
				iniBody.getLinks().isEmpty())
		{
			AspectInterface otherA = null;
			AspectInterface otherB = null;
			Body otBBody = null;
			Body otABody = null;

			
			/* this is where it goes wrong */
			for( Link l : iniBody.getLinks() )
			{
				if( l.getMembers().size() < 3 )
				for( AspectInterface a : l.getMembers())
					if( a != initiator)
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
				otABody = (Body) otherA.getValue( AspectRef.agentBody );
				p = iniBody.getClosePoint( otABody.getCenter( shape ), shape );
				q = comBody.getPoints().get(i);
				if( Vector.equals( p.getPosition(), q.getPosition() ))
				{
					i++;
					q = comBody.getPoints().get(i);
				}
				iniBody.getPoints().remove(i);
				comBody.getPoints().remove(1-i);
			}
			else
			{
				if(iniBody.getPoints().size() > 1 )
				{
					iniBody.getPoints().remove(1);
					comBody.getPoints().remove(0);
				}
			}

			if( otherB != null )
			{
				otBBody = (Body) otherB.getValue( AspectRef.agentBody );
				for( Link l : iniBody.getLinks() )
				{
					if( l.getMembers().size() < 3 && 
							l.getMembers().contains( initiator )	&& 
							l.getMembers().contains( otherB ))
					{
						iniBody.unLink(l);
						otBBody.unLink(l);
						continue;
					}
				}
			}
			
			iniBody.clearLinks();
			
			if( otherB != null)
			{
				for( Link l : otBBody.getLinks() )
				{
					if( l.getMembers().size() < 3 && 
							l.getMembers().contains( initiator ))
					{
						/* mother already unlinked */
						otBBody.unLink(l);
						continue;
					}
				}
				Link.linear( (Agent) otherB, compliant );
				Link.torsion( (Agent) otherB, compliant, initiator );
			}
			
			if( otherA != null )
			{
				for( Link l : otABody.getLinks() )
				{
					if( l.getMembers().size() < 3 && 
							l.getMembers().contains( initiator ))
					{
						/* daughter already unlinked */
						otABody.unLink(l);
						continue;
					}
				}
				if( !unlink )
				{
					Link.linear( (Agent) otherA, initiator );
					Link.torsion( (Agent) otherA, initiator, compliant );
				}
			}

			
			if( otherA != null )
			{
				for( Link l : otABody.getLinks() )
					if(l.getMembers().size() > 2)
					{
						if( !unlink )
						{
							int i = 0;
							if( l.getMembers().get(i) != initiator || 
									l.getMembers().get(i) == otherA )
								i = 2;
							if( l.getMembers().get(i) != otherA )
							{
								l.setPoint(1, otABody.getClosePoint(
										iniBody.getCenter(shape), shape), true);
								l.putMember( i, initiator );
								l.setPoint( i, iniBody.getPoints().get(0) );
							}
						}
						else
							otABody.unLink(l);
					}
			}
			
			if( otherB != null )
			{
				for(Link l : otBBody.getLinks())
					if(l.getMembers().size() > 2)
					{
						int i = 0;
						if( l.getMembers().get(i) != initiator )
							i = 2;
						if( l.getMembers().get(i) != otherB )
						{
							l.setPoint(1, otBBody.getClosePoint( 
									comBody.getCenter( shape ), shape ), true);
							l.putMember( i, compliant );
							l.setPoint( i, comBody.getPoints().get(0) );
						}
					}
			}
		}
		else
		{
			if(iniBody.getPoints().size() > 1 )
			{
				iniBody.getPoints().remove(1);
				comBody.getPoints().remove(0);
			}
		}

		if(iniBody.getPoints().size() > 1 )
		{
			iniBody.getPoints().remove(1);
			comBody.getPoints().remove(0);
		}

			/* reshape */
			iniBody.getSurfaces().clear();
			iniBody.assignMorphology( Morphology.COCCOID.name() );
			iniBody.constructBody( 0.0, 
					initiator.getDouble( AspectRef.bodyRadius ));

			comBody.getSurfaces().clear();
			comBody.assignMorphology( Morphology.COCCOID.name() );
			comBody.constructBody(0.0, 
					compliant.getDouble( AspectRef.bodyRadius ));

			Link.linear( (Agent) initiator, compliant );
	}
}

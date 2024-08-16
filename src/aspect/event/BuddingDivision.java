package aspect.event;

import agent.Agent;
import agent.Body;
import analysis.FilterLogic;
import aspect.AspectInterface;
import aspect.methods.DivisionMethod;
import idynomics.Idynomics;
import instantiable.object.InstantiableMap;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import surface.Point;
import surface.link.Link;
import utility.ExtraMath;
import utility.Helper;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx
 */
public class BuddingDivision extends DivisionMethod
{
	private double shiftFrac = 0.01;
	private boolean randomization = true;

	public void start(AspectInterface initiator,
					  AspectInterface compliant, Double timeStep)
	{
		if ( ! this.shouldChange( initiator )) {
			super.start(initiator, compliant, timeStep);
			return;
		}

		if ( initiator.isAspect( AspectRef.agentBody ) && ((Body)
				initiator.getValue( AspectRef.agentBody )).getMorphology()
				!= Body.Morphology.BACILLUS )
		{
			shiftMorphology( (Agent) initiator );
			updateAgents( (Agent) initiator, null );
		}

		super.start(initiator, compliant, timeStep);
	}

	protected boolean shouldChange( AspectInterface initiator )
	{
		boolean shift = false;
		if( initiator.isAspect("shiftCondition"))
			shift = FilterLogic.filterFromString( initiator.getString("shiftCondition") ).match( initiator );
		return shift;
//		return false;
	}

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

			p.setPosition( shape.applyBoundaries(
					Vector.add( originalPos, Vector.times( shiftA, rs ))));
			q.setPosition( shape.applyBoundaries(
					Vector.add( originalPos, Vector.times( shiftB, rs ))));

			/* reshape */
			iniBody.getPoints().add( new Point( q ));
			iniBody.getSurfaces().clear();
			iniBody.assignMorphology( Body.Morphology.BACILLUS.name() );
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

			p.setPosition( shape.applyBoundaries(
					Vector.add( originalPos, Vector.times( shiftA, rs))) );
			q.setPosition( shape.applyBoundaries(
					Vector.minus( originalPos, Vector.times( shiftA, rs))) );

			/* reshape */
			iniBody.getPoints().add( new Point( q ));
			iniBody.getSurfaces().clear();
			iniBody.assignMorphology( Body.Morphology.BACILLUS.name() );
			iniBody.constructBody( 0.1,
					initiator.getDouble( AspectRef.transientRadius ));
		}

		if( otherA != null)
			Link.torsion( (Agent) otherA, initiator, initiator );
		if( otherB != null)
			Link.torsion( (Agent) otherB, initiator, initiator );
	}


	/**
	 * \brief Shift the bodies of <b>initiator</b> to <b>compliant</b> in space, so
	 * that they do not overlap and assign filial links.
	 * 
	 * Note: class was designed for coccoid cells, rod-like cells will require
	 * some modifications
	 * 
	 * @param initiator Original agent.
	 * @param compliant Bud agent
	 * <b>initiator</b>.
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

		/* FIXME probably too rigorous */
		if( unlink && initiator.isAspect(AspectRef.partners) ) {
			InstantiableMap<Integer,String> initiatorMap =
					(InstantiableMap<Integer, String>) initiator.get(AspectRef.partners);
			Agent parent = null;
			for( Integer i : initiatorMap.keySet() )
				if( initiatorMap.get(i).equals("parent") )
					parent = Idynomics.simulator.findAgent( i );
			if( parent != null ) {
				Body parBody = (Body) parent.getValue(AspectRef.agentBody);
				parBody.clearLinks();
				iniBody.clearLinks();
				parent.delete(AspectRef.partners);
				initiator.delete(AspectRef.partners);
			}
		}

		/* register bud and parent in partner map, create partner map if none */
		if( !initiator.isAspect(AspectRef.partners) )
		{
			initiator.set( AspectRef.partners,
					new InstantiableMap<Integer,String>( Integer.class, String.class, XmlRef.identity,
					XmlRef.valueAttribute, XmlRef.map, XmlRef.item, false) );
		}
		@SuppressWarnings("unchecked")
		InstantiableMap<Integer,String> initiatorMap =
				(InstantiableMap<Integer, String>) initiator.get(AspectRef.partners);
		initiatorMap.put(compliant.identity(),"bud");
		InstantiableMap<Integer,String> compliantMap =
				new InstantiableMap<Integer,String>(Integer.class, String.class, XmlRef.identity,
				XmlRef.valueAttribute, XmlRef.map, XmlRef.item, false);
		compliant.set( AspectRef.partners, compliantMap );
		compliantMap.put(initiator.identity(),"parent");
		initiator.set("parent",1.0);

		/* If there is no link choose a random direction, if there is an additional branch it should not align,
		if it is the first branch (continuing) it should be straight.  */
		if( iniBody.getLinks().size() != 1 || unlink ) {

			randomPlacement(rs, initiator, iniBody, comBody);
		} else if(  initiator.getBoolean( AspectRef.directionalDivision ) && initiatorMap.containsValue("parent") &!
				iniBody.getLinks().isEmpty())
		{

			/* find parent link to determine alignment */
			Integer parent = -1;
			for( Integer i : initiatorMap.keySet())
				if ( initiatorMap.get(i).equals("parent")) {
					parent = i;
					break;
				}
			double[] direction = null;
			Agent p = Idynomics.simulator.findAgent( parent );
					direction = ((Body) p.getValue( AspectRef.agentBody )).
							getClosePoint( iniBody.getCenter(shape), shape).getPosition();
					double[] diff = Vector.minus( iniBody.getCenter(shape), direction );
					direction = Vector.add(iniBody.getCenter(shape), diff );

			Body othBody = (Body) p.getValue( AspectRef.agentBody );
			
			double[] oriPos = iniBody.getClosePoint(
					othBody.getCenter( shape ), shape ).getPosition();
			double[] shift = initiator.getCompartment().getShape().
					getMinDifferenceVector(	direction, oriPos );

//			Point p = iniBody.getClosePoint( othBody.getCenter(shape), shape);
//			p.setPosition(Vector.minus( oriPos, Vector.times( shift, rs )));
			
			Point q = comBody.getClosePoint( iniBody.getCenter(shape), shape);
			q.setPosition(Vector.add( oriPos, Vector.times( shift, rs )));
			
			/* body has more points? */
			for( Point w : comBody.getPoints() )
			{
				if(w != q)
					q.setPosition( Vector.add( oriPos, 
							Vector.times( shift, rs )));
			}
			Link.torsion(p, initiator, compliant);

		}
		else
		{
			randomPlacement(rs, initiator, iniBody, comBody);
		}

		/* reshape */
		iniBody.getSurfaces().clear();
		iniBody.assignMorphology( Body.Morphology.COCCOID.name() );
		iniBody.constructBody( 0.0,
				initiator.getDouble( AspectRef.bodyRadius ));

		comBody.getSurfaces().clear();
		comBody.assignMorphology( Body.Morphology.COCCOID.name() );
		comBody.constructBody(0.0,
				compliant.getDouble( AspectRef.bodyRadius ));

		Link.linear( initiator, compliant );
	}

	/**
	 * \brief: place a bud in a random direction on the cell wall.
	 *
	 * @param rs
	 * @param initiator
	 * @param iniBody
	 * @param comBody
	 */
	private void randomPlacement(double rs, Agent initiator, Body iniBody, Body comBody)
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
}
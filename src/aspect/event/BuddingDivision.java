package aspect.event;

import agent.Agent;
import agent.Body;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx
 */
public class BuddingDivision extends DivisionMethod
{
	private double shiftFrac = 0.01;
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
			Link link = iniBody.getLinks().get(0);
			for( Link l : iniBody.getLinks() )
				if(l.getMembers().contains(Idynomics.simulator.findAgent( parent ))) // select parent link FIXME: update link objects to safe parent id for speedup
					link = l;
			AspectInterface other = null;

			/* identify tip direction */
			for( AspectInterface a : link.getMembers())
				if( a != initiator )
				{
					other = a;
					direction = ((Body) a.getValue( AspectRef.agentBody )).
							getClosePoint( iniBody.getCenter(shape), shape).getPosition();
					double[] diff = Vector.minus( iniBody.getCenter(shape), direction );
					direction = Vector.add(iniBody.getCenter(shape), Vector.flip(diff) );
				}
			Body othBody = (Body) other.getValue( AspectRef.agentBody );
			
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
			Link.torsion((Agent) other, initiator, compliant);

		}
		else
		{
			randomPlacement(rs, initiator, iniBody, comBody);
		}
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
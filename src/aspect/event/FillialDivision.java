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
							getPoints().get(0).getPosition();
					continue;
				}
			
			Body otherBody = (Body) other.getValue(AspectRef.agentBody);
			
			double[] originalPos = momBody.getPosition(0);
			double[] shift = Vector.times(
					mother.getCompartment().getShape().getMinDifferenceVector(
							direction, originalPos), 0.5);
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.minus(originalPos, shift));
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.add(originalPos, shift));
			
			for( Link l : momBody.getLinks() )
			{
				if(l.getMembers().size() < 3 && l.getMembers().contains(mother)
						&& l.getMembers().contains(other))
				{
				momBody.unLink(link);
				otherBody.unLink(link);
				continue;
				}
			}
			
			link((Agent) other, daughter);
			
			/* update torsion links */
			for( Link l : momBody.getLinks() )
				if(l.getMembers().size() > 2)
				{
					int i;
					i = l.getMembers().indexOf(other);
					l.addMember(i, daughter);
					l.update(i, daughterBody.getPoints().get(0));
				}
			
			for( Link l : ((Body) other.getValue(AspectRef.agentBody)).getLinks())
				if(l.getMembers().size() > 2)
				{
					int i;
					i = l.getMembers().indexOf(mother);
					l.addMember(i, daughter);
					l.update(i, daughterBody.getPoints().get(0));
				}
			
			torLink((Agent) other, daughter, mother);
		}
		else
		{
			double[] originalPos = momBody.getPosition(0);
			double[] shift = Vector.randomPlusMinus(originalPos.length, 
					0.5*mother.getDouble(AspectRef.bodyRadius));
			
			Point p = momBody.getPoints().get(0);
			p.setPosition(Vector.add(originalPos, shift));
			Point q = daughterBody.getPoints().get(0);
			q.setPosition(Vector.minus(originalPos, shift));
		}
		link(mother, daughter);
	}
	
	protected void torLink(Agent a, Agent b, Agent c)
	{
		Body aBody = (Body) a.get(AspectRef.agentBody);
		Body bBody = (Body) b.get(AspectRef.agentBody);
		Body cBody = (Body) c.get(AspectRef.agentBody);
		Double linkerStifness = (double) b.getOr( 
				AspectRef.linkerStifness, 100000.0);
		/* FIXME placeholder default function */
		Expression springFun = (Expression) b.getOr( 
				AspectRef.filialLinker, new Expression( 
						"stiffness * dif * dif * 1000" ));

		Point[] points = new Point[] { aBody.getPoints().get(0), 
				bBody.getPoints().get(0), cBody.getPoints().get(0) };
		
		Link link = new Link();
		Spring spring = new TorsionSpring(linkerStifness, points, springFun,
				3.14159265359);
		link.addSpring(spring);
		link.addMember(0, a);
		link.addMember(1, b);
		link.addMember(2, c);
		bBody.addLink(link);
	}
	
	protected void link(Agent mother, Agent daughter)
	{
		Body momBody = (Body) mother.get(AspectRef.agentBody);
		Body daughterBody = (Body) daughter.get(AspectRef.agentBody);
		Double linkerStifness = (double) mother.getOr( 
				AspectRef.linkerStifness, 100000.0);
		/* FIXME placeholder default function */
		Expression springFun = (Expression) mother.getOr( 
				AspectRef.filialLinker, new Expression( 
						"stiffness * dh * dh * 100.0 )" ));

		Point[] points = new Point[] { momBody.getPoints().get(0), 
				daughterBody.getPoints().get(0) };
		
		Link link = new Link();
		Spring spring = new LinearSpring(linkerStifness, points, springFun,
				mother.getDouble(AspectRef.bodyRadius) + 
				daughter.getDouble(AspectRef.bodyRadius));
		link.addSpring(spring);
		link.addMember(0, mother);
		link.addMember(1, daughter);
		momBody.addLink(link);
		daughterBody.addLink(link);
	}
}

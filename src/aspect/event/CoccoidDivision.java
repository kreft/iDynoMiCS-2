package aspect.event;

import agent.Agent;
import agent.Body;
import aspect.methods.DivisionMethod;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import surface.Point;

/**
 * Simple coccoid division class, divides mother cell in two with a random
 * moves mother and daughter in a random opposing direction and registers the
 * daughter cell to the compartment
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class CoccoidDivision extends DivisionMethod
{
	/**
	 * \brief Shift the bodies of <b>parent</b> to <b>child</b> in space, so
	 * that they do not overlap.
	 * 
	 * @param parent An agent.
	 * @param child Another agent, whose body overlaps a lot with that of
	 * <b>parent</b>.
	 */
	protected void shiftBodies(Agent parent, Agent child)
	{
		Body parentBody = (Body) parent.get(AspectRef.agentBody);
		Body childBody = (Body) child.get(AspectRef.agentBody);

		double[] originalPos = parentBody.getPosition(0);
		double[] shift = Vector.randomPlusMinus(originalPos.length, 
				0.5*parent.getDouble(AspectRef.bodyRadius));

		Point p = parentBody.getPoints().get(0);
		p.setPosition(Vector.add(originalPos, shift));
		Point q = childBody.getPoints().get(0);
		q.setPosition(Vector.minus(originalPos, shift));
	}
}

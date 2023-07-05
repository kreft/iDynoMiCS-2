package analysis.specs;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import compartment.Compartment;
import grid.ArrayType;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;

/**
 * 
 * @author Bastiaan
 *
 */
public class EnvironmentSpecification extends Specification {

	private String _sol;
	private Compartment _compartment;
	
	public EnvironmentSpecification(String expression, Compartment compartment )
	{
		this._compartment = compartment;
		this._sol = expression.split("%")[1];
	}
	
	public Object value(AspectInterface subject)
	{
		if ( this._sol.equals( XmlRef.volume ) )
			return String.valueOf( 
					this._compartment.getShape().getTotalVolume() );
		if (subject == null || 
				this._compartment.isDimensionless() |! 
				subject.isAspect(AspectRef.agentBody) )
		{
			return this._compartment.getSolute( 
					this._sol ).getAverage( ArrayType.CONCN );
		}
		else {
			Shape shape = ((Agent) subject).getCompartment().getShape();
			return this._compartment.getSolute( 
				this._sol ).getValueAt(ArrayType.CONCN , ((Body) 
						subject.getValue(AspectRef.agentBody)).getCenter(shape));
		}
	}

	public String header()
	{
		return "%" + _sol;
	}
}

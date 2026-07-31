package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentDouble;

public class Maximum extends ComponentDouble {

	public Maximum(Component a, Component b) {
        super(a, b);
        this._expr = " MAX ";
	}

	@Override
	protected double calculateValue(Map<String, Double> variables) {
		return Math.max(this._a.getValue(variables),
				this._b.getValue(variables));
	}

	@Override
	public Component differentiate(String withRespectTo) {
		return null;
	}

	@Override
	public Object evaluate(AspectInterface subject) {
		return Math.max( (double) this._a.evaluate(subject),
				(double) this._b.evaluate(subject));
		
		
	}

}

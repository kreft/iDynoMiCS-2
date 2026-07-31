package expression.arithmetic;

import java.util.Map;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentDouble;

public class Minimum extends ComponentDouble {

	public Minimum(Component a, Component b) {
        super(a, b);
        this._expr = " MIN ";
	}

	@Override
	protected double calculateValue(Map<String, Double> variables) {
		return Math.min(this._a.getValue(variables),
				this._b.getValue(variables));
	}

	@Override
	public Component differentiate(String withRespectTo) {
		return null;
	}

	@Override
	public Object evaluate(AspectInterface subject) {
		return Math.min( (double) this._a.evaluate(subject),
				(double) this._b.evaluate(subject));
		
		
	}

}

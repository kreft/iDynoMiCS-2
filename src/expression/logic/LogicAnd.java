package expression.logic;

import java.util.Map;

import expression.Component;
import expression.ComponentBoolean;

public class LogicAnd extends ComponentBoolean {

	public LogicAnd(Component a, Component b) {
		super(a, b);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double calculateValue(Map<String, Double> variables) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Component differentiate(String withRespectTo) {
		// TODO Auto-generated method stub
		return null;
	}

}

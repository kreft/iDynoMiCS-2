package expression.arithmetic;

import aspect.AspectInterface;
import expression.Component;
import expression.ComponentMultiple;
import expression.ConstantVector;

import java.util.ArrayList;
import java.util.Map;

public class Trapezoid extends ComponentMultiple {

    /**
     * /brief: The trapezoid, placed with the long edge on the x-axis and a height of 1.0, is used to calculate the
     * height of the trapezoid at location x.
     *
     * One use case for the trapezoid is to scale activity based on a input variable x, for example pH.
     *
     * The first input (component a) is the queried x value, the following inputs (ConstantVector b) are the
     * x-position of the 4 corners of the trapezoid from lowest x-position to highest.
     */
	public Trapezoid(Component a, ConstantVector b) {
        super();
        ArrayList<Component> comps = new ArrayList<Component>();
            comps.add(a);
            comps.addAll(b.getAllComponents());
            this._components = comps;
            this._expr = " TRAPEZOID ";
        }

    public Trapezoid(ArrayList<Component> comps) {
        super(comps);
    }

    @Override
        public String getName()
        {
            String out = this._components.get(0).getName() + this._expr + "{";
            for( Component c : this._components.subList(1,this._components.size()))
                out += c.getName() + " ";
            out += "}";
            return out;
        }

        @Override
        public String reportEvaluation(Map<String, Double> variables)
        {
            String out = this._components.get(0).reportEvaluation(variables) + this._expr + "{";
            for( Component c : this._components.subList(1,this._components.size()))
                out += c.reportEvaluation(variables) + " ";
            out += "}";
            return out;
        }

    /**
     * /brief: The trapezoid, placed with the long edge on the x-axis and a height of 1.0, returns the height of
     * the trapezoid at location x.
     */
    @Override
        protected double calculateValue(Map<String, Double> variables)
        {
            double x = this._components.get(0).getValue(variables);
            double a = this._components.get(1).getValue(variables);
            double b = this._components.get(2).getValue(variables);
            double c = this._components.get(3).getValue(variables);
            double d = this._components.get(4).getValue(variables);

            if (x <= a || x >= d) {
                return 0.0;
            } else if (x >= b && x <= c) {
                return 1.0;
            } else if (x > a && x < b) {
                return (x - a) / (b - a);
            } else {
                return (d - x) / (d - c);
            }
        }

    @Override
    public Component differentiate(String withRespectTo) {
        return null;
    }

    /**
     * /brief: The trapezoid, placed with the long edge on the x-axis and a height of 1.0, returns the height of
     * the trapezoid at location x.
     */
    @Override
        public Object evaluate(AspectInterface subject)
        {
            double x = (double) this._components.get(0).evaluate(subject);
            double a = (double) this._components.get(1).evaluate(subject);
            double b = (double) this._components.get(2).evaluate(subject);
            double c = (double) this._components.get(3).evaluate(subject);
            double d = (double) this._components.get(4).evaluate(subject);

            if (x <= a || x >= d) {
                return 0.0;
            } else if (x >= b && x <= c) {
                return 1.0;
            } else if (x > a && x < b) {
                return (x - a) / (b - a);
            } else {
                return (d - x) / (d - c);
            }
        }
    }
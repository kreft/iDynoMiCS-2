/**
 *
 */
package expression;

import aspect.AspectInterface;
import expression.arithmetic.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ConstantVector extends Elemental
{

    protected String _expr;

    protected ArrayList<Component> _components;

    public ConstantVector(double[] components)
    {
        super(Type.numeric);
        this._components = new ArrayList<Component>();
        for (double d : components)
            this.appendComponent(new Constant(String.valueOf(d), d));
    }

    public ConstantVector(ArrayList<Component> components)
    {
        super(Type.numeric);
        this._components = components;
    }

    @Override
    public String getName()
    {
        String out = this._components.get(0).getName();
        for ( int i = 1; i < this._components.size(); i++ )
            out += this._expr+this._components.get(i).getName();
        return ( "(") + out + ")";
    }

    /**
     *
     * @param variables
     * @return length of vector __NOT CONTENT__
     */
    public double getValue(Map<String, Double> variables)
    {
        return Double.valueOf(this.calculateValue(variables).length);
    }

    @Override
    public Object evaluate(AspectInterface subject) {
        return this;
    }

    public String reportEvaluation(Map<String, Double> variables)
    {
        String out = this._components.get(0).reportEvaluation(variables);
        for ( int i = 1; i < this._components.size(); i++ )
            out += this._expr+this._components.get(i).reportEvaluation(variables);
        return  out;
    }

    protected double[] calculateValue(Map<String, Double> variables) {
        return new double[0];
    }

    public void prependComponent(Component component)
    {
        this._components.add(0, component);
    }

    public void appendComponent(Component component)
    {
        this._components.add(component);
    }

    public ArrayList<Component> getAllComponents()
    {
        return this._components;
    }

    @Override
    public void appendVariablesNames(Collection<String> names)
    {
        for ( Component c : this._components )
            if( c != null )
                c.appendVariablesNames(names);
    }
}

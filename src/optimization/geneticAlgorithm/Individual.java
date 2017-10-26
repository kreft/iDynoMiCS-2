package optimization.geneticAlgorithm;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import optimization.TestModel;
import optimization.objectiveFunction.ObjectiveFunction;
import utility.Helper;

public class Individual {

	public double[] _inputs;
	public double[] _outputs;
	public TestModel _model;

    /**
     * Constructor including parameter values
     * @param input parameter values (ordered).
     */
    public Individual( double[] inputs )
    {
    	this._inputs = inputs;
    	this._model = new TestModel(inputs[0], inputs[1]);
    }

    public void evaluate( double[] x )
    {
    	this._model = new TestModel(_inputs[0], _inputs[1]);
    	this._outputs = new double[x.length];
    	for(int i = 0; i < x.length; i++)
    		this._outputs[i] = this._model.getY( x[i] );
    }

    /* Public methods */
    public int size() {
        return _inputs.length;
    }
    
    public double getGene(int i)
    {
    	return this._inputs[i];
    }
    
    public void setGene(int i, double d)
    {
    	this._inputs[i] = d;
    }

    
    public double getLoss( ObjectiveFunction lossFunction ) {
    	if ( Helper.isNullOrEmpty( this._outputs ) )
    	{
    		Log.out(Tier.NORMAL, "Warning: cannot callculate loss bacause " +
    				"output values are not defined.");
    		return Double.MAX_VALUE;
    	}
    	else
    		return lossFunction.loss( _outputs );
    }

    @Override
    public String toString() {
        return "inputs: " + Vector.toString( _inputs ) + "\noutputs: " + 
        		Vector.toString( _outputs ) + "\n";
    }
}
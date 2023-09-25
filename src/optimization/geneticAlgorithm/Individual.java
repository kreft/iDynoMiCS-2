package optimization.geneticAlgorithm;

import java.util.Collection;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import optimization.TestModel;
import optimization.constraint.Bound;
import optimization.constraint.Constraint;
import optimization.objectiveFunction.ObjectiveFunction;
import utility.ExtraMath;
import utility.Helper;

/**
 * This gentetic algorithm implementation is based on the online tutorial by:
 * Lee Jacobson, 2012
 * 
 * http://www.theprojectspot.com/tutorial-post/creating-a-genetic-algorithm-for-beginners/3
 */
public class Individual {

	private double[] _inputs;
	private double[] _outputs;
	
	@Deprecated
	public TestModel _model;

    /**
     * Constructor including parameter values and evaluation outputs.
     * @param outputs 
     * @param input parameter values (ordered).
     */
    public Individual( double[] inputs, double[] outputs )
    {
    	if( ExtraMath.random == null )
    		ExtraMath.initialiseRandomNumberGenerator();
    	
    	this._inputs = inputs;
    	this._outputs = outputs;
    }
    
    /**
     * Constructor including parameter values
     * @param inputs
     */
    public Individual( double[] inputs )
    {
    	this._inputs = inputs;
    }
    
    public Individual( Individual individual )
    {
    	this._inputs = Vector.copy( individual._inputs);
    	this._outputs = Vector.copy( individual._outputs);
    }

    /**
     * Run the model and store model output
     * @param x
     */
    public void evaluate( double[] x )
    {
    	this._model = new TestModel(_inputs[0], _inputs[1]);
    	this._outputs = new double[x.length];
    	for(int i = 0; i < x.length; i++)
    		this._outputs[i] = this._model.getY( x[i] );
    }
    
    /**
     * Apply scaled mutations
     * @param probability
     * @param scale
     * @param _constraints 
     */
    public void mutate(double probability, double scale, 
    		Collection<Constraint> constraints) 
    {
    	Object lBounds = constraints.toArray()[0];
    	Object uBounds = constraints.toArray()[1];
    	double[] parBoundsLower = null;
    	double[] parBoundsUpper = null;
    	if (lBounds instanceof Bound && uBounds instanceof Bound) {
    		parBoundsLower = ((Bound)lBounds).bound();
    		parBoundsUpper = ((Bound)uBounds).bound();
    	}
        for (int i = 0; i < this.size(); i++) 
        {
            if (Math.random() <= probability) 
            {
                /* Create random gene */
            	double[] temp = Vector.copy(_inputs);
            	double parSpaceScalar = 1.0;
            	if (parBoundsUpper != null && parBoundsLower != null)
            		parSpaceScalar = parBoundsUpper[i]-parBoundsLower[i];
            	temp[i] = this.get(i) + ExtraMath.getNormRand() * scale * parSpaceScalar;
            	while ( ! Constraint.allMet(constraints, temp) )
            		temp[i] = this.get(i) + ExtraMath.getNormRand() * scale * parSpaceScalar;
                this.set(i, temp[i]);
            }
        }
    }

    /**
     * 
     * @param indiv
     * @param probability
     * @return
     */
    public Individual crossover(Individual indiv, double probability) {
        Individual out = new Individual( Vector.copy( this._inputs ) );
        for (int i = 0; i < this.size(); i++) {
            if (Math.random() > probability) 
                out.set(i, indiv.get(i));
        }
        return out;
    }

    /**
     * Length of input parameters 'genome'
     * @return
     */
    public int size() 
    {
        return _inputs.length;
    }
    
    /**
     * get input parameter i 'gene'
     * @param i
     * @return
     */
    public double get(int i)
    {
    	return this._inputs[i];
    }
    
    public double[] get()
    {
    	return this._inputs;
    }
    
    /**
     * set d as input parameter i 'gene'
     * @param i
     * @param d
     */
    public void set(int i, double d)
    {
    	this._inputs[i] = d;
    }

    /**
     * Use an
     * @param lossFunction
     * @return
     */
    public double loss( ObjectiveFunction lossFunction ) 
    {
    	if ( Helper.isNullOrEmpty( this._outputs ) )
    	{
    		Log.out(Tier.NORMAL, "Warning: cannot callculate loss bacause " +
    				"output values are not defined.");
    		return Double.MAX_VALUE;
    	}
    	else
    		return lossFunction.loss( _outputs );
    }

    /**
     * string representation
     */
    @Override
    public String toString() 
    {
        return "inputs: " + Vector.toString( this._inputs ) + "\noutputs: " + 
        		Vector.toString( this._outputs );
    }
}

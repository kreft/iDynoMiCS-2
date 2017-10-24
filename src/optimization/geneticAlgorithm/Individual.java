package optimization.geneticAlgorithm;

import dataIO.Log;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import optimization.objectiveFunction.ObjectiveFunction;
import utility.Helper;

public class Individual {

	protected double[] _inputs;
	protected double[] _outputs;
	
    static int defaultGeneLength = 64;
    private byte[] genes = new byte[defaultGeneLength];
    // Cache
    private int fitness = 0;
    
    /**
     * Constructor including parameter values
     * @param input parameter values (ordered).
     */
    public Individual( double[] inputs )
    {
    	
    }
    
    /**
     * Legacy constructor
     */
    public Individual( )
    {
    	
    }

    // Create a random individual
    public void generateIndividual() {
        for (int i = 0; i < size(); i++) {
            byte gene = (byte) Math.round(Math.random());
            genes[i] = gene;
        }
    }

    /* Getters and setters */
    // Use this if you want to create individuals with different gene lengths
    public static void setDefaultGeneLength(int length) {
        defaultGeneLength = length;
    }
    
    public byte getGene(int index) {
        return genes[index];
    }

    public void setGene(int index, byte value) {
        genes[index] = value;
        fitness = 0;
    }

    /* Public methods */
    public int size() {
        return genes.length;
    }

    public int getFitness() {
        if (fitness == 0) {
            fitness = FitnessCalc.getFitness(this);
        }
        return fitness;
    }
    
    // Mutate an individual
    public void mutate(double mutationRate) {
        // Loop through genes
        for (int i = 0; i < this.size(); i++) {
            if (Math.random() <= mutationRate) {
                // Create random gene
                byte gene = (byte) Math.round(Math.random());
                this.setGene(i, gene);
            }
        }
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
        return Vector.toString( _inputs ) + "\n outputs: " + 
        		Vector.toString( _outputs ) + "\n";
    }
}
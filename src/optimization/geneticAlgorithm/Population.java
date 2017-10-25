package optimization.geneticAlgorithm;

import linearAlgebra.Vector;
import optimization.objectiveFunction.ObjectiveFunction;
import optimization.sampling.LatinHyperCubeSampling;

public class Population {

    Individual[] individuals;
    ObjectiveFunction _of;
    
    public Population( int slices, double[] lowerBound, 
    		double[] upperBound, double[] x )
    {
    	/* initialize population parameters */
    	int populationSize = slices;
    	individuals = new Individual[populationSize];
    	
    	/* Latin hyper cube sampling for good coverage of parameter space */
    	double[][] lhc = LatinHyperCubeSampling.sample( slices, 
    			lowerBound.length );
    	
    	/* generate individuals */
        for (int i = 0; i < this.size(); i++) {
            Individual newIndividual = new Individual( Vector.times(lhc[i], upperBound), x );
            saveIndividual(i, newIndividual);
        }
        
    }

    public Population(ObjectiveFunction of, int size) {
		this._of = of;
		individuals = new Individual[size];
	}
    
    public void setObjectiveFunction( ObjectiveFunction of )
    {
    	this._of = of;
    }

	/* Getters */
    public Individual getIndividual(int index) {
        return individuals[index];
    }

    public Individual getFittest() {
        Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.getLoss( _of ) > getIndividual(i).getLoss( _of ) ) {
                fittest = getIndividual(i);
            }
        }
        return fittest;
    }

    /* Public methods */
    // Get population size
    public int size() {
        return individuals.length;
    }

    // Save individual
    public void saveIndividual(int index, Individual indiv) {
        individuals[index] = indiv;
    }
}
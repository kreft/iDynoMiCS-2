package optimization.geneticAlgorithm;

import optimization.sampling.LatinHyperCubeSampling;

public class Population {

    Individual[] individuals;

    /*
     * Constructors
     */
    // Create a population
    public Population(int populationSize, boolean initialise) {
        individuals = new Individual[populationSize];
        // Initialise population
        if (initialise) {
            // Loop and create individuals
            for (int i = 0; i < size(); i++) {
                Individual newIndividual = new Individual();
                newIndividual.generateIndividual();
                saveIndividual(i, newIndividual);
            }
        }
    }
    
    public Population( int slices, double[] lowerBound, 
    		double[] upperBound )
    {
    	/* initialize population parameters */
    	int populationSize = slices*lowerBound.length;
    	individuals = new Individual[populationSize];
    	
    	/* Latin hyper cube sampling for good coverage of parameter space */
    	double[][] lhc = LatinHyperCubeSampling.sample( slices, 
    			lowerBound.length );
    	
    	/* generate individuals */
        for (int i = 0; i < this.size(); i++) {
            Individual newIndividual = new Individual( lhc[i] );
            saveIndividual(i, newIndividual);
        }
        
    }

    /* Getters */
    public Individual getIndividual(int index) {
        return individuals[index];
    }

    public Individual getFittest() {
        Individual fittest = individuals[0];
        // Loop through individuals to find fittest
        for (int i = 0; i < size(); i++) {
            if (fittest.getFitness() <= getIndividual(i).getFitness()) {
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
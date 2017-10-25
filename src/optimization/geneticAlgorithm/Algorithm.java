package optimization.geneticAlgorithm;

import linearAlgebra.Vector;
import optimization.objectiveFunction.ObjectiveFunction;
import utility.ExtraMath;

public class Algorithm {

    /* GA parameters */
    private static final double uniformRate = 0.0;
    private static final double mutationRate = 0.5;
    private static final double mutationScale = 1.0;
    private static final int tournamentSize = 5;
    private static final boolean elitism = true;

    /* Public methods */
    
    // Evolve a population
    public static Population evolvePopulation(Population pop, ObjectiveFunction of, double[] x) {
    	
        Population newPopulation = new Population( of , pop.size());

        // Keep our best individual
        if (elitism) {
            newPopulation.saveIndividual(0, pop.getFittest());
        }

        // Crossover population
        int elitismOffset;
        if (elitism) {
            elitismOffset = 1;
        } else {
            elitismOffset = 0;
        }
        // Loop over the population size and create new individuals with
        // crossover
        for (int i = elitismOffset; i < pop.size(); i++) {
            Individual indiv1 = tournamentSelection(pop, of);
            Individual indiv2 = tournamentSelection(pop, of);
            Individual newIndiv = crossover(indiv1, indiv2, x);
            newPopulation.saveIndividual(i, newIndiv);
        }

        // Mutate population
        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }
        
        return newPopulation;
    }

    // Crossover individuals
    private static Individual crossover(Individual indiv1, Individual indiv2, double[] x) {
        Individual newSol = new Individual( new double[] { indiv1.getGene(0), indiv1.getGene(1) } , x);
        // Loop through genes
        for (int i = 0; i < indiv1.size(); i++) {
            // Crossover
            if (Math.random() <= uniformRate) {
                // already set
            } else {
                newSol.setGene(i, indiv2.getGene(i));
            }
        }
        return newSol;
    }

    // Mutate an individual
    private static void mutate(Individual indiv) {
        // Loop through genes
        for (int i = 0; i < indiv.size(); i++) {
            if (Math.random() <= mutationRate) {
                // Create random gene
            	double gene = Double.MAX_VALUE;
            	while (gene > 10.0 || gene < 0.0 )
	                gene = indiv.getGene(i) + ExtraMath.getNormRand() * 
	                		mutationScale;
                indiv.setGene(i, gene);
            }
        }
    }

    // Select individuals for crossover
    private static Individual tournamentSelection(Population pop, ObjectiveFunction of) {
        // Create a tournament population
        Population tournament = new Population( of, tournamentSize );
        // For each place in the tournament get a random individual
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(i, pop.getIndividual(randomId));
        }
        // Get the fittest
        Individual fittest = tournament.getFittest();
        return fittest;
    }
}
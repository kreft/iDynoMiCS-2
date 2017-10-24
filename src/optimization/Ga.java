package optimization;

import optimization.geneticAlgorithm.*;
import utility.ExtraMath;

public class Ga {

    public static void main(String[] args)     	
        {

    	ExtraMath.initialiseRandomNumberGenerator();
    	Population pop =  new Population( 5, new double[] {0.0, 1.0, 2.0, 0.0}, 
    			new double[] {10.0, 11.0, 11.0, 10.0} );
    	
        // Set a candidate solution
        FitnessCalc.setSolution("1111000000000000000000000000000000000000000000000000000000001111");

        // Create an initial population
        Population myPop = new Population(50, true);
        
        // Evolve our population until we reach an optimum solution
        int generationCount = 0;
        while (myPop.getFittest().getFitness() < FitnessCalc.getMaxFitness()) {
            generationCount++;
            System.out.println("Generation: " + generationCount + " Fittest: " + myPop.getFittest().getFitness());
            myPop = Algorithm.evolvePopulation(myPop);
        }
        System.out.println("Solution found!");
        System.out.println("Generation: " + generationCount);
        System.out.println("Genes:");
        System.out.println(myPop.getFittest());

    }
}
package optimization;

import optimization.geneticAlgorithm.Population;
import optimization.objectiveFunction.ObjectiveFunction;
import optimization.objectiveFunction.QuadraticLossFunction;
import sensitivityAnalysis.XmlCreate;

public class GeneticAlgorithm {

	public static ObjectiveFunction getOp( double[] dataVector )
	{
		ObjectiveFunction qlf = new QuadraticLossFunction();
    	qlf.setData( dataVector );
    	return qlf;
	}
	
	public static void step(ObjectiveFunction op, double fitnessThreshold, 
			Population pop, int generationCount , int maxIter)
	{
        System.out.println("Generation: " + generationCount + " Fittest: " + 
        		pop.fittest().loss( op ) + " " + pop.fittest() );
        generationCount++;
        
    	if ( pop.fittest().loss( op ) > fitnessThreshold && 
    			maxIter > generationCount ) {
    		pop = pop.evolvePopulation();
    		
    		double[][] inMatrix = pop.getInMatrix();
    		XmlCreate.writeOutputs( pop.size(), inMatrix );
        }
    	else
    	{
    		double fitness = pop.fittest().loss( op );
    		if ( generationCount == maxIter )
    		{
    			System.out.println("Reached maximum number of iterations: "+ 
    					maxIter + " fitness: " + fitness);
    		}
    		else
    		{
    			System.out.println("Reached fitness requirement at iteration: "+ 
    					generationCount + " fitness: " + fitness);
    		}
    		
    		//further reporting
    	}
	}
}

package optimization;

import dataIO.Log;
import dataIO.Log.Tier;
import optimization.geneticAlgorithm.Population;
import optimization.objectiveFunction.ObjectiveFunction;
import optimization.objectiveFunction.QuadraticLossFunction;
import sensitivityAnalysis.ProtocolCreater;

public class GeneticAlgorithm {

	public static ObjectiveFunction getOp( double[] dataVector )
	{
		ObjectiveFunction of = new QuadraticLossFunction();
    	of.setData( dataVector );
    	return of;
	}
	
	public static void step(ObjectiveFunction op, double fitnessThreshold, 
			Population pop, int generationCount , int maxIter, ProtocolCreater xmlc)
	{
		// Getting ProtocolCreater from the calling class so that _master is defined 
		// ProtocolCreater xmlc = new ProtocolCreater();
		Log.out(Tier.NORMAL, "Generation: " + generationCount + " Population: " + 
				pop.size() + " \nFittest: " + pop.fittest().loss( op )  + " " 
				+ pop.fittest() );
        
    	if ( pop.fittest().loss( op ) > fitnessThreshold && 
    			maxIter > generationCount ) {
    		pop = pop.evolvePopulation();
    		
    		double[][] inMatrix = pop.getInMatrix();
    		xmlc.writeOutputs( pop.size(), inMatrix, generationCount );
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
    	generationCount++;
	}
}

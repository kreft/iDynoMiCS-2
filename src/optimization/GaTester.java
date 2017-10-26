package optimization;

import linearAlgebra.Vector;
import optimization.geneticAlgorithm.Algorithm;
import optimization.geneticAlgorithm.Population;
import optimization.objectiveFunction.ObjectiveFunction;
import optimization.objectiveFunction.QuadraticLossFunction;
import utility.ExtraMath;

public class GaTester {

	public static void main(String[] args) {
    	ObjectiveFunction qlf = new QuadraticLossFunction();
    	double fitnessThreshold = 0.05;
    	
    	ExtraMath.initialiseRandomNumberGenerator();
    	
    	TestModel mod = new TestModel();
    	double[] tp = new double[] { 1.0, 2.0, 3.0, 4.0 };
    	double[] measured = new double[] { 
    			mod.getMeasurment(1.0, 0.0), 
    			mod.getMeasurment(2.0, 0.0), 
    			mod.getMeasurment(3.0, 0.0), 
    			mod.getMeasurment(4.0, 0.0) };
 
    	qlf.setData( measured );
    	
    	Population pop =  new Population( 100, new double[] {0.0, 0.0}, 
    			new double[] { 10.0, 10.0 }, tp );
    	
    	pop.setObjectiveFunction( qlf );
        
        // Evolve our population until we reach an optimum solution
        int generationCount = 0;
        
        pop = solve(qlf,  fitnessThreshold,  pop,  generationCount , tp);
        
        System.out.println(" measured: " + Vector.toString(measured));
        System.out.println(pop.getFittest());
        System.out.println( pop.getFittest().getLoss(qlf) );
	}
	
	public static Population solve(ObjectiveFunction op, double fitnessThreshold, 
			Population pop, int generationCount , double[] x)
	{
        generationCount++;
        System.out.println("Generation: " + generationCount + " Fittest: " + 
        		pop.getFittest().getLoss( op ) + " " + 
        		pop.getFittest().getGene(0) + " " +
        		pop.getFittest().getGene(1));
        pop = Algorithm.evolvePopulation(pop, op, x);
        
    	if ( pop.getFittest().getLoss( op ) > fitnessThreshold ) {
    		return solve(op, fitnessThreshold, pop, generationCount, x);
        }
    	else
    	{
    		System.out.println( pop.getFittest().getLoss( op ) );
    		return pop;
    	}
	}

}

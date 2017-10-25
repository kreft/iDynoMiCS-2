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
    	double fitnessThreshold = 0.01;
    	
    	ExtraMath.initialiseRandomNumberGenerator();
    	
    	TestModel mod = new TestModel();
    	
    	double[] measured = new double[] { 
    			mod.getMeasurment(1.0, 0.0), 
    			mod.getMeasurment(2.0, 0.0), 
    			mod.getMeasurment(3.0, 0.0), 
    			mod.getMeasurment(4.0, 0.0) };
 
    	qlf.setData( measured );
    	
    	Population pop =  new Population( 100, new double[] {0.0, 0.0}, 
    			new double[] { 10.0, 10.0 }, new double[] {1.0, 2.0, 3.0, 4.0} );
    	
    	pop.setObjectiveFunction( qlf );
        
        // Evolve our population until we reach an optimum solution
        int generationCount = 0;
        
        while ( pop.getFittest().getLoss( qlf ) > fitnessThreshold ) {
            generationCount++;
            System.out.println("Generation: " + generationCount + " Fittest: " + 
            		pop.getFittest().getLoss( qlf ) + " " + 
            		pop.getFittest());
            pop = Algorithm.evolvePopulation(pop, qlf, new double[] {1.0, 2.0, 3.0, 4.0});
        }
        System.out.println(" measured: " + Vector.toString(measured));
        System.out.println(pop.getFittest());
        System.out.println( pop.getFittest().getLoss(qlf) );
	}

}

package optimization;

import java.util.Collection;
import java.util.LinkedList;

import linearAlgebra.Vector;
import optimization.constraint.Bound;
import optimization.constraint.Constraint;
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
    			mod.getMeasurment(1.0, 0.01), 
    			mod.getMeasurment(2.0, 0.01), 
    			mod.getMeasurment(3.0, 0.01), 
    			mod.getMeasurment(4.0, 0.01) };
 
    	qlf.setData( measured );
    	
    	Collection<Constraint> constraints = new LinkedList<Constraint>();
    	
    	constraints.add( new Bound(new double[] { -10.0, -5.0 }, false) );
    	constraints.add( new Bound(new double[] { 10.0, 10.0 }, true) );
    	
    	Population pop =  new Population( qlf, 100, constraints, tp );

        
        // Evolve our population until we reach an optimum solution
        int generationCount = 0;
        
        pop = solve(qlf,  fitnessThreshold,  pop,  generationCount , tp);
        System.out.println( pop.fittest() );
        System.out.println("measured: " + Vector.toString(measured));
	}
	
	public static Population solve(ObjectiveFunction op, double fitnessThreshold, 
			Population pop, int generationCount , double[] x)
	{
		int maxIter = 20;
        System.out.println("Generation: " + generationCount + " Fittest: " + 
        		pop.fittest().loss( op ) + " " + 
        		pop.fittest().get(0) + " " +
        		pop.fittest().get(1));
        pop = pop.evolvePopulation( x );
        generationCount++;
        
    	if ( pop.fittest().loss( op ) > fitnessThreshold && 
    			maxIter > generationCount ) {
    		return solve(op, fitnessThreshold, pop, generationCount, x);
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
    		
    		return pop;
    	}
	}

}

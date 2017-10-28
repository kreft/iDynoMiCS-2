package idynomics.launchable;

import java.util.Collection;

import optimization.GeneticAlgorithm;
import optimization.constraint.Constraint;
import optimization.geneticAlgorithm.Population;
import optimization.objectiveFunction.ObjectiveFunction;

public class GeneticAlgorithmLaunch implements Launchable {

	@Override
	public void initialize(String[] args) {

		String rootFolder;
		String dataFile;
		String protocolfile;
		int generation = 0;
		double fitnessThreshold = 0;
		int maxIter = 0;
		
		if ( args == null || args.length == 1 || args[1] == null )
		{
			System.out.print("No generation speciefied! \n");
		}
		else
			generation = Integer.valueOf( args[2] );
		if ( args == null || args.length == 2 || args[2] == null )
		{
			System.out.print("No generation path speciefied! \n");
		}
		else
			rootFolder = args[2];
		if ( args == null || args.length == 3 || args[3] == null )
		{
			System.out.print("No data file profided! \n");
		}
		else
			dataFile = args[3];
		if ( args == null || args.length == 4 || args[4] == null )
		{
			System.out.print("No master protocolfile profided! \n");
		}
		else
			protocolfile = args[4];
		if ( args == null || args.length == 5 || args[5] == null )
		{
			System.out.print("No fitness threshold speciefied! \n");
		}
		else
			fitnessThreshold = Double.valueOf( args[5] );
		if ( args == null || args.length == 6 || args[6] == null )
		{
			System.out.print("No maximum number of iterations speciefied! \n");
		}
		else
			maxIter = Integer.valueOf( args[6] );
		/*
		 *  TODO error function etc, GA parameters
		 */
		
		double[][] inMatrix = null; // csvReader( rootFolder.. generation / input matrix.csv )
		
		double[] dataVector = null; // csvReader( dataFile );
		
		double[][] outMatrix = null; // csvReader( rootFolder.. / dataFile / iterate over subs, read in datapoints corresponding to data file )
		
		
		
		Collection<Constraint> constraints = null; // XMLcreate( protocolfile )
		
		ObjectiveFunction op = GeneticAlgorithm.getOp( dataVector );
		Population pop = new Population( op, inMatrix, outMatrix, constraints);
		GeneticAlgorithm.step(op, fitnessThreshold, pop, generation, maxIter);
	}

}

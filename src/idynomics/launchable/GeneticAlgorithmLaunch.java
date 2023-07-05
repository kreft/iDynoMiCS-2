package idynomics.launchable;

import java.util.Collection;
import java.util.LinkedList;

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import optimization.GeneticAlgorithm;
import optimization.constraint.Bound;
import optimization.constraint.Constraint;
import optimization.geneticAlgorithm.DataFromCSV;
import optimization.geneticAlgorithm.Population;
import optimization.objectiveFunction.ObjectiveFunction;
import sensitivityAnalysis.ProtocolCreater;

public class GeneticAlgorithmLaunch implements Launchable {

	@Override
	public void initialize(String[] args) {

		String rootFolder = null; // Idynomics.global.outputRoot;
		String dataFile = null; // rootFolder+"/"+Idynomics.global.subFolderStruct+"obsData.csv";
		String protocolfile = null; // Idynomics.global.protocolFile;
		int generation = 0;
		double fitnessThreshold = 0;
		int maxIter = 0;

		if ( args == null || args.length == 1 || args[1] == null )
		{
			System.out.print("No generation speciefied! \n");
		}
		else
			generation = Integer.valueOf( args[1] );
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
			System.out.print("No master protocolfile provided! \n");
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
		Idynomics.setupGlobals( protocolfile );
		ProtocolCreater xmlc = new ProtocolCreater( protocolfile, false );
		
		if (generation == 0)
		{
			/* We can try to create a generation 0 to nudge the user in the
			 * right direction, but this will probably mean the run has to be
			 * restart.  */
			if (Log.shouldWrite(Tier.NORMAL))
				Log.out(Tier.NORMAL, "Warning GA cannot evolve population"
						+ "without generation 0, trying to generate population"
						+ "(Not recomended).");
			SamplerLaunch sl = new SamplerLaunch();
			String[] sl_args = new String[] { "-s", protocolfile };
			sl.initialize(sl_args);
		}
		else
			/* We cannot create a new generation if the previous one has not
			 * been evaluated! */
		{
			
			Collection<Constraint> constraints = new LinkedList<Constraint>();
			
			DataFromCSV csvIn = new DataFromCSV();
			double[] dataVector = csvIn.getData(dataFile);
			int prev = generation-1;
			
			Log.out(Tier.NORMAL, "pervious: " + prev + " current: " + generation);
			
			double[][] outMatrix = DataFromCSV.getOutput( 
					Idynomics.global.outputRoot + "/" + Idynomics.global.subFolderStruct +
					"/result/gen_"+ prev +"/");
			
			double[][] inMatrix = DataFromCSV.getInput( 
					Idynomics.global.outputRoot + "/" + Idynomics.global.subFolderStruct  +
					"/input/gen_"+ prev +"/xVal.csv" );
			
			constraints.add( new Bound( xmlc.getBounds()[0], false) );
	    	constraints.add( new Bound( xmlc.getBounds()[1], true) );
	    	
	    	System.out.println(Vector.toString( xmlc.getBounds()[0]));
	    	System.out.println(Vector.toString( xmlc.getBounds()[1]));
			
			ObjectiveFunction op = GeneticAlgorithm.getOp( dataVector );
			Population pop = new Population( op, inMatrix, outMatrix, constraints);
						
			GeneticAlgorithm.step(op, fitnessThreshold, pop, generation, maxIter, xmlc);
		}
	}

}

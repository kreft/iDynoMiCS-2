package testJUnit;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.junit.Test;

import linearAlgebra.Vector;
import spatialRegistry.RTree;
import spatialRegistry.RTree.SeedPicker;
import spatialRegistry.splitTree.FindSplit;
import spatialRegistry.splitTree.SplitTree;
import spatialRegistry.splitTree.SplitTree.Area;
import spatialRegistry.splitTree.SplitTree.Entry;
import utility.ExtraMath;

public class treeTest {

	static ForkJoinPool pool = new ForkJoinPool(8);
	
	@Test
	public void largeCollection()
	{
		treeTest(100000);
		rTreeTest(100000);
	}
	
	public void treeTest(int sampleSize)
	{
		
		SplitTree tree = new SplitTree(3, 3, 50, null, 8);
		
		double tic = System.currentTimeMillis();
		
		ExtraMath.initialiseRandomNumberGenerator();
		for ( int i = 0; i < sampleSize; i++)
		{
			double[] a = Vector.randomPlusMinus(3, 10000.0);
			tree.add( Vector.add(a, -1.0), Vector.add(a, 1.0),
					Double.valueOf(i) );
		}

		tic = (System.currentTimeMillis() - tic);
		System.out.println("splitTree generated and added random entries: "+sampleSize + " in: "+ tic);
		tic = (System.currentTimeMillis());
//		List<Entry> list = pool.invoke(tree.node);
//
//		tic = (System.currentTimeMillis() - tic);
//		System.out.println("return concurrent " + list.size() + " in: "+ tic);

		System.out.println("returned unfiltered " +
				tree.node.allUnfiltered(new LinkedList<Area>()).size() + 
				" in: "+ (System.currentTimeMillis() - tic));
		
//		tic = (System.currentTimeMillis());
//		System.out.println("return filtered " + tree.allObjects().size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
		
		tic = (System.currentTimeMillis());
		double[] f = Vector.randomPlusMinus(3, 10000.0);
		List<Entry> list = tree.find(tree.new Entry(Vector.add(f, -0.1), Vector.add(f, 0.1),
				null));
		System.out.println("find single " + list.size() + 
				" in: "+ (System.currentTimeMillis() - tic));
		
		LinkedList<Area> objectives = new LinkedList<Area>();
		for (int i = 0; i < 1000; i++)
		{
			double[] a = Vector.randomPlusMinus(3, 10000.0);
			objectives.add( tree.new Entry(Vector.add(a, -1.0), Vector.add(a, 1.0),
					Double.valueOf(i) ) );
		}
		
		tic = (System.currentTimeMillis());
		List<List<Entry>> listr = new FindSplit(tree, objectives).invoke();
		System.out.println("multi find " + listr.size() + 
				" in: "+ (System.currentTimeMillis() - tic));
	}
	
	public void rTreeTest(int sampleSize)
	{
		RTree<Double> tree = new RTree<Double>(50, 3, 3, SeedPicker.LINEAR);
		
		double tic = System.currentTimeMillis();
		
		ExtraMath.initialiseRandomNumberGenerator();
		for ( int i = 0; i < sampleSize; i++)
		{
			double[] b = new double[3];
			tree.insert(Vector.randomPlusMinus(3, 10000.0), Vector.setAll(b, 2), Double.valueOf(i));

		}

		tic = (System.currentTimeMillis() - tic);
		System.out.println("rTree generated and added random entries: "+sampleSize + " in: "+ tic);
		tic = (System.currentTimeMillis());
		List<Double> list = tree.all();

		tic = (System.currentTimeMillis() - tic);
		System.out.println("return " + list.size() + " in: "+ tic);
		System.out.println(tree.all().size());
		
		tic = (System.currentTimeMillis());
		List<List<Double>> listr = new LinkedList<List<Double>>();
		for (int i = 0; i < 1000; i++)
		{
			double[] b = new double[3];
			listr.add(tree.search(Vector.randomPlusMinus(3, 10000.0), Vector.setAll(b, 0.2)));
		}
		System.out.println("multi find " + listr.size() + 
				" in: "+ (System.currentTimeMillis() - tic));

	}

}

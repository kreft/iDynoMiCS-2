package test.junit.oldTests;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import linearAlgebra.Vector;
import spatialRegistry.RTree;
import spatialRegistry.RTree.SeedPicker;
import utility.ExtraMath;

/**
 * Tests some properties of different tree structures
 * 
 * one of the potential benefits of an octree type structure is that if we are
 * iterating through all agents and their neighbourhoods, we can simply cycle 
 * trough the leaf nodes of the octree and do not have to do a search from root
 * 
 * TODO clean-up
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class TreeTest {
	
	@Test
	public void largeCollection()
	{
//		System.out.println("Random distributed");
//		System.out.println("------------------------------------");
//		treeTest(50000);
//
//		System.out.println("------------------------------------");
//		rTreeTest(50000);
//
//
//		System.out.println("------------------------------------");
//		System.out.println("Large unpopulated area");
//		System.out.println("------------------------------------");
//		treeTestLargeUnpop(50000);
//
//		System.out.println("------------------------------------");
//		rTreeTestLargeUnpop(50000);
	}
		
	public void treeTest(int sampleSize)
	{
//		
//		SplitTree tree = new SplitTree(3, 3, 100, new double[]{ -100.0, -100.0, -100.0 }, new double[]{ 100.0, 100.0, 100.0 }, null);
//		
//		double tic = System.currentTimeMillis();
//		List<Entry> ae = new LinkedList<Entry>();
//		ExtraMath.initialiseRandomNumberGenerator();
//		for ( int i = 0; i < sampleSize; i++)
//		{
////			double[] a = Vector.randomPlusMinus(3, 100.0);
////			Entry e = new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
////					Double.valueOf(i));
////			tree.add( e );
////			ae.add(e);
//		}
//
//		tic = (System.currentTimeMillis() - tic);
//		System.out.println("splitTree generated and added random entries: "+sampleSize + " in: "+ tic);
//		tic = (System.currentTimeMillis());
////		List<Entry> list = pool.invoke(tree.node);
////
////		tic = (System.currentTimeMillis() - tic);
////		System.out.println("return concurrent " + list.size() + " in: "+ tic);
//
//		System.out.println("returned unfiltered " +
//				tree.node.allUnfiltered(new LinkedList<Entry>()).size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		tic = (System.currentTimeMillis());
//		System.out.println("return filtered " + tree.allEntries(new LinkedList<Entry>()).size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		tic = (System.currentTimeMillis());
//		double[] f = Vector.randomPlusMinus(3, 100.0);
//		List<Entry> list = tree.find(new Entry(Vector.add(f, -0.1), Vector.add(f, 0.1),
//				null));
//		System.out.println("find single " + list.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
////		LinkedList<Area> objectives = new LinkedList<Area>();
////		for (int i = 0; i < 1000; i++)
////		{
////			double[] a = Vector.randomPlusMinus(3, 1000.0);
////			objectives.add( tree.new Entry(Vector.add(a, -1.0), Vector.add(a, 1.0),
////					Double.valueOf(i) ) );
////		}
////		
////		tic = (System.currentTimeMillis());
////		List<List<Entry>> listr = new FindSplit(tree, objectives).invoke();
////		System.out.println("multi find " + listr.size() + 
////				" in: "+ (System.currentTimeMillis() - tic));
//		
//		List<List<Entry>> listr = new LinkedList<List<Entry>>();
//		
//		tic = (System.currentTimeMillis());
//		for (int i = 0; i < 1000; i++)
//		{
//			double[] a = Vector.randomPlusMinus(3, 100.0);
//			listr.add(tree.find(new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
//					Double.valueOf(i) ) ));
//		}
//		System.out.println("multi find " + listr.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		listr = new LinkedList<List<Entry>>();
//		tic = (System.currentTimeMillis());
//		for (int i = 0; i < 10000; i++)
//		{
//			double[] a = Vector.randomPlusMinus(3, 100.0);
//			listr.add(tree.find(new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
//					Double.valueOf(i) ) ));
//		}
//		System.out.println("multi find " + listr.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		listr = new LinkedList<List<Entry>>();
//		tic = (System.currentTimeMillis());
//		for (int i = 0; i < 1000; i++)
//		{
//			double[] a = Vector.randomPlusMinus(3, 100.0);
//			listr.add(tree.node.findUnfiltered(new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
//					Double.valueOf(i) ) ));
//		}
//		System.out.println("multi find unfiltered " + listr.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		listr = new LinkedList<List<Entry>>();
//		tic = (System.currentTimeMillis());
//		for (int i = 0; i < 10000; i++)
//		{
//			double[] a = Vector.randomPlusMinus(3, 100.0);
//			listr.add(tree.node.findUnfiltered(new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
//					Double.valueOf(i) ) ));
//		}
//		System.out.println("multi find unfiltered " + listr.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		tic = (System.currentTimeMillis());
//
//		List<Node> nodes = tree.node.allLeaves(new LinkedList<Node>());
//
//		System.out.println("all neighbourhoods " + nodes.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		tic = (System.currentTimeMillis());
//		int i = 0;
//		int b = 0;
//		for ( Node n : nodes )
//		{
//			if (n.size() == 0)
//				i++;
//			for ( Object e : n.allEntries(new LinkedList<Entry>()))
//			{
//				ae.remove(e);
////				if ( ae.contains(e) )
////				{
////					ae.remove(e);
////					i++;
////				}
////				if ( ae.contains(e) )
////				{
////					b++;
////					System.out.println("duplicate?");
////				}
//			}
//		}
//		System.out.println("assessed all, " + i + //" duplicate " + b +
//				" turned out to be empty, in: "+ (System.currentTimeMillis() - tic));

	}
	
	public void rTreeTest(int sampleSize)
	{
		RTree<Double> tree = new RTree<Double>(8, 3, 3, SeedPicker.LINEAR);
		
		double tic = System.currentTimeMillis();
		
		ExtraMath.initialiseRandomNumberGenerator();
		for ( int i = 0; i < sampleSize; i++)
		{
			double[] b = new double[3];
			tree.insert(Vector.randomPlusMinus(3, 100.0), Vector.setAll(b, 0.2), Double.valueOf(i));

		}

		tic = (System.currentTimeMillis() - tic);
		System.out.println("rTree generated and added random entries: "+sampleSize + " in: "+ tic);
		tic = (System.currentTimeMillis());
		List<Double> list = tree.all();

		tic = (System.currentTimeMillis() - tic);
		System.out.println("return " + list.size() + " in: "+ tic);
		
		tic = (System.currentTimeMillis());
		List<List<Double>> listr = new LinkedList<List<Double>>();
		for (int i = 0; i < 1000; i++)
		{
			double[] a = Vector.randomPlusMinus(3, 100.0);
			double[] b = new double[3];
			listr.add(tree.search(a, Vector.add(a, Vector.setAll(b, 0.2))));
		}
		System.out.println("multi find " + listr.size() + 
				" in: "+ (System.currentTimeMillis() - tic));
		
		tic = (System.currentTimeMillis());
		listr = new LinkedList<List<Double>>();
		for (int i = 0; i < 10000; i++)
		{
			double[] b = new double[3];
			listr.add(tree.localSearch(Vector.randomPlusMinus(3, 100.0), Vector.setAll(b, 0.2)));
		}
		System.out.println("multi find " + listr.size() + 
				" in: "+ (System.currentTimeMillis() - tic));
		
		Map<double[],double[]> boxes = tree.allBoxes();
		tic = (System.currentTimeMillis());
		for ( double[] d : boxes.keySet())
		{
			tree.localSearch(d, boxes.get(d));
		}
		System.out.println("assess all " + boxes.size() + 
				" in: "+ (System.currentTimeMillis() - tic));

	}
	
	public void treeTestLargeUnpop(int sampleSize)
	{
//		
//		SplitTree tree = new SplitTree(3, 3, 100, new double[]{ -100.0, -100.0, -100.0 }, new double[]{ 100.0, 100.0, 100.0 }, null);
//		
//		double tic = System.currentTimeMillis();
//		List<Entry> ae = new LinkedList<Entry>();
//		ExtraMath.initialiseRandomNumberGenerator();
//		for ( int i = 0; i < sampleSize; i++)
//		{
////			double[] a = Vector.randomPlusMinus(3, 20.0);
////			Entry e = new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
////					Double.valueOf(i));
////			tree.add( e );
////			ae.add(e);
//		}
//
//		tic = (System.currentTimeMillis() - tic);
//		System.out.println("splitTree generated and added random entries: "+sampleSize + " in: "+ tic);
//		tic = (System.currentTimeMillis());
////		List<Entry> list = pool.invoke(tree.node);
////
////		tic = (System.currentTimeMillis() - tic);
////		System.out.println("return concurrent " + list.size() + " in: "+ tic);
//
//		System.out.println("returned unfiltered " +
//				tree.node.allUnfiltered(new LinkedList<Entry>()).size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
////		tic = (System.currentTimeMillis());
////		System.out.println("return filtered " + tree.allEntries(new LinkedList<Entry>()).size() + 
////				" in: "+ (System.currentTimeMillis() - tic));
//		
//		tic = (System.currentTimeMillis());
//		double[] f = Vector.randomPlusMinus(3, 100.0);
//		List<Entry> list = tree.find(new Entry(Vector.add(f, -0.1), Vector.add(f, 0.1),
//				null));
//		System.out.println("find single " + list.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
////		LinkedList<Area> objectives = new LinkedList<Area>();
////		for (int i = 0; i < 1000; i++)
////		{
////			double[] a = Vector.randomPlusMinus(3, 1000.0);
////			objectives.add( tree.new Entry(Vector.add(a, -1.0), Vector.add(a, 1.0),
////					Double.valueOf(i) ) );
////		}
////		
////		tic = (System.currentTimeMillis());
////		List<List<Entry>> listr = new FindSplit(tree, objectives).invoke();
////		System.out.println("multi find " + listr.size() + 
////				" in: "+ (System.currentTimeMillis() - tic));
//		
//		List<List<Entry>> listr = new LinkedList<List<Entry>>();
//		
//		tic = (System.currentTimeMillis());
//		for (int i = 0; i < 1000; i++)
//		{
//			double[] a = Vector.randomPlusMinus(3, 100.0);
//			listr.add(tree.find(new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
//					Double.valueOf(i) ) ));
//		}
//		System.out.println("multi find " + listr.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		listr = new LinkedList<List<Entry>>();
//		tic = (System.currentTimeMillis());
//		for (int i = 0; i < 1000; i++)
//		{
//			double[] a = Vector.randomPlusMinus(3, 100.0);
//			listr.add(tree.node.findUnfiltered(new Entry(Vector.add(a, -0.1), Vector.add(a, 0.1),
//					Double.valueOf(i) ) ));
//		}
//		System.out.println("multi find unfiltered " + listr.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		tic = (System.currentTimeMillis());
//
//		List<Node> nodes = tree.node.allLeaves(new LinkedList<Node>());
//
//		System.out.println("all neighbourhoods " + nodes.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		tic = (System.currentTimeMillis());
//		int i = 0;
//		int b = 0;
//		for ( Node n : nodes )
//		{
//			for ( Object e : n.allEntries(new LinkedList<Entry>()))
//			{
//				ae.remove(e);
////				if ( ae.contains(e) )
////				{
////					ae.remove(e);
////					i++;
////				}
////				if ( ae.contains(e) )
////				{
////					b++;
////					System.out.println("duplicate?");
////				}
//			}
//		}
//		System.out.println("assess all " + //i + " duplicate " + b +
//				" in: "+ (System.currentTimeMillis() - tic));
//
//	}
//	
//	public void rTreeTestLargeUnpop(int sampleSize)
//	{
//		RTree<Double> tree = new RTree<Double>(50, 3, 3, SeedPicker.LINEAR);
//		
//		double tic = System.currentTimeMillis();
//		
//		ExtraMath.initialiseRandomNumberGenerator();
//		for ( int i = 0; i < sampleSize; i++)
//		{
//			double[] b = new double[3];
//			tree.insert(Vector.randomPlusMinus(3, 20.0), Vector.setAll(b, 0.2), Double.valueOf(i));
//
//		}
//
//		tic = (System.currentTimeMillis() - tic);
//		System.out.println("rTree generated and added random entries: "+sampleSize + " in: "+ tic);
//		tic = (System.currentTimeMillis());
//		List<Double> list = tree.all();
//
//		tic = (System.currentTimeMillis() - tic);
//		System.out.println("return " + list.size() + " in: "+ tic);
//		
//		tic = (System.currentTimeMillis());
//		List<List<Double>> listr = new LinkedList<List<Double>>();
//		for (int i = 0; i < 1000; i++)
//		{
//			double[] a = Vector.randomPlusMinus(3, 100.0);
//			double[] b = new double[3];
//			listr.add(tree.search(a, Vector.add(a, Vector.setAll(b, 0.2))));
//		}
//		System.out.println("multi find " + listr.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//		
//		Map<double[],double[]> boxes = tree.allBoxes();
//		tic = (System.currentTimeMillis());
//		for ( double[] d : boxes.keySet())
//		{
//			tree.localSearch(d, boxes.get(d));
//		}
//		System.out.println("assess all " + boxes.size() + 
//				" in: "+ (System.currentTimeMillis() - tic));
//
	}


}

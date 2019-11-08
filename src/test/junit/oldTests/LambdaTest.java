package test.junit.oldTests;

/**
 * Lambda expressions are known to be slower than alternatives, try to avoid
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class LambdaTest {
//
//	@Test
//	public void smallCollection()
//	{
//		lambdaExpressions(1000);
//		predicateOperation(1000);
//	}
//	
//	@Test
//	public void mediumCollection()
//	{
//		lambdaExpressions(100000);
//		predicateOperation(100000);
//	}
//	
//	@Test
//	public void largeCollection()
//	{
//		lambdaExpressions(10000000);
//		predicateOperation(10000000);
//	}
//	
//	public void lambdaExpressions(int sampleSize)
//	{
//		LinkedList<Double> numbers = new LinkedList<Double>();
//		for ( int i = 0; i < sampleSize; i++)
//			numbers.add(Double.valueOf(i));
//		
//		double tic = System.currentTimeMillis();
//		numbers.removeIf((a) -> 
//		{ 
//			if ( a/2 != Math.round(a))
//				return true;
//			return false;
//		}
//		);
//		tic = (System.currentTimeMillis() - tic) * 0.001;
//
//		System.out.println("lambdaExpression with: "+sampleSize + " in: "+ tic);
//	}
//	
//	public void predicateOperation(int sampleSize)
//	{
//		SamplePredicate<Double> filter = new SamplePredicate<>();
//		LinkedList<Double> numbers = new LinkedList<Double>();
//		for ( int i = 0; i < sampleSize; i++)
//			numbers.add(Double.valueOf(i));
//
//		double tic = System.currentTimeMillis();
//		numbers.removeIf(filter);
//		tic = (System.currentTimeMillis() - tic) * 0.001;
//		System.out.println("using predicate with: "+sampleSize + " in: "+ tic);
//	}
//	
//	class SamplePredicate<Double> implements Predicate<Double>{  
//
//		@Override
//		public boolean test(Double a) 
//		{
//			if ( ((double) a/2.0) != Math.round((double) a) )
//				 return false;
//			return true;
//		}  
//	}
}

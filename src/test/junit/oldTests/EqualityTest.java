package test.junit.oldTests;

import org.junit.Test;

public class EqualityTest {

	@Test
	public void doubleAndIntegerTest()
	{
		Double a = 1.0;
		Integer i = 1;
		
		System.out.println( String.valueOf( a.equals(i) ) );
		System.out.println( String.valueOf( Double.valueOf( a ).equals( 
				Double.valueOf( String.valueOf( i ) ) ) ) );
	}
}

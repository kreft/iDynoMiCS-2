package testJUnit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dataIO.Log;
import dataIO.Log.Tier;
import expression.UnitConversion;
import expression.UnitConversion.Units;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class UnitUnitTest {

	@Test
	public void unitInterpretation()
	{
		Log.set(Tier.DEBUG);
		UnitConversion conv = new UnitConversion();
		Units myUnit = conv.new Units();

		myUnit.fromString("g+1·m-1");
		System.out.println(myUnit.toString());
		assertTrue("correct unit conversion", myUnit.toString().contains("0.001 kg·m-1") );
		
		Units unitA= conv.new Units();
		unitA.fromString("kg·cm-2");
		System.out.println("kg·cm-2 = " + unitA.toString());
		assertTrue("correct unit conversion", unitA.toString().contains("10000.0") );
		
		Units unitB= conv.new Units();
		unitB.fromString("d");
		System.out.println("d = " + unitB.toString());
		assertTrue("correct unit conversion", unitB.toString().contains("86400") );
	}
}

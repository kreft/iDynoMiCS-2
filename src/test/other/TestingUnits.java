package test.other;

import java.util.Map;

import expression.Expression;
import expression.arithmetic.Unit;
import expression.arithmetic.Unit.SI;
import utility.GenericTrio;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class TestingUnits {

	public static void main(String[] args) {
		test();
	}

	public static void test() {


		Unit myUnit = new Unit();

		myUnit.fromString("g+1·m-1");
		System.out.println( myUnit );
		if ( myUnit.toString().contains("kg·m-1") || myUnit.toString().contains("m-1·kg"))
			System.out.println("correct unit conversion");
		
		System.out.println("\n");
		
		Unit unitA = new Unit();
		unitA.fromString("kg·cm-2");
		System.out.println("kg·cm-2 = " + unitA );
		if ( unitA.toString().contains("10000.0") )
			System.out.println("correct unit conversion");
		
		Unit unitB = new Unit();
		unitB.fromString("d");
		System.out.println("d = " + unitB );
		if ( unitB.toString().contains("86400") )
			System.out.println("correct unit conversion");
		
	
		
		Unit unitAB = Unit.product(unitA, unitB);
		System.out.println("product = " + unitAB );
		
		unitAB = Unit.quotient(unitA, unitB);
		System.out.println("quotient = " + unitAB + "\n");
		
		Expression expressiona = new Expression("35.0 *-2.0 ");
		double a = expressiona.getValue();

		Expression expressionb = new Expression("35.0 *-2.0 [g·dm-3]");
		double b = expressionb.getValue();

		System.out.println("no units no conversion " + a+ 
				" \nUnits, conversion to SI " + b );
		
		Unit unitC = new Unit();
		unitC.fromString("N");
		System.out.println("C (newton) = " + unitC);
		
		String format = "mN";
		System.out.println("C (formatted to mN) = " + unitC.toString(format));
		System.out.println("C (formatted to kg) = " + unitC.toString("kg"));	
		
		/* micro_units */
		Map<SI,GenericTrio<SI, String, Double>> unitSystem =
				Unit.formatMap("pg","µm","min");
		
		Unit testUnit = new Unit("g+1·m-3");
		System.out.println( testUnit + " to: " + testUnit.toString(unitSystem));
		
		double inputValue = 1.0;
		String inputUnit = "g+1·m-3";
		double output = inputValue * new Unit( inputUnit ).format(unitSystem);
		
		System.out.println( myUnit + " to: " + myUnit.toString(unitSystem) );
		System.out.println( unitA + " to: " + unitA.toString(unitSystem) );
		System.out.println( unitC + " to: " + unitC.toString(unitSystem) );
		
		System.out.println( unitC + " to: " + unitC.format(unitSystem) );
		System.out.println( myUnit + " to: " + myUnit.format("pg·µm-1") );
	}


}

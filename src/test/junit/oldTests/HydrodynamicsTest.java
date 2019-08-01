/**
 * 
 */
package test.junit.oldTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import surface.Drag;
import utility.ExtraMath;

/**
 * \brief Test class for methods to do with hydrodynamics.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class HydrodynamicsTest
{
	
	public final static double CELSIUS_TO_KELVIN = 273.15;
	
	/**
	 * Use a more forgiving absolute tolerance for viscosity calculations.
	 * Units of Pa s.
	 */
	public final static double VISCOSITY_TOLERANCE = 1E-4;
	
	/**
	 * \brief Check that the formula predicting the viscosity of water at
	 * various is consistent with values given in standard tables.
	 * 
	 * <p>See Table 5 from <a 
	 * href="http://www.nist.gov/data/PDFfiles/jpcrd243.pdf">this article</a>
	 *  for values used.</p>
	 */
	@Test
	public void checkViscosity()
	{
		double tempC, tempK, mu;
		/* 0°C */
		tempC = 0 ;
		tempK = tempC + CELSIUS_TO_KELVIN;
		mu = Drag.viscosityWater(tempK);
		//System.out.println("T = "+tempC+" (C) = "+tempK+" (K) -> mu = "+mu);
		assertTrue(ExtraMath.areEqual(mu, 0.001792, VISCOSITY_TOLERANCE));
		/* 10°C */
		tempC = 10 ;
		tempK = tempC + CELSIUS_TO_KELVIN;
		mu = Drag.viscosityWater(tempK);
		//System.out.println("T = "+tempC+" (C) = "+tempK+" (K) -> mu = "+mu);
		assertTrue(ExtraMath.areEqual(mu, 0.001308, VISCOSITY_TOLERANCE));
		/* 20°C */
		tempC = 20 ;
		tempK = tempC + CELSIUS_TO_KELVIN;
		mu = Drag.viscosityWater(tempK);
		//System.out.println("T = "+tempC+" (C) = "+tempK+" (K) -> mu = "+mu);
		assertTrue(ExtraMath.areEqual(mu, 0.001002, VISCOSITY_TOLERANCE));
		/* 30°C */
		tempC = 30 ;
		tempK = tempC + CELSIUS_TO_KELVIN;
		mu = Drag.viscosityWater(tempK);
		//System.out.println("T = "+tempC+" (C) = "+tempK+" (K) -> mu = "+mu);
		assertTrue(ExtraMath.areEqual(mu, 0.0007978, VISCOSITY_TOLERANCE));
		/* 40°C */
		tempC = 40 ;
		tempK = tempC + CELSIUS_TO_KELVIN;
		mu = Drag.viscosityWater(tempK);
		//System.out.println("T = "+tempC+" (C) = "+tempK+" (K) -> mu = "+mu);
		assertTrue(ExtraMath.areEqual(mu, 0.000653, VISCOSITY_TOLERANCE));
	}

	
	
}

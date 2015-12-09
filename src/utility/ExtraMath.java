/**
 * \package utils
 * \brief Package of classes that perform utility functions in the process of
 * running an iDynoMiCS Simulation.
 * 
 * Package of classes that perform utility functions in the process of running
 * an iDynoMiCS Simulation. This package is part of iDynoMiCS v2.0, governed by
 * the CeCILL license under French law and abides by the rules of distribution
 * of free software.  You can use, modify and/ or redistribute iDynoMiCS under
 * the terms of the CeCILL license as circulated by CEA, CNRS and INRIA at the
 * following URL "http://www.cecill.info".
 */
package utility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;

import utility.MTRandom;

/**
 * \brief Abstract class with some extra useful math functions.
 * 
 * Contents:
 * 		Simple calculations
 * 		Shapes
 * 		Arrays of doubles
 * 		Dealing with signs
 * 		Dealing with strings
 * 		Random number generation
 * 
 * @author João Xavier (xavierj@mskcc.org), Memorial Sloan-Kettering Cancer
 * Center (NY, USA)
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu)
 * @author Robert Clegg (rjc096@bham.ac.uk), University of Birmingham, UK
 */
public final class ExtraMath
{
	/**
	 * \brief One option for writing decimals to screen.
	 * 
	 * This always has 3 digits before the decimal point, and adjusts the
	 * scientific exponent accordingly.
	 */
	public static java.text.DecimalFormat dfSc = new DecimalFormat("000.###E0");
	
	/**
	 * \brief Second option for writing decimals to screen.
	 * 
	 * This always has 2 digits after the decimal point, and will round any
	 * smaller decimals.
	 */
	public static java.text.DecimalFormat dfUs = new DecimalFormat("########.##");
	
	/**
	 * Random number generator
	 */
	public static Random random;
	
	/* ----------------------- Initialising random ------------------------ */
	
	/**
	 * \brief Initialise the random number generator with a randomly generated
	 * seed.
	 */
	public static void initialiseRandomNumberGenerator()
	{
		long seed = (long) ( Calendar.getInstance().getTimeInMillis() *
															Math.random() );
		intialiseRandomNumberGenerator(seed);
	}
	
	/**
	 * \brief Initialise the random number generator with a given <b>seed</b>.
	 * 
	 * @param seed long integer number to seed the random number generator. 
	 */
	public static void intialiseRandomNumberGenerator(long seed)
	{
		random = new MTRandom(seed);
	}
	
	/**
	 * \brief Initialise the random number generator with an 
	 * ObjectInputStream.
	 * 
	 * @param stream ObjectInputStream to read from.
	 */
	public static void initialiseRandomNumberGenerator(ObjectInputStream stream)
	{
		try
		{
			random = (MTRandom) stream.readObject();
		}
		catch (ClassNotFoundException e)
		{
			// TODO
		}
		catch (IOException e)
		{
			// TODO
		}
	}
	
	/**
	 * \brief Write the state of the random number generator to an 
	 * ObjectOutputStream.
	 * 
	 * @param stream ObjectOutputStream to write to.
	 */
	public static void writeRandomNumberGenerator(ObjectOutputStream stream)
	{
		try
		{
			stream.writeObject(random);
		}
		catch (IOException e)
		{
			// TODO
		}
	}
	
	/* ----------------------- Simple calculations ------------------------ */
	
	public static final boolean areEqual(double x, double y, double absTol)
	{
		return Math.abs(x - y) < absTol;
	}
	
	/**
	 * \brief Computes the logarithm of base 2.
	 * 
	 * <p>If x is non-positive Double.NaN will be returned.</p>
	 * 
	 * @param x The double to take the logarithm of.
	 * @return double value of the logarithm (base 2) of <b>x</b>.
	 */
	public static final double log2(double x)
	{
		return Math.log(x) / Math.log(2.0);
	}
	
	/**
	 * \brief Square an integer number.
	 * 
	 * @param x The int to square.
	 * @return The int square of x.
	 */
	public static final int sq(int x)
	{
		return x*x;
	}
	
	/**
	 * \brief Square a double number.
	 * 
	 * @param x The double to square.
	 * @return The double square of <b>x</b>.
	 */
	public static final double sq(double x)
	{
		return x*x;
	}
	
	/**
	 * \brief Cube an integer number.
	 * 
	 * @param x The int to cube.
	 * @return The int cube of x.
	 */
	public static final int cube(int x)
	{
		return x*x*x;
	}
	
	/**
	 * \brief Cube a double number.
	 * 
	 * @param x The double to cube.
	 * @return The double cube of x.
	 */
	public static final double cube(double x)
	{
		return x*x*x;
	}
	
	/**
	 * \brief Find the real cube root of a double number.
	 * 
	 * @param x The double to take the cube root of.
	 * @return The double real cube root of x.
	 */
	public static final double cubeRoot(double x)
	{
		return Math.pow( x, (1.0/3.0) );
	}
	
	/**
	 * \brief Calculate 2 to the power of x, where x is an integer.
	 * 
	 * <p>Returns 1 if x is less than zero.</p>
	 * 
	 * @param x The exponent
	 * @return 2<sup><b>x</b></sup>
	 */
	public static final int exp2(int x)
	{
		Integer out = 1;
		for ( int i = 0; i < x; i++ )
			out *= 2;
		return out;
	}
	
	/**
	 * \brief Calculate 2 to the power of x, where x is a double.
	 * 
	 * @param x The exponent
	 * @return 2<sup><b>x</b></sup>
	 */
	public static final double exp2(double x)
	{
		return Math.pow(2, x);
	}
	
	/**
	 * \brief Calculate the hypotenuse of a 3D right-angled triangle using
	 * Pythagoras.
	 * 
	 * <p>For example, if you know the side lengths of a cuboid, this will 
	 * give you the length of the diagonal.</p>
	 * 
	 * <p>Formula: sqrt( a*a + b*b + c*c )</p>
	 * 
	 * <p>Note: for a 2D hypotenuse, use Math.hypot(a, b)</p>
	 * 
	 * @param a double value of the length of the first side of the triangle.
	 * @param b double value of the length of the second side of the triangle.
	 * @param c double value of the length of the third side of the triangle.
	 * @return double value of the hypotenuse of the triangle.
	 */
	public static final double hypotenuse(double a, double b, double c)
	{
		return Math.sqrt( sq(a) + sq(b) + sq(c) );
	}
	
	/**
	 * \brief Calculate the hypotenuse of a 3D right-angled triangle using
	 * Pythagoras.
	 * 
	 * <p>For example, if you know the side lengths of a cuboid, this will 
	 * give you the length of the diagonal.</p>
	 * 
	 * <p>Formula: sqrt( a*a + b*b + c*c )</p>
	 * 
	 * @param a integer value of the length of the first side of the triangle.
	 * @param b integer value of the length of the second side of the triangle.
	 * @param c integer value of the length of the third side of the triangle.
	 * @return double value of the hypotenuse of the triangle.
	 */
	public static final double hypotenuse(int a, int b, int c)
	{
		return Math.sqrt( sq(a) + sq(b) + sq(c) );
	}
	
	/**
	 * \brief Calculate the side of a 2D right-angled triangle  using
	 * Pythagoras.
	 * 
	 * <p>Formula: sqrt( hypotenuse*hypotenuse - side*side )</p>
	 * 
	 * @param side double value of the length of the other side of the triangle.
	 * @param hypotenuse double value of the length of the hypotenuse of the
	 * triangle.
	 * @return double value of the length of the side of the triangle being
	 * calculated.
	 */
	public static final double triangleSide(double hypotenuse, double side)
	{
		return Math.sqrt( sq(hypotenuse) - sq(side) );
	}
	
	/**
	 * \brief Calculate the side of a 3D right-angled triangle  using
	 * Pythagoras.
	 * 
	 * <p>For example, if you know two of the side lengths of a cuboid and the
	 * length of the diagonal, this will give you the length of the third 
	 * side.</p>
	 * 
	 * <p>Formula: sqrt( hypotenuse*hypotenuse - sideA*sideA -sideB*sideB)</p>
	 * 
	 * @param sideA double value of the length of the first of the other sides
	 * of the triangle.
	 * @param sideB double value of the length of the second of the other
	 * sides of the triangle.
	 * @param hypotenuse double value of the hypotenuse of the triangle.
	 * @return double value of the side of the triangle being calculated.
	 */
	public static final double triangleSide(double hypotenuse, double sideA,
																double sideB)
	{
		return Math.sqrt( sq(hypotenuse) - sq(sideA) - sq(sideB) );
	}
	
	/**
	 * \brief Calculates the roots of a quadratic equation.
	 * 
	 * <p>Form of the equation should be <b>a</b>*<i>x</i>^2 + 
	 * <b>b</b>*<i>x</i> + <b>c</b> = 0, where <i>x</i> is the variable to be
	 * solved.</p>
	 * 
	 * @param a double value of the <i>x<sup>2</sup></i> coefficient.
	 * @param b double value of the <i>x</i> coefficient.
	 * @param c double value of the constant.
	 * @return two Complex roots of the given quadratic equation. 
	 */
	public static final Complex[] rootsQuadratic(double a, double b, double c)
	{
		Double discriminant = sq(b) - (4.0*a*c);
		Complex[] out = new Complex[2];
		if ( discriminant < 0.0 )
		{
			discriminant = Math.sqrt(-discriminant);
			for ( Complex num : out )
				num.setImag(discriminant);
		}
		else
		{
			discriminant = Math.sqrt(discriminant);
			for ( Complex num : out )
				num.setReal(discriminant);
		}
		for ( Complex num : out )
		{
			num.add(-b);
			num.div(2*a);
		}
		return out;
	}
	
	/*  ----------------------------- Shapes  ----------------------------- */
	
	/**
	 * \brief Calculate the area of circle with given <b>radius</b>.
	 * 
	 * <p>The formula for this is pi*<b>radius</b><sup>2</sup>.</p>
	 * 
	 * @see {@link #radiusOfACircle(double radius)}
	 * @param radius Radius of the circle
	 * @return Area of the circle.
	 */
	public static final double areaOfACircle(double radius)
	{
		return Math.PI * sq(radius);
	}
	
	/**
	 * \brief Calculate the volume of a cylinder with given <b>radius</b> and
	 * <b>length</b>.
	 * 
	 * <p>The formula for this is pi*l*<b>radius</b><sup>2</sup>.</p>
	 * 
	 * @param radius Radius of the cylinder.
	 * @param length Length of the cylinder.
	 * @return Volume of the cylinder.
	 */
	public static final double volumeOfACylinder(double radius, double length)
	{
		return areaOfACircle(radius) * length;
	}
	
	/**
	 * \brief Calculate the radius of a circle with given <b>area</b>.
	 * 
	 * <p>The formula for this is (<b>area</b>/pi)<sup>1/2</sup>.</p>
	 * 
	 * @see {@link #areaOfACircle(double radius)}
	 * @param area Area of the circle.
	 * @return Radius of the circle.
	 */
	public static final double radiusOfACircle(double area)
	{
		return Math.sqrt(area / Math.PI);
	}
	
	/**
	 * \brief Calculate the radius of a cylinder with given <b>volume</b> and
	 * <b>length</b>.
	 * 
	 * <p>This is calculated from the area of the cross-section: 
	 * <b>volume</b>/<b>length</b>.</p>
	 * 
	 * @see {@link #lengthOfACylinder(double volume, double radius)}
	 * @param volume Volume of the cylinder.
	 * @param length Length of the cylinder.
	 * @return Radius of the cylinder.
	 */
	public static final double radiusOfACylinder(double volume, double length)
	{
		return radiusOfACircle(volume/length);
	}
	
	/**
	 * \brief Calculate the length of a cylinder with given <b>volume</b> and
	 * <b>radius</b>.
	 * 
	 * <p>This is calculated from the area of the cross-section: 
	 * <b>volume</b>/(pi*<b>radius</b><sup>2</sup>).</p>
	 * 
	 * @see {@link #radiusOfACylinder(double volume, double length)}
	 * @param volume Volume of the cylinder.
	 * @param radius Radius of the cylinder.
	 * @return Length of the cylinder.
	 */
	public static final double lengthOfACylinder(double volume, double radius)
	{
		return volume / areaOfACircle(radius);
	}
	
	/**
	 * \brief Calculate the volume of a sphere with given <b>radius</b>.
	 * 
	 * <p>The formula for this is 4/3 * pi * <b>radius</b><sup>3</sup>.</p>
	 * 
	 * @see {@link #radiusOfASphere(double volume)}
	 * @param radius Radius of the sphere.
	 * @return Volume of the sphere.
	 */
	public static final double volumeOfASphere(double radius)
	{
		return (4.0/3.0) * Math.PI * cube(radius);
	}
	
	/**
	 * \brief Calculate the radius of a sphere with volume v.
	 * 
	 * The formula for this is ( (v*3)/(4*pi) )^(1/3)
	 * 
	 * <p>The formula for this is 
	 * ( (3*<b>volume</b>) / (4*pi) )<sup>1/3</sup>.</p>
	 * 
	 * @see {@link #volumeOfASphere(double radius)}
	 * @param volume Volume of the sphere.
	 * @return Radius of the sphere.
	 */
	public static final double radiusOfASphere(double volume)
	{
		return cubeRoot(volume*0.75/Math.PI);
	}
	
	
	/*  ----------------------- Dealing with signs  ----------------------- */
	
	/**
	 * \brief Unequivocally determine the sign of a double <b>value</b>. 
	 * 
	 * <p>Copied from 
	 * http://stackoverflow.com/questions/3994531/how-to-determine-if-a-number-is-positive-or-negative-in-java
	 * on 7 August 2013.</p>
	 * 
	 * @param value double to be inspected.
	 * @return integer with the sign of <b>value</b>: -1, 0, or +1
	 */
	public static int sign(double value)
	{
		// Not sure if checking -0.0 is necessary, but better safe than sorry!
	    if ( value == 0.0 || value == -0.0 )
	    	return 0;
	    value *= Double.POSITIVE_INFINITY;
	    if ( value == Double.POSITIVE_INFINITY )
	    	return +1;
	    if ( value == Double.NEGATIVE_INFINITY )
	    	return -1;
	    throw new IllegalArgumentException("Unfathomed double");
	}
	
	/**
	 * \brief Determine if two doubles have the same sign.
	 * 
	 * <p>Note that this is true if if either (or both) of the arguments is
	 * zero.<p>
	 * 
	 * @param a	First double value.
	 * @param b	Second double value.
	 * @return boolean noting whether or not the two have the same sign.
	 */
	public static boolean sameSign(double a, double b)
	{
		return ( sign(a)*sign(b) >= 0 );
	}
	
	/**
	 * \brief Output a double value as a string, in a particular decimal
	 * format.
	 * 
	 * <p>If <b>scFormat</b> is true, use dfSc; if false, use dfUs.</p>
	 * 
	 * @param value	double to be formatted.
	 * @param scFormat	The decimal format to use.
	 * @return	A String containing that <b>value</b> in the required decimal
	 * format.
	 */
	public static String toString(double value, boolean scFormat)
	{
		return (scFormat) ? dfSc.format(value) : dfUs.format(value); 
	}
	
	/**
	 * \brief Searches for a substring within a main string, and returns a
	 * double immediately after if it exists.
	 * 
	 * <p>Note that Double.NaN will be returned if the substring is not found,
	 * the substring is at the very end of the main string, or there is no
	 * double immediately after.</p>
	 * 
	 * @param mainString The string within which the search will be made.
	 * @param subString The substring being searched for.
	 * @return The double immediately after subString, if found. If not found,
	 * 1.0
	 */
	public static double doubleAfterSubstring(String mainString,
															String subString)
	{
		double out = Double.NaN;
		if ( mainString.contains(subString) )
		{
			int startIndex = mainString.indexOf(subString) + 
														subString.length();
			int endIndex = startIndex + 1;
			int maxIndex = mainString.length();
			String potential;
			while ( (endIndex < maxIndex) && 
					(isNumeric(mainString.substring(startIndex, endIndex+1))))
			{
				endIndex++;
			}
			potential = mainString.substring(startIndex, endIndex);
			if ( isNumeric(potential) )
				out = Double.parseDouble(potential); 
		}
		return out;
	}
	
	/**
	 * \brief Checks if the supplied String can be safely parsed as a double.
	 * 
	 * @param str The string to be tested.
	 * @return True or False depending on the outcome of the test.
	 */
	public static boolean isNumeric(String str)  
	{ 
	  try
	  {
	    @SuppressWarnings("unused")
		double d = Double.parseDouble(str);  
	  }
	  catch(NumberFormatException nfe)
	  {
	    return false;
	  }
	  return true;
	}
	
	/*  -------------------- Random number generation  -------------------- */
	
	/**
	 * \brief Return a random boolean.
	 * 
	 * @return A random boolean.
	 */
	public static boolean getRandBool()
	{
		return random.nextBoolean();
	}
	
	/**
	 * \brief Return a uniformly distributed random number between 0 and 1.
	 * 
	 * <p>Lower bound (0) is inclusive, upper bound (1) is exclusive.</p>
	 * 
	 * @return A uniformly distributed random number in [0,1).
	 */
	public static double getUniRandDbl()
	{
		return random.nextDouble();
	}
	
	/**
	 * \brief Return a double random number between two set bounds.
	 * 
	 * @param lBound	Lower bound (inclusive).
	 * @param uBound	Upper bound (exclusive).
	 * @return A uniformly distributed random double number in
	 * [lBound, uBound).
	 */
	public static double getUniRandDbl(double lBound, double uBound)
	{
		return getUniRandDbl()*(uBound-lBound)+lBound;
	}
	
	/**
	 * \brief Return a uniformly distributed random number between 0 and 2*pi.
	 * 
	 * <p>Lower bound (0) is inclusive, upper bound (2*pi) is exclusive.</p>
	 * 
	 * @return A uniformly distributed random number in [0, 2*pi).
	 */
	public static double getUniRandAngle()
	{
		return 2 * Math.PI * random.nextDouble();
	}
	
	/**
	 * \brief Return 2 to the power of a uniformly distributed random number
	 * in [0,1).
	 * 
	 * @return 2 to the power of a uniformly distributed random number in
	 * [0,1).
	 */
	public static double getExp2Rand()
	{
		return exp2( getUniRandDbl() );
	}
	
	/**
	 * \brief Return an integer random number less than the upper bound
	 * supplied.
	 * 
	 * @param uBound Upper bound (exclusive).
	 * @return A uniformly distributed random integer number in [0, uBound).
	 */
	public static int getUniRandInt(int uBound)
	{
		return random.nextInt(uBound);
	}
	
	/**
	 * \brief Return a random integer number between two set bounds.
	 * 
	 * @param lBound Lower bound (inclusive).
	 * @param uBound Upper bound (exclusive).
	 * @return A uniformly distributed random integer in [lBound, uBound).
	 */
	public static int getUniRandInt(int lBound, int hBound)
	{
		return getUniRandInt(hBound-lBound) + lBound;
	}
	
	/**
	 * \brief Return a random double number between two set bounds.
	 * 
	 * @param lBound Lower bound (inclusive).
	 * @param hBound Upper bound (exclusive).
	 * @return A uniformly distributed random double in [lBound, uBound).
	 */
	public static double getUniRand(double lBound, double hBound)
	{
		return random.nextDouble()*(hBound-lBound) + lBound;
	}
	
	/**
	 * \brief Return a truncated N(0,1) distributed random number.
	 * 
	 * <p>Normal distributed random numbers are truncated at 2*sigma to prevent
	 * extreme values.</p>
	 * 
	 * @return Truncated N(0,1) distributed random number. 
	 */
	public static double getNormRand()
	{
		double phi;
		do {
			phi = random.nextGaussian();
		} while ( Math.abs(phi) > 2 );
		return phi;
	}
	
	/**
	 * \brief Randomise a value with a normal distribution in a range fixed by
	 * the Coefficient of Variation (CV).
	 * 
	 * <p>Randomise a value mu with a normal (Gaussian) distribution in a
	 * range fixed by <b>cv</b>. The result will be the same sign (+/-) as
	 * <b>mu</b>.</p> 
	 * 
	 * <p>This is different from 
	 * {@link #deviateFromSD(double mu, double sd)}!</p>
	 * 
	 * <p> Examples:<ul><li>If <b>mu</b> = 1 and <b>cv</b> = .1, the results
	 * form a truncated normal distribution between 0.8 and 1.2</li><li>If
	 * <b>mu</b> = -3 and <b>cv</b> = .05 the results form a truncated normal
	 * distribution between -3.3 and -2.7</li></ul></p>
	 * 
	 * @param mu Mean value.
	 * @param cv Coefficient of Variation.
	 * @return N(<b>mu</b>, <b>cv</b>)-distributed random value within
	 * [<b>mu</b>*(1-2*<b>cv</b>), <b>mu</b>*(1+2*<b>cv</b>)]
	 */
	public static double deviateFromCV(double mu, double cv) 
	{
		/*
		 * No point going further if either is zero. 
		 */
		if ( mu == 0.0 || cv == 0.0)
			return mu;
		/*
		 * Calculate the value.
		 */
		Double out;
		do {
			out = mu * ( 1.0 + cv*getNormRand() );
		} while ( ! sameSign(out, mu) );
		return out;
	}
	
	/**
	 * \brief Randomise a value with a normal distribution in a range fixed by
	 * the Standard Deviation (SD).
	 * 
	 * <p>Randomise a value mu with a normal (Gaussian) distribution in a
	 * range fixed by <b>sd</b>. The result will be the same sign (+/-) as
	 * <b>mu</b>.</p>
	 * 
	 * <p>This is different from 
	 * {@link #deviateFromCV(double mu, double cv)}!</p>
	 * 
	 * <p>Examples:<ul><li>If <b>mu</b> = 1 and <b>sd</b> = .1, the results
	 * form a truncated normal distribution between 0.8 and 1.2</li><li>If 
	 * <b>mu</b> = -3 and <b>sd</b> = .05 the results form a truncated normal
	 * distribution between -3.1 and -2.9</li></ul></p>
	 * 
	 * @param mu Mean value.
	 * @param sd Standard Deviation.
	 * @return N(<b>mu</b>, <b>sd</b>)-distributed random value within
	 * [<b>mu</b>-2*<b>sd</b>, <b>mu</b>+2*<b>sd</b>]
	 */
	public static double deviateFromSD(double mu, double sd) 
	{
		/*
		 * No point going further if the standard deviation is zero. 
		 */
		if ( sd == 0.0 )
			return mu;
		/*
		 * Calculate the value.
		 */
		Double out;
		do {
			out = mu + ( sd * getNormRand() );
		} while ( ! sameSign(out, mu) );
		return out;
	}                           
}

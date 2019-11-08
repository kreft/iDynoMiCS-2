package test.junit.oldTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import linearAlgebra.Vector;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.ResolutionCalculator;
import shape.resolution.UniformResolution;
import test.OldTests;

/**
 * \brief Set of tests for the shape iterator, using a square with cyclic
 * dimension.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class IteratorForCyclicSquaresTests
{
	private Shape _shape;
	
	private final static int sideLength = 4;
	
	@Before
	public void createTestObjects()
	{
		this._shape = OldTests.GetShape("Rectangle");
		for ( DimName dimName : new DimName[] { DimName.X, DimName.Y })
		{
			Dimension dimension = this._shape.getDimension(dimName);
			dimension.setLength(sideLength);
			ResolutionCalculator resCalc = new UniformResolution(dimension);
			resCalc.setResolution(1.0);
			this._shape.setDimensionResolution(dimName, resCalc);
			this._shape.makeCyclic(dimName);
		}
	}
	
	@Test
	public void iteratorFindsAllNeighbours()
	{
		int[] trueNhb = Vector.zerosInt(3), nhb;
		for ( int[] current = this._shape.resetIterator();
				this._shape.isIteratorValid();
				current = this._shape.iteratorNext() )
		{
			trueNhb[0] = (current[0]+sideLength-1) % sideLength;
			trueNhb[1] = current[1];
			nhb = this._shape.resetNbhIterator();
			assertTrue(Vector.areSame(trueNhb, nhb));
			
			trueNhb[0] = (current[0]+1) % sideLength;
			trueNhb[1] = current[1];
			nhb = this._shape.nbhIteratorNext();
			assertTrue(Vector.areSame(trueNhb, nhb));
			
			trueNhb[0] = current[0];
			trueNhb[1] = (current[1]+sideLength-1) % sideLength;
			nhb = this._shape.nbhIteratorNext();
			assertTrue(Vector.areSame(trueNhb, nhb));
			
			trueNhb[0] = current[0];
			trueNhb[1] = (current[1]+1) % sideLength;
			nhb = this._shape.nbhIteratorNext();
			assertTrue(Vector.areSame(trueNhb, nhb));
			
			nhb = this._shape.nbhIteratorNext();
			assertFalse(this._shape.isNbhIteratorValid());
		}
	}
}

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
 * \brief Set of tests for the shape iterator, using a line with cyclic
 * dimension.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class IteratorForCyclicLineTests
{
	private Shape _shape;
	
	@Before
	public void createTestObjects()
	{
		this._shape = OldTests.GetShape("Line");
		Dimension x = this._shape.getDimension(DimName.X);
		x.setLength(4.0);
		ResolutionCalculator resCalc = new UniformResolution(x);
		resCalc.setResolution(1.0);
		this._shape.setDimensionResolution(DimName.X, resCalc);
		this._shape.makeCyclic(DimName.X);
	}
	
	@Test
	public void iteratorFindsAllNeighbours()
	{
		int[] trueNhb = Vector.zerosInt(3);
		for ( int[] current = this._shape.resetIterator();
				this._shape.isIteratorValid();
				current = this._shape.iteratorNext() )
		{
			trueNhb[0] = (current[0]+3) % 4;
			int[] nhb = this._shape.resetNbhIterator();
			assertTrue(Vector.areSame(trueNhb, nhb));
			
			trueNhb[0] = (current[0]+1) % 4;
			nhb = this._shape.nbhIteratorNext();
			assertTrue(Vector.areSame(trueNhb, nhb));
			
			nhb = this._shape.nbhIteratorNext();
			assertFalse(this._shape.isNbhIteratorValid());
		}
	}
}

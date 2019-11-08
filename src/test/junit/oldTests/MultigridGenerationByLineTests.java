package test.junit.oldTests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import boundary.SpatialBoundary;
import boundary.spatialLibrary.FixedBoundary;
import boundary.spatialLibrary.SolidBoundary;
import shape.Dimension;
import shape.Dimension.DimName;
import shape.Shape;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import test.OldTests;

public class MultigridGenerationByLineTests
{
	private Shape _testShape;
	
	@Before
	public void setup()
	{
		OldTests.setupSimulatorForTest(1.0, 1.0, this.getClass().getName());
		this._testShape = OldTests.GetShape("Line");
		Dimension x = this._testShape.getDimension(DimName.X);
		x.setLength(8.0);
		ResolutionCalculator resCalc = new MultigridResolution(x);
		resCalc.setResolution(1.0);
		this._testShape.setDimensionResolution(DimName.X, resCalc);
	}
	
	@Test
	public void coarserShapeHasSameBoundaries()
	{
		SpatialBoundary min = new FixedBoundary();
		SpatialBoundary max = new SolidBoundary();
		min.setParent(this._testShape);
		max.setParent(this._testShape);
		this._testShape.setBoundary(DimName.X, 0, min);
		this._testShape.setBoundary(DimName.X, 1, max);
		
		Shape coarserShape = this._testShape.generateCoarserMultigridLayer();
		
		Dimension xCoarser = coarserShape.getDimension(DimName.X);
		SpatialBoundary minCoarser = xCoarser.getBoundary(0);
		SpatialBoundary maxCoarser = xCoarser.getBoundary(1);
		
		assertEquals(min, minCoarser);
		assertEquals(max, maxCoarser);
	}
	
	@Test
	public void boundariesParentRemainsFinerShape()
	{
		SpatialBoundary min = new FixedBoundary();
		SpatialBoundary max = new SolidBoundary();
		min.setParent(this._testShape);
		max.setParent(this._testShape);
		this._testShape.setBoundary(DimName.X, 0, min);
		this._testShape.setBoundary(DimName.X, 1, max);
		
		Shape coarserShape = this._testShape.generateCoarserMultigridLayer();
		
		Dimension xCoarser = coarserShape.getDimension(DimName.X);
		SpatialBoundary minCoarser = xCoarser.getBoundary(0);
		SpatialBoundary maxCoarser = xCoarser.getBoundary(1);
		
		assertEquals(this._testShape, minCoarser.getParent());
		assertEquals(this._testShape, maxCoarser.getParent());
	}
}

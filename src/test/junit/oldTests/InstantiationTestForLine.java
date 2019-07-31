package test.junit.oldTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static test.OldTests.TOLERANCE;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dataIO.XmlHandler;
import instantiable.Instance;
import shape.Dimension.DimName;
import shape.Shape;
import shape.ShapeLibrary.Line;

public class InstantiationTestForLine
{
	private Shape _shape;
	
	@Before
	public void createTestObjects()
	{
		Document document = XmlHandler.newDocument();
		
		Element shapeElem = document.createElement("shape");
		shapeElem.setAttribute("class", "Line");
		shapeElem.setAttribute("resolutionCalculator", "UniformResolution");
		
		Element xElem = document.createElement("dimension");
		xElem.setAttribute("name", "X");
		xElem.setAttribute("isCyclic", "false");
		xElem.setAttribute("targetResolution", "1.0");
		xElem.setAttribute("max", "40.0");
		shapeElem.appendChild(xElem);
		
		this._shape = (Shape)Instance.getNew(shapeElem, null, (String[]) null);
	}
	
	@Test
	public void shapeIsLine()
	{
		assertTrue(this._shape instanceof Line);
	}
	
	@Test
	public void shapeHasCorrectNumberOfVoxels()
	{
		double[][][] array = this._shape.getNewArray(0.0);
		assertEquals(40, array.length);
		for ( int i = 0; i < array.length; i++ )
		{
			assertEquals(1, array[i].length);
			assertEquals(1, array[i][0].length);
		}
	}
	
	@Test
	public void insignificantBoundariesHaveNominalLength()
	{
		double yLength = this._shape.getDimension(DimName.Y).getLength();
		double zLength = this._shape.getDimension(DimName.Z).getLength();
		assertEquals(1.0, yLength, TOLERANCE);
		assertEquals(1.0, zLength, TOLERANCE);
	}
}

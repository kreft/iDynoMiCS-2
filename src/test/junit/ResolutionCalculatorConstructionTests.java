package test.junit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import shape.resolution.UniformResolution;

public class ResolutionCalculatorConstructionTests
{
	@Test
	public void CanConstructUniformResolution()
	{
		ResolutionCalculator rC =
				ResolutionCalculator.getNew("UniformResolution");
		
		assertTrue(rC instanceof UniformResolution);
	}
	
	@Test
	public void CanConstructMultigridResolution()
	{
		ResolutionCalculator rC =
				ResolutionCalculator.getNew("MultigridResolution");
		
		assertTrue(rC instanceof MultigridResolution);
	}
}

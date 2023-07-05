package test.junit.oldTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import instantiable.Instance;
import shape.resolution.MultigridResolution;
import shape.resolution.ResolutionCalculator;
import shape.resolution.UniformResolution;

public class ResolutionCalculatorConstructionTests
{
	@Test
	public void CanConstructUniformResolution()
	{
		ResolutionCalculator rC = (ResolutionCalculator)
				Instance.getNew("uniformResolution", null);
		
		assertTrue(rC instanceof UniformResolution);
	}
	
	@Test
	public void CanConstructMultigridResolution()
	{
		ResolutionCalculator rC = (ResolutionCalculator) 
				Instance.getNew("multigridResolution", null);
		
		assertTrue(rC instanceof MultigridResolution);
	}
}

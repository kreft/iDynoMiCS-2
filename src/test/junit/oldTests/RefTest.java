package test.junit.oldTests;

import org.junit.Test;

import referenceLibrary.ClassRef;

public class RefTest {

	@Test
	public void test()
	{
		ClassRef.getAllOptionsFullPath();
		ClassRef.getAllOptions();
		ClassRef.getAllOptions("aspect.event");
		ClassRef.getAllOptions("aspect.calculated");
	}
}

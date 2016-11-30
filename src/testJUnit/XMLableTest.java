/**
 * 
 */
package testJUnit;

import org.junit.Test;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;

import generalInterfaces.Instantiatable;
import nodeFactory.NodeConstructor;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class XMLableTest
{
	public static class TestXMLable implements Instantiatable
	{
		private int x = 1;
		
		public void init(Element xmlElem, NodeConstructor parent)
		{
			// TODO
		}
		
		public void setX(int x)
		{
			this.x = x;
		}
		
		public boolean isX(int y)
		{
			return this.x == y;
		}
	}
	
	@Test
	public void dummyClassIsInstanciable()
	{
		/* This should crash if the method is wrong. */
		TestXMLable t = (TestXMLable) Instantiatable.getNewInstance(null, null, "testJUnit.XMLableTest$testXMLable");
		/* Dummy test to check that t in an instance. */
		assertTrue( t.isX(1) );
		t.setX(3);
		assertTrue( t.isX(3) );
	}
}

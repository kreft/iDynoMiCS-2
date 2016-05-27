/**
 * 
 */
package testJUnit;

import org.junit.Test;
import org.w3c.dom.Element;

import static org.junit.Assert.assertTrue;

import generalInterfaces.XMLable;
import nodeFactory.ModelNode;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class XMLableTest
{
	public static class TestXMLable implements XMLable
	{
		private int x = 1;
		
		public void init(Element xmlElem)
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
		
		public static Object getNewInstance(String className)
		{
			return XMLable.getNewInstance(className, "testJUnit.XMLableTest$");
		}
		
		@Override
		public String getXml() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ModelNode getNode() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	@Test
	public void dummyClassIsInstanciable()
	{
		/* This should crash if the method is wrong. */
		TestXMLable t = (TestXMLable)TestXMLable.getNewInstance("testXMLable");
		/* Dummy test to check that t in an instance. */
		assertTrue( t.isX(1) );
		t.setX(3);
		assertTrue( t.isX(3) );
	}
}

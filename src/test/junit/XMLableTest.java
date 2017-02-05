/**
 * 
 */
package test.junit;

import org.junit.Test;
import org.w3c.dom.Element;

import instantiable.Instance;
import instantiable.Instantiable;
import settable.Module;
import settable.Settable;

import static org.junit.Assert.assertTrue;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class XMLableTest
{
	public static class TestSettable implements Settable
	{

		@Override
		public Module getModule() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String defaultXmlTag() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setParent(Settable parent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Settable getParent() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class TestXMLable implements Instantiable
	{
		private int x = 1;
		
		public void instantiate(Element xmlElem, Settable parent)
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
		TestXMLable t = (TestXMLable) Instance.getNew(null, null, 
				"test.junit.XMLableTest$TestXMLable");
		/* Dummy test to check that t in an instance. */
		assertTrue( t.isX(1) );
		t.setX(3);
		assertTrue( t.isX(3) );
	}
}

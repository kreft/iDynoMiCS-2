/**
 * 
 */
package boundary.agent;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import modelBuilder.InputSetter;

/**
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public class AgentMethodLibrary
{
	
	public static class SolidSurface extends AgentMethod
	{
		@Override
		public String getName()
		{
			return "Solid surface";
		}
		
		@Override
		public List<InputSetter> getRequiredInputs()
		{
			return new LinkedList<InputSetter>();
		}
		
		@Override
		public void acceptInput(String name, Object input)
		{
			/* Do nothing. */
		}
		
		@Override
		public void init(Element xmlElem)
		{
			
		}
		
		@Override
		public String getXml()
		{
			return null;
		}
		
	}
	
}

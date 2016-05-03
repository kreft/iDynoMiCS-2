/**
 * 
 */
package boundary.agent;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import idynomics.AgentContainer;
import idynomics.Compartment;
import idynomics.EnvironmentContainer;
import modelBuilder.InputSetter;
import surface.Surface;

/**
 * \brief TODO
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
		
		@Override
		public void agentsArrive(Compartment comp, Surface surf)
		{
			/* Do nothing. */
		}
	}
	
	public static class BoundaryLayer extends AgentMethod
	{
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<InputSetter> getRequiredInputs() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void acceptInput(String name, Object input) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void init(Element xmlElem) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getXml() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void agentsArrive(Compartment comp, Surface surf)
		{
			
		}
	}
}

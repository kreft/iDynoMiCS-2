/**
 * 
 */
package boundary;

import java.util.LinkedList;
import java.util.List;

import boundary.grid.GridBoundaryLibrary.ZeroFlux;
import modelBuilder.InputSetter;

/**
 * \brief Collection of commonly used {@code Boundary} classes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class BoundaryLibrary
{
	
	public static class CustomBoundary extends Boundary
	{
		public CustomBoundary()
		{
			
		}
		
		@Override
		public List<InputSetter> getRequiredInputs()
		{
			List<InputSetter> out = new LinkedList<InputSetter>();
			// TODO GridMethod, AgentMethod
			//out.add(new GridMethodMaker(Requirement.ONE_TO_MANY));
			//out.add(new AgentMethodMaker(Requirement.EXACTLY_ONE));
			return out;
		}
	}
	
	public static class SolidBoundary extends Boundary
	{
		public SolidBoundary()
		{
			this._defaultGridMethod = new ZeroFlux();
			//this._agentMethod = TODO
		}
	}
	
	public static class ChemostatConnection extends Boundary
	{
		
	}
}

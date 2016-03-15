/**
 * 
 */
package boundary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import boundary.grid.GridMethod.GridMethodMaker;
import boundary.grid.GridMethodLibrary.*;
import boundary.agent.AgentMethod;
import boundary.grid.GridMethod;
import dataIO.XmlLabel;
import modelBuilder.InputSetter;
import modelBuilder.ParameterSetter;
import modelBuilder.SubmodelMaker.Requirement;

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
			out.add(new GridMethodMaker(
						"Default Grid Method", Requirement.ZERO_OR_ONE, this));
			out.add(new GridMethodMaker(
							"Grid Method", Requirement.ZERO_TO_MANY, this));
			//out.add(new AgentMethodMaker(Requirement.EXACTLY_ONE));
			return out;
		}
		
		public void acceptInput(String name, Object input)
		{
			if ( input instanceof GridMethod )
			{
				GridMethod gm = (GridMethod) input;
				// TODO Need to be very careful about how non-default methods
				// are assigned!
				if ( name.equals("Default Grid Method") )
					this._defaultGridMethod = gm;
				else
					this._gridMethods.put(name, gm);
			}
			if ( input instanceof AgentMethod )
				this._agentMethod = (AgentMethod) input;
		}
	}
	
	/**************************************************************************
	 * SPATIAL BOUNDARIES
	 *************************************************************************/
	
	public static class SolidBoundary extends Boundary
	{
		public SolidBoundary()
		{
			this._defaultGridMethod = new ZeroFlux();
			//this._agentMethod = TODO
		}
	}
	
	/**************************************************************************
	 * CHEMOSTAT BOUNDARIES
	 *************************************************************************/
	
	public static class ChemostatInflow extends Boundary
	{
		protected double _flowRate;
		
		protected HashMap<String, Double> _concentrations;
		
		@Override
		public List<InputSetter> getRequiredInputs()
		{
			List<InputSetter> out = new LinkedList<InputSetter>();
			out.add(new ParameterSetter("flowRate", this, "Double"));
			// TODO concentrations? AspectRegistry?
			return out;
		}
		
		@Override
		public void acceptInput(String name, Object input)
		{
			if (name.equals("flowRate") && input instanceof Double)
				this._flowRate = (Double) input;
		}
	}
	
	public static class ChemostatConnection extends Boundary
	{
		protected String _name;
		
		protected String _partnerName;
		
		protected double _flowRate;
		
		@Override
		public List<InputSetter> getRequiredInputs()
		{
			List<InputSetter> out = new LinkedList<InputSetter>();
			out.add(new ParameterSetter(XmlLabel.nameAttribute, this, "String"));
			out.add(new ParameterSetter("flowRate", this, "Double"));
			out.add(new ParameterSetter("partnerName", this, "String"));
			//out.add(new GridMethodMaker(Requirement.ONE_TO_MANY));
			//out.add(new AgentMethodMaker(Requirement.EXACTLY_ONE));
			return out;
		}
		
		@Override
		public void acceptInput(String name, Object input)
		{
			if ( input instanceof String )
			{
				String str = (String) input;
				if ( name.equals(XmlLabel.nameAttribute) )
					this._name = str;
				if ( name.equals("partnerName") )
					this._partnerName = str;
			}
			
			if (name.equals("flowRate") && input instanceof Double)
				this._flowRate = (Double) input;
		}
	}
}

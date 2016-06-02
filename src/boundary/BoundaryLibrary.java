/**
 * 
 */
package boundary;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import boundary.agent.AgentMethodLibrary.*;
import boundary.grid.GridMethod.GridMethodMaker;
import boundary.grid.GridMethodLibrary.*;
import boundary.agent.AgentMethod;
import boundary.grid.GridMethod;
import dataIO.ObjectRef;
import dataIO.XmlLabel;
import idynomics.Compartment;
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

	public final static String PARTNER = XmlLabel.boundaryPartner;
		
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
							Boundary.DEFAULT_GM, Requirement.ZERO_OR_ONE, this));
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
				if ( name.equals(Boundary.DEFAULT_GM) )
					this._defaultGridMethod = gm;
				else
					this._gridMethods.put(name, gm);
			}
			if ( input instanceof AgentMethod )
				this._agentMethod = (AgentMethod) input;
		}
		
		@Override
		public Boundary makePartnerBoundary()
		{
			// TODO?
			return null;
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
			this._agentMethod = new SolidSurface();
		}
		
		@Override
		public Boundary makePartnerBoundary()
		{
			return null;
		}
	}
	
	public static class BulkBLBoundary extends Boundary
	{
		public BulkBLBoundary()
		{
			//this._defaultGridMethod = new ZeroFlux();
			this._agentMethod = new BoundaryLayer();
		}
		
		@Override
		public Boundary makePartnerBoundary()
		{
			// TODO 
			return null;
		}
	}
	
	public static class FixedBoundary extends Boundary
	{
		public FixedBoundary()
		{
			this._defaultGridMethod = new ConstantDirichlet();
			// TODO change agent method?
			this._agentMethod = new SolidSurface();
		}
		
		public FixedBoundary(double value)
		{
			this();
			this.setValue(value);
		}
		
		public void setValue(double value)
		{
			((ConstantDirichlet) this._defaultGridMethod).setValue(value);
		}
		
		public void setValue(String soluteName, double value)
		{
			if ( soluteName.equals(Boundary.DEFAULT_GM) )
				this.setValue(value);
			else
			{
				ConstantDirichlet gm = new ConstantDirichlet();
				gm.setValue(value);
				this._gridMethods.put(soluteName, gm);
			}
		}
		
		@Override
		public Boundary makePartnerBoundary()
		{
			return null;
		}
		
	}
	
	/**************************************************************************
	 * CHEMOSTAT BOUNDARIES
	 *************************************************************************/
	

	public static class ChemostatOutflow extends Boundary
	{
		protected double _flowRate;
		
		protected double _agentsToDiluteTally;
		
		public void setFlowRate(double flowRate)
		{
			this._flowRate = flowRate;
		}
		
		public void setPartnerCompartment(Compartment comp)
		{
			ChemostatInflow cIn = new ChemostatInflow();
			cIn.setFlowRate(this._flowRate);
			comp.getShape().addOtherBoundary(cIn);
			this.setPartner(cIn);
		}
		
		@Override
		public Boundary makePartnerBoundary()
		{
			ChemostatInflow cIn = new ChemostatInflow();
			cIn.setFlowRate(this._flowRate);
			this._partner = cIn;
			return cIn;
		}
		
		public double getFlowRate()
		{
			return this._flowRate;
		}
		
		/**
		 * 
		 * @return
		 */
		public double getAgentsToDiluteTally()
		{
			return this._agentsToDiluteTally;
		}
		
		/**
		 * 
		 * @param timeStep
		 */
		public void updateAgentsToDiluteTally(double timeStep)
		{
			/* Remember to subtract, since flow rate out is negative. */
			this._agentsToDiluteTally -= this._flowRate * timeStep;
		}
		
		public void knockDownAgentsToDiluteTally()
		{
			this._agentsToDiluteTally--;
		}
		
		/**
		 * 
		 */
		public void resetAgentsToDiluteTally()
		{
			this._agentsToDiluteTally = 0.0;
		}
	}
	
	
	public static class ChemostatInflow extends Boundary
	{
		protected double _flowRate;
		
		protected HashMap<String, Double> _concentrations;
		
		@Override
		public List<InputSetter> getRequiredInputs()
		{
			List<InputSetter> out = new LinkedList<InputSetter>();
			out.add(new ParameterSetter("flowRate", this, ObjectRef.DBL));
			// TODO concentrations? AspectRegistry?
			return out;
		}
		
		@Override
		public void acceptInput(String name, Object input)
		{
			if (name.equals("flowRate") && input instanceof Double)
				this._flowRate = (Double) input;
		}
		
		public void setFlowRate(double flowRate)
		{
			this._flowRate = flowRate;
		}
		
		public double getFlowRate()
		{
			return this._flowRate;
		}
		
		public double getConcentration(String soluteName)
		{
			return this._concentrations.get(soluteName);
		}
		
		@Override
		public Boundary makePartnerBoundary()
		{
			ChemostatOutflow cOut = new ChemostatOutflow();
			cOut.setFlowRate(this._flowRate);
			this._partner = cOut;
			return cOut;
		}
	}
}

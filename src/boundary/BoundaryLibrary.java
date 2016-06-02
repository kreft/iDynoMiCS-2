/**
 * 
 */
package boundary;

import java.util.HashMap;

import boundary.agent.AgentMethodLibrary.*;
import boundary.grid.GridMethodLibrary.*;

import dataIO.XmlLabel;
import idynomics.Compartment;

/**
 * \brief Collection of commonly used {@code Boundary} classes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 */
public final class BoundaryLibrary
{

	public final static String PARTNER = XmlLabel.boundaryPartner;
	
	/**
	 * TODO
	 * @author Robert
	 *
	 */
	public static class CustomBoundary extends Boundary
	{
		/**
		 * TODO
		 */
		public CustomBoundary()
		{
			
		}
		
		//FIXME to be replaced by ModelNode Paradigm?
//		@Override
//		public List<InputSetter> getRequiredInputs()
//		{
//			List<InputSetter> out = new LinkedList<InputSetter>();
//			// TODO GridMethod, AgentMethod
//			out.add(new GridMethodMaker(
//							Boundary.DEFAULT_GM, Requirement.ZERO_OR_ONE, this));
//			out.add(new GridMethodMaker(
//							"Grid Method", Requirement.ZERO_TO_MANY, this));
//			//out.add(new AgentMethodMaker(Requirement.EXACTLY_ONE));
//			return out;
//		}
//		
//		public void acceptInput(String name, Object input)
//		{
//			if ( input instanceof GridMethod )
//			{
//				GridMethod gm = (GridMethod) input;
//				// TODO Need to be very careful about how non-default methods
//				// are assigned!
//				if ( name.equals(Boundary.DEFAULT_GM) )
//					this._defaultGridMethod = gm;
//				else
//					this._gridMethods.put(name, gm);
//			}
//			if ( input instanceof AgentMethod )
//				this._agentMethod = (AgentMethod) input;
//		}
		
		/**
		 * TODO
		 */
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
	
	/**
	 * TODO
	 * @author Robert
	 *
	 */
	public static class SolidBoundary extends Boundary
	{
		/**
		 * TODO
		 */
		public SolidBoundary()
		{
			this._defaultGridMethod = new ZeroFlux();
			this._agentMethod = new SolidSurface();
		}
		
		/**
		 * TODO
		 */
		@Override
		public Boundary makePartnerBoundary()
		{
			return null;
		}
	}
	
	/**
	 * TODO
	 * @author Robert
	 *
	 */
	public static class BulkBLBoundary extends Boundary
	{
		/**
		 * TODO
		 */
		public BulkBLBoundary()
		{
			//this._defaultGridMethod = new ZeroFlux();
			this._agentMethod = new BoundaryLayer();
		}
		
		/**
		 * TODO
		 */
		@Override
		public Boundary makePartnerBoundary()
		{
			// TODO 
			return null;
		}
	}
	
	/**
	 * TODO
	 * @author Robert
	 *
	 */
	public static class FixedBoundary extends Boundary
	{
		/**
		 * TODO
		 */
		public FixedBoundary()
		{
			this._defaultGridMethod = new ConstantDirichlet();
			// TODO change agent method?
			this._agentMethod = new SolidSurface();
		}
		
		/**
		 * TODO
		 * @param value
		 */
		public FixedBoundary(double value)
		{
			this();
			this.setValue(value);
		}
		
		/**
		 * TODO
		 * @param value
		 */
		public void setValue(double value)
		{
			((ConstantDirichlet) this._defaultGridMethod).setValue(value);
		}
		
		/**
		 * TODO
		 * @param soluteName
		 * @param value
		 */
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
		
		/**
		 * TODO
		 */
		@Override
		public Boundary makePartnerBoundary()
		{
			return null;
		}
		
	}
	
	/**************************************************************************
	 * CHEMOSTAT BOUNDARIES
	 *************************************************************************/
	
	/**
	 * TODO
	 * @author Robert
	 *
	 */
	public static class ChemostatOutflow extends Boundary
	{
		/**
		 * TODO
		 */
		protected double _flowRate;
		
		/**
		 * TODO
		 */
		protected double _agentsToDiluteTally;
		
		/**
		 * TODO
		 * @param flowRate
		 */
		public void setFlowRate(double flowRate)
		{
			this._flowRate = flowRate;
		}
		
		/**
		 * TODO
		 * @param comp
		 */
		public void setPartnerCompartment(Compartment comp)
		{
			ChemostatInflow cIn = new ChemostatInflow();
			cIn.setFlowRate(this._flowRate);
			comp.getShape().addOtherBoundary(cIn);
			this.setPartner(cIn);
		}
		
		/**
		 * TODO
		 */
		@Override
		public Boundary makePartnerBoundary()
		{
			ChemostatInflow cIn = new ChemostatInflow();
			cIn.setFlowRate(this._flowRate);
			this._partner = cIn;
			return cIn;
		}
		
		/**
		 * TODO
		 * @return
		 */
		public double getFlowRate()
		{
			return this._flowRate;
		}
		
		/**
		 * TODO
		 * @return
		 */
		public double getAgentsToDiluteTally()
		{
			return this._agentsToDiluteTally;
		}
		
		/**
		 * TODO
		 * @param timeStep
		 */
		public void updateAgentsToDiluteTally(double timeStep)
		{
			/* Remember to subtract, since flow rate out is negative. */
			this._agentsToDiluteTally -= this._flowRate * timeStep;
		}
		
		/**
		 * TODO
		 */
		public void knockDownAgentsToDiluteTally()
		{
			this._agentsToDiluteTally--;
		}
		
		/**
		 * TODO
		 */
		public void resetAgentsToDiluteTally()
		{
			this._agentsToDiluteTally = 0.0;
		}
	}
	
	/**
	 * TODO
	 * @author Robert
	 *
	 */
	public static class ChemostatInflow extends Boundary
	{
		/**
		 * TODO
		 */
		protected double _flowRate;
		
		/**
		 * TODO
		 */
		protected HashMap<String, Double> _concentrations;
			
		//FIXME to be replaced by ModelNode Paradigm?
//		@Override
//		public List<InputSetter> getRequiredInputs()
//		{
//			List<InputSetter> out = new LinkedList<InputSetter>();
//			out.add(new ParameterSetter("flowRate", this, ObjectRef.DBL));
//			// TODO concentrations? AspectRegistry?
//			return out;
//		}
//		
//		@Override
//		public void acceptInput(String name, Object input)
//		{
//			if (name.equals("flowRate") && input instanceof Double)
//				this._flowRate = (Double) input;
//		}
		
		/**
		 * TODO
		 * @param flowRate
		 */
		public void setFlowRate(double flowRate)
		{
			this._flowRate = flowRate;
		}
		
		/**
		 * TODO
		 * @return
		 */
		public double getFlowRate()
		{
			return this._flowRate;
		}
		
		/**
		 * TODO
		 * @param soluteName
		 * @return
		 */
		public double getConcentration(String soluteName)
		{
			return this._concentrations.get(soluteName);
		}
		
		/**
		 * TODO
		 */
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

package idynomics;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import generalInterfaces.XMLable;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.ParameterSetter;
import modelBuilder.SubmodelMaker;
import dataIO.Log.Tier;
import utility.Helper;

public class Timer implements IsSubmodel, XMLable
{
	private int iteration;
	
	private double now;
	
	protected double timerStepSize;
	
	protected double endOfSimulation;
	
	public Timer()
	{
		this.iteration = 0;
		this.now = 0.0;
	}
	
	public String getName()
	{
		return "Timer";
	}
	
	public void init(Element xmlNode)
	{
		Log.out(Tier.NORMAL, "Timer loading...");
		String s;
		double d;
		/* Get the time step. */
		s = XmlHandler.gatherAttribute(xmlNode, XmlLabel.timerStepSize);
		s = Helper.obtainInput(s, "Timer time step size");
		d = Double.valueOf(s);
		// TODO safety
		setTimeStepSize(d);
		/* Get the total time span. */
		s = XmlHandler.gatherAttribute(xmlNode, XmlLabel.endOfSimulation);
		s = Helper.obtainInput(s, "End of simulation");
		d = Double.valueOf(s);
		// TODO safety
		setEndOfSimulation(d);
		report(Tier.NORMAL);
		Log.out(Tier.NORMAL, "Timer loaded!\n");
		
		if ( Helper.gui )
			GuiLaunch.resetProgressBar();
	}

	/**
	 * 
	 * 
	 */
	public String getXml() {
		String out = "<" + XmlLabel.timer + " " + XmlLabel.timerStepSize + 
				" =\"" + this.timerStepSize + "\" " + XmlLabel.endOfSimulation +
				"=\"" + this.endOfSimulation + "\" />\n";
		return out;
	}
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/

	
	public void reset()
	{
		now = 0.0;
		iteration = 0;
	}
	
	public void setTimeStepSize(double stepSize)
	{
		this.timerStepSize = stepSize;
	}
	
	public double getCurrentTime()
	{
		return now;
	}
	
	public int getCurrentIteration()
	{
		return iteration;
	}
	
	public double getTimeStepSize()
	{
		return this.timerStepSize;
	}
	
	public double getEndOfCurrentIteration()
	{
		return now + getTimeStepSize();
	}
	
	public void step()
	{
		now += getTimeStepSize();
		iteration++;
		if ( Helper.gui )
			GuiLaunch.updateProgressBar();
	}
	
	public double getEndOfSimulation()
	{
		return this.endOfSimulation;
	}
	
	public void setEndOfSimulation(double timeToStopAt)
	{
		this.endOfSimulation = timeToStopAt;
	}
	
	public int estimateLastIteration()
	{
		return (int) (getEndOfSimulation() / getTimeStepSize());
	}
	
	public boolean isRunning()
	{
		Log.out(Tier.DEBUG, "Timer.isRunning()? now = "+now+", end = "+
				getEndOfSimulation()+", so "+(now<getEndOfSimulation())); 
		return now < getEndOfSimulation();
	}
	
	public void report(Tier outputLevel)
	{
		Log.out(outputLevel, "Timer: time is   = "+now);
		Log.out(outputLevel, "       iteration = "+iteration);
		Log.out(outputLevel, "       step size = "+getTimeStepSize());
		Log.out(outputLevel, "       end time  = "+getEndOfSimulation());
	}
	
	/*************************************************************************
	 * SUBMODEL BUILDING
	 ************************************************************************/
	
	public List<InputSetter> getRequiredInputs()
	{
		List<InputSetter> out = new LinkedList<InputSetter>();
		out.add(new ParameterSetter(XmlLabel.timerStepSize, this, "Double"));
		out.add(new ParameterSetter(XmlLabel.endOfSimulation, this, "Double"));
		return out;
	}
	
	public void acceptInput(String name, Object input)
	{
		if ( input instanceof Double )
		{
			Double dbl = (Double) input;
			if ( name.equals(XmlLabel.timerStepSize) )
				this.setTimeStepSize(dbl);
			if ( name.equals(XmlLabel.endOfSimulation) )
				this.setEndOfSimulation(dbl);
		}
	}
	
	public static class TimerMaker extends SubmodelMaker
	{
		private static final long serialVersionUID = 1486068039985317593L;
		
		public TimerMaker(Requirement req, IsSubmodel target)
		{
			super(XmlLabel.timer, req, target);
			
		}
		
		@Override
		public void doAction(ActionEvent e)
		{
			this.addSubmodel(new Timer());
		}
	}
}

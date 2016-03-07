package idynomics;

import org.w3c.dom.Element;

import aspect.Aspect;
import aspect.AspectInterface;
import aspect.AspectReg;
import aspect.AspectRestrictionsLibrary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import generalInterfaces.XMLable;
import dataIO.Log.Tier;
import utility.Helper;

public class Timer implements AspectInterface, XMLable
{
	private int iteration;
	
	private double now;
	
	private AspectReg<Object> aspectRegistry;
	
	public Timer()
	{
		this.iteration = 0;
		this.now = 0.0;
		this.aspectRegistry = defaultValues();
	}
	
	private static AspectReg<Object> defaultValues()
	{
		AspectReg<Object> out = new AspectReg<Object>();
		/* The time step size is required. */
		Double tStep = null;
//		tStep.description = "Timer time step size";
//		tStep.setRestriction(AspectRestrictionsLibrary.positiveDbl());
		out.add(XmlLabel.timerStepSize, tStep);
		/* The simulation end time is required. */
		Double endT = null;
//		endT.description = "End of simulation";
		out.add(XmlLabel.endOfSimulation, endT);
		/* Return the default values. */
		return out;
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
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/
	
	@Override
	public AspectReg<?> reg()
	{
		return aspectRegistry;
	}
	
	public void reset()
	{
		now = 0.0;
		iteration = 0;
	}
	
	public void setTimeStepSize(double stepSize)
	{
		aspectRegistry.set(XmlLabel.timerStepSize, stepSize);
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
		return (double) aspectRegistry.getValue(this, XmlLabel.timerStepSize);
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
		return (double) aspectRegistry.getValue(this, XmlLabel.endOfSimulation);
	}
	
	public void setEndOfSimulation(double timeToStopAt)
	{
		aspectRegistry.set(XmlLabel.endOfSimulation, timeToStopAt);
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
	
	
}

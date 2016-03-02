package idynomics;

import org.w3c.dom.Node;

import aspect.Aspect;
import aspect.AspectInterface;
import aspect.AspectReg;
import aspect.AspectRestrictionsLibrary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.Tier;
import utility.Helper;

public class Timer implements AspectInterface
{
	private static int iteration;
	
	private static double now;
	
	private static AspectReg<Object> aspectRegistry = defaultValues();
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/
	
	@Override
	public AspectReg<?> reg()
	{
		return aspectRegistry;
	}
	
	public static AspectReg<Object> defaultValues()
	{
		AspectReg<Object> out = new AspectReg<Object>();
		/* The time step size is required. */
		Aspect<Double> tStep = new Aspect<Double>(null);
		tStep.description = "Timer time step size";
		tStep.setRestriction(AspectRestrictionsLibrary.positiveDbl());
		out.add(XmlLabel.timerStepSize, tStep);
		/* The simulation end time is required. */
		Aspect<Double> endT = new Aspect<Double>(null);
		endT.description = "End of simulation";
		out.add(XmlLabel.endOfSimulation, endT);
		/* Return the default values. */
		return out;
	}
	
	public static void init(Node xmlNode)
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
	
	public static void reset()
	{
		now = 0.0;
		iteration = 0;
	}
	
	public static void setTimeStepSize(double stepSize)
	{
		aspectRegistry.set(XmlLabel.timerStepSize, stepSize);
	}
	
	public static double getCurrentTime()
	{
		return now;
	}
	
	public static int getCurrentIteration()
	{
		return iteration;
	}
	
	public static double getTimeStepSize()
	{
		return (double) aspectRegistry.getPrimaryValue(XmlLabel.timerStepSize);
	}
	
	public static double getEndOfCurrentIteration()
	{
		return now + getTimeStepSize();
	}
	
	public static void step()
	{
		now += getTimeStepSize();
		iteration++;
		if ( Helper.gui )
			GuiLaunch.updateProgressBar();
	}
	
	public static double getEndOfSimulation()
	{
		return (double) aspectRegistry.getPrimaryValue(XmlLabel.endOfSimulation);
	}
	
	public static void setEndOfSimulation(double timeToStopAt)
	{
		aspectRegistry.set(XmlLabel.endOfSimulation, timeToStopAt);
	}
	
	public static int estimateLastIteration()
	{
		return (int) (getEndOfSimulation() / getTimeStepSize());
	}
	
	public static boolean isRunning()
	{
		Log.out(Tier.DEBUG, "Timer.isRunning()? now = "+now+", end = "+
				getEndOfSimulation()+", so "+(now<getEndOfSimulation())); 
		return now < getEndOfSimulation();
	}
	
	public static void report(Tier outputLevel)
	{
		Log.out(outputLevel, "Timer: time is   = "+now);
		Log.out(outputLevel, "       iteration = "+iteration);
		Log.out(outputLevel, "       step size = "+getTimeStepSize());
		Log.out(outputLevel, "       end time  = "+getEndOfSimulation());
	}
	
	
}

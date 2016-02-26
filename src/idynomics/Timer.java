package idynomics;

import org.w3c.dom.Node;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import dataIO.Log.tier;
import utility.Helper;

public class Timer
{
	private static int iteration;
	
	private static double now;
	
	private static double endOfSimulation;
	
	private static double timeStepSize;
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/
	
	public static void init(Node xmlNode)
	{
		Log.out(tier.NORMAL, "Timer loading...");
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
		report(tier.NORMAL);
		Log.out(tier.NORMAL, "Timer loaded!\n");
	}
	
	public static void reset()
	{
		now = 0.0;
		iteration = 0;
	}
	
	public static void setTimeStepSize(double stepSize)
	{
		timeStepSize = stepSize;
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
		return timeStepSize;
	}
	
	public static double getEndOfCurrentIteration()
	{
		return now + timeStepSize;
	}
	
	public static void step()
	{
		now += timeStepSize;
		iteration++;
	}
	
	public static void setEndOfSimulation(double timeToStopAt)
	{
		endOfSimulation = timeToStopAt;
	}
	
	public static boolean isRunning()
	{
		Log.out(tier.DEBUG, "Timer.isRunning()? now = "+now+", end = "+
								endOfSimulation+", so "+(now<endOfSimulation)); 
		return now < endOfSimulation;
	}
	
	public static void report(tier outputLevel)
	{
		Log.out(outputLevel, "Timer: time is   = "+now);
		Log.out(outputLevel, "       iteration = "+iteration);
		Log.out(outputLevel, "       step size = "+timeStepSize);
		Log.out(outputLevel, "       end time  = "+endOfSimulation);
	}
}

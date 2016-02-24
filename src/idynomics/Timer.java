package idynomics;

import org.w3c.dom.Node;

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
		String s;
		double d;
		/* Get the time step. */
		s = Helper.obtainInput(Param.timeStepSize, "Timer time step size");
		d = Double.valueOf(s);
		// TODO safety
		setTimeStepSize(d);
		/* Get the total time span. */
		s = Helper.obtainInput(Param.timeStepSize, "Timer time step size");
		d = Double.valueOf(s);
		// TODO safety
		setTimeStepSize(d);
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
		return now < endOfSimulation;
	}
}

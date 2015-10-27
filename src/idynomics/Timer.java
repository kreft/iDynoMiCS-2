package idynomics;

public class Timer
{
	protected static int _iteration;
	
	protected static double _now;
	
	protected static double _endOfSimulation;
	
	protected static double _timeStepSize;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Timer()
	{
		
	}
	
	public void init()
	{
		
	}
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/
	
	public static void setTimeStepSize(double timeStepSize)
	{
		_timeStepSize = timeStepSize;
	}
	
	public static double getCurrentTime()
	{
		return _now;
	}
	
	public static double getTimeStepSize()
	{
		return _timeStepSize;
	}
	
	public static double getEndOfCurrentIteration()
	{
		return _now + _timeStepSize;
	}
	
	
}

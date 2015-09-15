package idynomics;

public class Timer
{
	protected static int _iteration;
	
	protected static Double _now;
	
	protected static Double _endOfSimulation;
	
	protected static Double _timeStepSize;
	
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
	
	public static Double getCurrentTime()
	{
		return _now;
	}
	
	public static Double getTimeStepSize()
	{
		return _timeStepSize;
	}
	
	public static Double getEndOfCurrentIteration()
	{
		return _now + _timeStepSize;
	}
	
	
}

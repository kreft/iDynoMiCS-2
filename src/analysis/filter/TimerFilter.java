package analysis.filter;

import aspect.AspectInterface;
import idynomics.Idynomics;

/**
 * Output current simulation time in the data csv file.
 * 
 * Spcified by '#' in the filter logic.
 * 
 * @author Sankalp Arya (stxsa33@nottingham.ac.uk), UoN, Nottingham.
 *
 */
public class TimerFilter implements Filter {
	
	private String header;

	public TimerFilter(String timeString) {
		this.header = timeString;
	}
	
	@Override
	public String stringValue(AspectInterface subject)
	{
		return String.valueOf(Idynomics.simulator.timer.getCurrentTime());
	}

	@Override
	public String header() 
	{
		return this.header;
	}

	@Override
	public boolean match(AspectInterface subject) 
	{
		return false;
	}

}

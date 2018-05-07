package idynomics;

import java.math.BigDecimal;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import gui.GuiButtons;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import dataIO.Log.Tier;
import instantiable.Instantiable;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class Timer implements Instantiable, Settable
{
	/**
	 * TODO
	 */
	private int _iteration;
	
	/**
	 * TODO
	 */
	private double _now;
	
	/**
	 * TODO
	 */
	protected double _timerStepSize;
	
	/**
	 * TODO
	 */
	protected double _endOfSimulation;

	private Settable _parentNode;
		
	public Timer()
	{
		this._iteration = 0;
		this._now = 0.0;
	}
		
	public void instantiate(Element xmlNode, Settable parent)
	{
		Log.out(Tier.NORMAL, "Timer loading...");

		/* Get starting time step */
		this.setCurrentTime( Double.valueOf( Helper.setIfNone( 
				XmlHandler.gatherAttribute(
				xmlNode, XmlRef.currentTime ), "0.0" ) ) );
		
		this.setCurrentIteration( Integer.valueOf( Helper.setIfNone( 
				XmlHandler.gatherAttribute(
				xmlNode, XmlRef.currentIter ), "0" ) ) );
		
		/* Get the time step. */
		this.setTimeStepSize( Double.valueOf( XmlHandler.obtainAttribute(
				xmlNode, XmlRef.timerStepSize, this.defaultXmlTag() ) ) );

		/* Get the total time span. */
		this.setEndOfSimulation( Double.valueOf( XmlHandler.obtainAttribute(
				xmlNode, XmlRef.endOfSimulation, this.defaultXmlTag() ) ) );

		
		this.report(Tier.NORMAL);
		Log.out(Tier.NORMAL, "Timer loaded!\n");
	}
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/

	
	public void reset()
	{
		this._now = 0.0;
		this._iteration = 0;
	}
	
	public void setTimeStepSize(double stepSize)
	{
		this._timerStepSize = stepSize;
	}
	
	public void setCurrentTime(double time)
	{
		this._now = time;
	}
	
	public double getCurrentTime()
	{
		return this._now;
	}
	
	private void setCurrentIteration(int iteration) 
	{
		this._iteration = iteration;
	}
	
	public int getCurrentIteration()
	{
		return this._iteration;
	}
	
	public double getTimeStepSize()
	{
		return this._timerStepSize;
	}
	
	public double getEndOfCurrentIteration()
	{
		return ( BigDecimal.valueOf( this._now ) ).
				add( BigDecimal.valueOf( this._timerStepSize ) ).doubleValue();
	}
	
	public void step()
	{
		this._now = getEndOfCurrentIteration();
		this._iteration++;
		if ( Helper.isSystemRunningInGUI )
			GuiButtons.updateProgressBar();
	}
	
	public double getEndOfSimulation()
	{
		return this._endOfSimulation;
	}
	
	public void setEndOfSimulation(double timeToStopAt)
	{
		this._endOfSimulation = timeToStopAt;
	}
	
	public int estimateIterationsRemaining()
	{
		double timeLeft = this.getEndOfSimulation() - this.getCurrentTime();
		return (int) (timeLeft / this.getTimeStepSize());
	}
	
	public boolean isRunning()
	{
		Log.out(Tier.DEBUG, "Timer.isRunning()? now = "+this._now+
				", end = "+this.getEndOfSimulation()+
				", so "+(this._now<getEndOfSimulation())); 
		return this._now < this.getEndOfSimulation();
	}
	
	public void report(Tier outputLevel)
	{
		Log.out(outputLevel, "Timer: time is   = "+_now);
		Log.out(outputLevel, "       iteration = "+getCurrentIteration());
		Log.out(outputLevel, "       step size = "+getTimeStepSize());
		Log.out(outputLevel, "       end time  = "+getEndOfSimulation());
	}
	
	/*************************************************************************
	 * model node
	 ************************************************************************/

	/**
	 * Get the ModelNode object for this Timer object
	 * @return ModelNode
	 */
	public Module getModule()
	{
		/* the timer node */
		Module modelNode = new Module(XmlRef.timer, this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		
		/* now */
		modelNode.add(new Attribute(XmlRef.currentTime, 
				String.valueOf(this._now), null, true ));

		/* time step size */
		modelNode.add(new Attribute(XmlRef.timerStepSize, 
				String.valueOf(this._timerStepSize), null, true ));
		
		/* end of simulation */
		modelNode.add(new Attribute(XmlRef.endOfSimulation, 
				String.valueOf(this._endOfSimulation), null, true ));
		
		return modelNode;
	}

	/**
	 * Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object
	 * @param node
	 */
	public void setModule(Module node)
	{
		this.setCurrentTime( Double.valueOf( 
				node.getAttribute( XmlRef.currentTime ).getValue() ));
		
		/* time step size */
		this.setTimeStepSize( Double.valueOf( 
				node.getAttribute( XmlRef.timerStepSize ).getValue() ));
		
		/* end of simulation */
		this.setEndOfSimulation( Double.valueOf( 
				node.getAttribute( XmlRef.endOfSimulation ).getValue() ));
	}

	/**
	 * return the default XMLtag for the XML node of this object
	 * @return String xmlTag
	 */
	@Override
	public String defaultXmlTag() {
		return XmlRef.timer;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}
}

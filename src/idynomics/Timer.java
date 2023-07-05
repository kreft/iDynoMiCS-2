package idynomics;

import java.math.BigDecimal;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import gui.GuiButtons;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
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
		this._iteration = 1;
		this._now = 0.0;
	}
		
	public void instantiate(Element xmlElem, Settable parent)
	{
		/* Get starting time step */
		this.setCurrentTime( Helper.setIfNone( XmlHandler.gatherDouble(
				xmlElem, XmlRef.currentTime ), 0.0 ) );
		
		this.setCurrentIteration( Integer.valueOf( Helper.setIfNone( 
				XmlHandler.gatherAttribute(
						xmlElem, XmlRef.currentIter ), "1" ) ) );
		
		/* Get the time step. */
		this.setTimeStepSize( XmlHandler.obtainDouble (
				xmlElem, XmlRef.timerStepSize, this.defaultXmlTag() ) );

		/* Get the total time span. */
		this.setEndOfSimulation( XmlHandler.obtainDouble (
				xmlElem, XmlRef.endOfSimulation, this.defaultXmlTag() ) );
		
		if ( XmlHandler.hasAttribute(xmlElem, XmlRef.outputskip) )
			Idynomics.global.outputskip = Integer.valueOf( 
					XmlHandler.obtainAttribute( xmlElem, XmlRef.outputskip, 
					XmlRef.simulation));
		
		if ( XmlHandler.hasAttribute(xmlElem, XmlRef.outputTime) )
			Idynomics.global.outputskip = (int) Math.round( (Double.valueOf(
					XmlHandler.obtainAttribute( xmlElem, XmlRef.outputTime, 
					XmlRef.simulation)) / this.getTimeStepSize()));
	}
	
	/*************************************************************************
	 * BASIC METHODS
	 ************************************************************************/

	
	public void reset()
	{
		this._now = 0.0;
		this._iteration = 1;
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
		if( Log.shouldWrite(Tier.DEBUG) )
			Log.out(Tier.DEBUG, "Timer.isRunning()? now = "+ this._now+
					", end = "+ this.getEndOfSimulation()+
					", so "+ (this._now<getEndOfSimulation())); 
		return this._now < this.getEndOfSimulation();
	}
	
	public void report(Tier outputLevel)
	{
		if( Log.shouldWrite(outputLevel))
			Log.out(outputLevel, "#"+ getCurrentIteration()+ " time: "+ _now+ 
					" step: "+ getTimeStepSize()+ " end: "+ getEndOfSimulation());
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

package idynomics;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XmlRef;
import generalInterfaces.Instantiatable;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;
import dataIO.Log.Tier;
import utility.Helper;

/**
 * \brief TODO
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Timer implements Instantiatable, NodeConstructor
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
		
	public Timer()
	{
		this._iteration = 0;
		this._now = 0.0;
	}
	
	public String getName()
	{
		return XmlRef.timer;
	}
	
	public void init(Element xmlNode)
	{
		Log.out(Tier.NORMAL, "Timer loading...");
		String s;
		double d;
		/* Get the time step. */
		s = XmlHandler.gatherAttribute(xmlNode, XmlRef.timerStepSize);
		s = Helper.obtainInput(s, "Timer time step size");
		d = Double.valueOf(s);
		// TODO safety
		setTimeStepSize(d);
		/* Get the total time span. */
		s = XmlHandler.gatherAttribute(xmlNode, XmlRef.endOfSimulation);
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

	
	public void reset()
	{
		_now = 0.0;
		_iteration = 0;
	}
	
	public void setTimeStepSize(double stepSize)
	{
		this._timerStepSize = stepSize;
	}
	
	public double getCurrentTime()
	{
		return _now;
	}
	
	public int getCurrentIteration()
	{
		return _iteration;
	}
	
	public double getTimeStepSize()
	{
		return this._timerStepSize;
	}
	
	public double getEndOfCurrentIteration()
	{
		return _now + getTimeStepSize();
	}
	
	public void step()
	{
		_now += getTimeStepSize();
		_iteration++;
		if ( Helper.gui )
			GuiLaunch.updateProgressBar();
	}
	
	public double getEndOfSimulation()
	{
		return this._endOfSimulation;
	}
	
	public void setEndOfSimulation(double timeToStopAt)
	{
		this._endOfSimulation = timeToStopAt;
	}
	
	public int estimateLastIteration()
	{
		return (int) (getEndOfSimulation() / getTimeStepSize());
	}
	
	public boolean isRunning()
	{
		Log.out(Tier.DEBUG, "Timer.isRunning()? now = "+_now+", end = "+
				getEndOfSimulation()+", so "+(_now<getEndOfSimulation())); 
		return _now < getEndOfSimulation();
	}
	
	public void report(Tier outputLevel)
	{
		Log.out(outputLevel, "Timer: time is   = "+_now);
		Log.out(outputLevel, "       iteration = "+_iteration);
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
	public ModelNode getNode() {

		/* the timer node */
		ModelNode modelNode = new ModelNode(XmlRef.timer, this);
		modelNode.requirement = Requirements.EXACTLY_ONE;
		
		/* time step size */
		modelNode.add(new ModelAttribute(XmlRef.timerStepSize, 
				String.valueOf(this._timerStepSize), null, true ));
		
		/* end of simulation */
		modelNode.add(new ModelAttribute(XmlRef.endOfSimulation, 
				String.valueOf(this._endOfSimulation), null, true ));
		
		return modelNode;
	}

	/**
	 * Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object
	 * @param node
	 */
	public void setNode(ModelNode node)
	{
		/* time step size */
		this.setTimeStepSize( Double.valueOf( 
				node.getAttribute( XmlRef.timerStepSize ).value ));
		
		/* end of simulation */
		this.setEndOfSimulation( Double.valueOf( 
				node.getAttribute( XmlRef.endOfSimulation ).value ));
	}
	
	/**
	 * Create a new minimal object of this class and return it
	 * @return NodeConstructor
	 */
	public NodeConstructor newBlank()
	{
		return new Timer();
	}

	/**
	 * return the default XMLtag for the XML node of this object
	 * @return String xmlTag
	 */
	@Override
	public String defaultXmlTag() {
		return XmlRef.timer;
	}
}

package agent;

import agent.AspectReg.AspectType;
import agent.event.Event;
import agent.state.Calculated;

/**
 * 
 * @author baco
 * @param <A>
 *
 */
public class Aspect<A>
{
	final A aspect;
	final AspectType type;
	
	/**
	 * Testing direct access fields (to prevent excessive casting).
	 * Worth skimming of some milliseconds here ;)
	 */
	final Calculated calc;
	final Event event;
	
	/**
	 * Sets the aspect and declares type
	 * @param <A>
	 * @param aspect
	 */
    public Aspect(A aspect)
    {
    	this.aspect = aspect;
		if(this.aspect instanceof Calculated)
		{
			  this.type = AspectType.CALCULATED;
			  this.calc = (Calculated) aspect;
			  this.event = null;
		}
		else if(aspect instanceof Event)
		{
			  this.type = AspectType.EVENT;
			  this.event = (Event) aspect;
			  this.calc = null;
		}
		else
		{
			  this.type = AspectType.PRIMARY;
			  this.event = null;
			  this.calc = null;
		}
    }
} 
package aspect;

import expression.Component;

/**
 * 
 * @author baco
 * @param <A>
 *
 */
public class Aspect<A>
{
	/**
	 * The recognized aspect types
	 * @author baco
	 *
	 */
	public enum aspectClass
	{
		PRIMARY,
		CALCULATED,
		EVENT
	}

	final A aspect;
	final aspectClass type;
	
	/**
	 * Testing/experimenting direct access fields (to prevent excessive casting).
	 * Worth skimming of some milliseconds here ;)
	 */
	protected Calculated calc;
	protected Event event;
	
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
			  this.type = Aspect.aspectClass.CALCULATED;
			  this.calc = (Calculated) aspect;
		}
		else if(aspect instanceof Event)
		{
			  this.type = Aspect.aspectClass.EVENT;
			  this.event = (Event) aspect;
		}
		else
		{
			  this.type = Aspect.aspectClass.PRIMARY;
		}
    }
} 
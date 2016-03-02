package aspect;

/**
 * \brief Very general class that acts as a wrapper for other Objects.
 * 
 * @param <A> Class of the Aspect.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Aspect<A>
{
	/**
	 * \brief Recognised aspect types.
	 * 
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
	 */
	public enum AspectClass
	{
		/**
		 * Neither an instance of {@code aspect.Calculated}, nor of
		 * {@code aspect.Event}.
		 */
		PRIMARY,
		/**
		 * An instance of {@code aspect.Calculated}.
		 */
		CALCULATED,
		/**
		 * An instance of {@code aspect.Event}.
		 */
		EVENT
	}
	
	/**
	 * The object this Aspect wraps.
	 */
	protected A aspect;
	
	/**
	 * The type of object this Aspect wraps.
	 */
	final AspectClass type;
	
	/**
	 * Direct access field for a {@code Calculated} aspect (to prevent
	 * excessive casting).
	 */
	protected Calculated calc;
	
	/**
	 * Direct access field for an {@code Even} aspect (to prevent excessive
	 * casting).
	 */
	protected Event event;
	
	/**
	 * \brief Construct and Aspect by setting the aspect and declares type
	 * 
	 * @param <A>
	 * @param aspect
	 */
    public Aspect(A aspect)
    {
    	this.aspect = aspect;
		if ( this.aspect instanceof Calculated )
		{
			  this.type = AspectClass.CALCULATED;
			  this.calc = (Calculated) this.aspect;
		}
		else if ( this.aspect instanceof Event )
		{
			  this.type = AspectClass.EVENT;
			  this.event = (Event) this.aspect;
		}
		else
		{
			  this.type = AspectClass.PRIMARY;
		}
    }
    
    protected void updateAspect(A newAspect)
    {
    	if ( newAspect.getClass() != this.aspect.getClass() )
    	{
    		// TODO safety
    	}
    	this.aspect = newAspect;
    	/* Update the direct access fields, if appropriate. */
    	if ( this.type == AspectClass.CALCULATED )
    		this.calc = (Calculated) this.aspect;
    	if ( this.type == AspectClass.EVENT )
    		this.event = (Event) this.aspect;
    }
} 
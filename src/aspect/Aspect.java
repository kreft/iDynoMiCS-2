package aspect;

import java.util.function.Predicate;

import dataIO.Log;
import dataIO.Log.Tier;

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
	 * TODO
	 */
	public String description;
	
	private Predicate<A> _restriction;
	
	private String _restrictionExplanation;
	
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
    
    /**
     * \brief TODO
     * 
     * @param newAspect
     */
    protected void updateAspect(A newAspect)
    {
    	if ( newAspect.getClass() != this.aspect.getClass() )
    	{
    		// TODO safety
    	}
    	/* Check that the restriction is satisfied, if there is one. */
    	if ( this.isRestrictionBroken() )
    	{
    		if ( this._restrictionExplanation == null )
    			Log.out(Tier.CRITICAL, "Aspect restriction broken!");
    		else
    			Log.out(Tier.CRITICAL, this._restrictionExplanation);
    	}
    	this.aspect = newAspect;
    	/* Update the direct access fields, if appropriate. */
    	if ( this.type == AspectClass.CALCULATED )
    		this.calc = (Calculated) this.aspect;
    	if ( this.type == AspectClass.EVENT )
    		this.event = (Event) this.aspect;
    }
    
    public A value()
    {
    	return this.aspect;
    }
    
    public void setRestriction(Predicate<A> restriction)
    {
    	this._restriction = restriction;
    }
    
    public void setRestiction(Predicate<A> restriction, String explanation)
    {
    	this.setRestriction(restriction);
    	this._restrictionExplanation = explanation;
    }
    
    public boolean isRestrictionBroken()
    {
    	if ( this._restriction == null )
    		return false;
    	return ( ! this._restriction.test(this.aspect) );
    }
    
    public String getRestrictionExplanation()
    {
    	return this._restrictionExplanation;
    }
} 
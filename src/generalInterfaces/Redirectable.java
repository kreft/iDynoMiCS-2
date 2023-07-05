package generalInterfaces;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import referenceLibrary.XmlRef;

public interface Redirectable {
	
	/**
	 * FIXME we should still save the redirects somewhere since they should be
	 * stored in the output files to!
	 * 
	 * Redirect allows objects that use direct field assignments of aspects (for
	 * example using BODY = agent_body) to be redirected. Thus thereby it is
	 * possible to use an alternative aspect call for a specific object. This
	 * object would typically be an Event or Calculated aspect or a 
	 * ProcessManager
	 * 
	 * input formating is comma separated (,) assignment by equals sign (=): 
	 * FIELD_REFERENCE=redirection,SECOND_FIELD_REF=second_redirect etc.
	 * 
	 * @param redirections
	 */
	public default void redirect(String redirections)
	{
		/* Strip all whitespace. */
		redirections.replaceAll("\\s+","");
		/* Read in the input and split by field. */
		String[] array = redirections.split(",");
		String[] direction;
		/* assign the new field values to the object */
		for ( String s : array )
		{
			direction = s.split("=");
			setField(direction[0], direction[1]);
		}
	}
	
	/**
	 * 
	 */
	public default void redirect(Element xmlElem)
	{
		if (xmlElem != null && xmlElem.hasAttribute(XmlRef.fields))
			this.redirect(XmlHandler.obtainAttribute(xmlElem, XmlRef.fields, 
					"redirect fields"));
	}
	
	/**
	 * Set field "field" of this object to "value", assess existence and the 
	 * class of the field before assignment
	 * @param field
	 * @param value
	 */
	public default void setField(String field, String value)
	{
		try 
		{
			/* assess the field, on pass assign value */
			if (assessField(field))
				this.getClass().getField(field).set(this, value);
		} 
		catch (IllegalArgumentException | IllegalAccessException | 
				NoSuchFieldException | SecurityException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Assess field, return false if the field does not exist, is not accessible
	 * or if the field is not of String.class 
	 * @param field
	 * @return
	 */
	public default boolean assessField(String field)
	{
		try 
		{
			/* return true if the field is of String.class */
			return this.getClass().getField(field).getClass().equals(
					String.class );
		} 
		catch (NoSuchFieldException e) 
		{
			/* return false if the field does not exist */
			Log.out(Tier.DEBUG, "Field " +  field + " does not exist.");
			return false;
		} 
		catch (SecurityException e) 
		{
			/* return false if the field is not allowed to be accessed */
			Log.out(Tier.DEBUG, "Field " +  field + " is not accessible.");
			return false;
		}
	}
}

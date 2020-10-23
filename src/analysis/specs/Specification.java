package analysis.specs;

import aspect.AspectInterface;
import dataIO.Log;

public abstract class Specification {
	
	final String errormsg = "ERROR: Specification has no boolean outcome";
	
	public enum Clasification 
	{
		number,
		bool,
		factor,
		string;
	}
	
	public abstract String header();
	
	public abstract Object value(AspectInterface subject);
	
	public Boolean test(AspectInterface subject)
	{
		if( this instanceof EnvironmentSpecification )
		{
			Log.out( errormsg );
			return null;
		}
		
		Object test = this.value(subject);
		/* Boolean */
		if( test instanceof Boolean )
			return (Boolean) test;
		/* Double */
		else if( test instanceof Double )
		{
			if( (Double) test == 0.0 )
				return false;
			else if( (Double) test == 1.0 )
				return true;
		}
		/* String */
		else if( test instanceof String )
			return Boolean.valueOf((String) test);
		/* Unresolvable */
		Log.out( errormsg );
		return null;
	}
}

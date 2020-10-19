package analysis.filter;
import aspect.AspectInterface;
import expression.Expression;
import utility.Helper;

/**
 * The value filter obtains a single value in string format from an 
 * AspectInterface object, returns NONE if the aspect cannot be found.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class ValueFilter implements Filter
{

	private String header;
	private String property;
	
	public ValueFilter(String property) 
	{
		this.header = property;
		this.property = property;
	}

	@Override
	public String stringValue(AspectInterface subject, String format)
	{
		Object obj = subject.getValue(this.property);
		if( obj instanceof String )
			return (String) obj;
		else if ( obj instanceof Expression )
		{
			Expression ex = (Expression) obj;
			obj = ex.evaluate(subject);
		}
		return ( Helper.isNullOrEmpty(obj) ? "NONE" : String.format( screenLocale,  format, obj) );
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

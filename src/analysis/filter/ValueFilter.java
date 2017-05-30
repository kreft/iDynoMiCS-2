package analysis.filter;
import aspect.AspectInterface;
import utility.Helper;

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
	public String stringValue(AspectInterface subject) 
	{
		Object obj = subject.getValue(this.property);
		return ( Helper.isNullOrEmpty(obj) ? " " : String.valueOf(obj) );
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

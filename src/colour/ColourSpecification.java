package colour;

import java.util.HashMap;

import analysis.FilterLogic;
import analysis.filter.Filter;
import analysis.filter.ValueFilter;
import aspect.AspectInterface;
import compartment.Compartment;

public class ColourSpecification {
	
	public enum Type
	{
		BINAIRY,
		FACTOR;
	};
	
	private HashMap<String, Colour> factors = new HashMap<String, Colour>();
	
	private Palette palette;
	// Filter 
	String filter;

	HashMap<Filter,float[]> _gradients = new HashMap<Filter, float[]>();
	public ColourSpecification(Palette palette, String filter, HashMap<String,float[]> gradients, Compartment compartment)
	{
		this.palette = palette;
		this.filter = filter;
		int i = 0;
		if( gradients != null )
			for( String item : gradients.keySet() )
			{
				this._gradients.put(FilterLogic.filterFromString(item, compartment), gradients.get(item));
				i++;
				if(i > 1)
					return;
			}
	}

	/**
	 * Todo: implement gradients for agent mediator
	 * @param palette
	 * @param filter
	 */
	public ColourSpecification(Palette palette, String filter)
	{
		this.palette = palette;
		this.filter = filter;
	}

	public float[] colorize(AspectInterface subject)
	{
		/* placeholder code before merge */
		String factor = subject.getString( filter );
		if( !factors.containsKey( factor ) )
			this.factors.put( factor, palette.getNext() );
		if( this._gradients == null )
			return this.factors.get( factor ).returnColourRGB( new float[3] );
		else {
			//todo
			float[] values = new float[3];
			int i = 0;
			for( Filter f : this._gradients.keySet())
			{
				if( f instanceof ValueFilter) {
					String filterValue = f.stringValue(subject, "%e");
					// don't apply the gradient if the agent lacks the aspect
					if( filterValue == "NONE") {
						values[i] = 0;
					} else {
						float temp = (float) Float.valueOf(filterValue);
						float[] norm = this._gradients.get(f);
						if (norm[0] < norm[1]) {
							// normalize from 0 to 1
							temp = (temp - norm[0]) / (norm[1] - norm[0]);
						} else {
							// normalize from 1 to 0
							temp = (norm[0] - temp) / (norm[0] - norm[1]);
						}
						values[i] = temp;
					}
				}
				else
				{
					values[i] = 0;
				}
				i++;
			}
			return this.factors.get( factor ).returnColourRGB( values );
		}
	}
	
	public String toString()
	{
		return this.filter;
	}
}

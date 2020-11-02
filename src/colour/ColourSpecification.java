package colour;

import java.util.HashMap;

import aspect.AspectInterface;

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
	public ColourSpecification(Palette palette, String filter)
	{
		this.palette = palette;
		this.filter = filter;
	}

	public float[] colorize(AspectInterface subject)
	{
		/* placeholder code before merge */
		String factor = subject.getString(filter);
		if( !factors.containsKey(factor) )
			this.factors.put(factor, palette.getNext());
		return this.factors.get(factor).returnColour(new float[3]);
	}
}

package colour;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import linearAlgebra.Vector;

public class Palette {
	
	private HashMap<String, Colour> colours = new HashMap<String, Colour>();
	
	private LinkedList<String> unAssigned = new LinkedList<String>();
	
	public enum Property 
	{
		/* attributes */
		NAME("name", null, null),
		FORM("form", null, null),
		BASE("base", null, null),
		GRADIENT("gradient", null, null),
		
		HUE("hue", null, null),
		BRIGTHNESS("brightness", null, null),
		SATURATION("saturation", null, null),	
		
		OPACITY("opacity", null, null),
		
		/* Nodes */
		BASECOLOUR("baseColour", new Property[]{ BASE } , null ),
		SHIFT("shift", new Property[] {HUE, BRIGTHNESS, SATURATION },null ),
		COLOUR("colour", new Property[]{ GRADIENT, FORM, OPACITY, NAME },new Property[]{ BASECOLOUR, SHIFT } ),
		DOCUMENT("document", new Property[]{ NAME }, new Property[]{ COLOUR } );
		
	    public final String tag;
	    public final Property[] atributes;
	    public final Property[] children;
	    
		Property(String tag, Property[] atributes, Property[] children) {
	        this.tag = tag;
	        this.atributes = atributes;
	        this.children = children;
		}
	}

	public Palette(String paletteName)
	{
		String palettePath = "colourPalettes/" + paletteName;
		
		Element palette = XmlHandler.loadDocument(palettePath);
		Collection <Element> paletteColours = 
				XmlHandler.getElements(palette, Property.COLOUR.tag);
		int i = 0;
		for( Element c : paletteColours)
		{
			i++;
			String name = XmlHandler.gatherAttribute(c, Property.NAME.tag );
			if (name == null)
				name = String.valueOf(i);
			Colour myColour = new Colour( Vector.fltFromString( c.getAttribute( 
					Property.BASECOLOUR.tag ) ), 
					c.getAttribute( Property.FORM.tag ), 
					name );
			this.colours.put( name, myColour );
			this.unAssigned.add(name);
			Collection <Element> colourGradients = 
					XmlHandler.getElements(c, Property.SHIFT.tag);
			
			for( Element g : colourGradients )
			{
				myColour.addGradient(
						XmlHandler.gatherAttribute(g, Property.HUE.tag), 
						XmlHandler.gatherAttribute(g, Property.SATURATION.tag),
						XmlHandler.gatherAttribute(g, Property.BRIGTHNESS.tag));
			}
			
			if( c.hasAttribute( Property.OPACITY.tag ) )
			{
				myColour.setOpacity( Float.valueOf( 
						c.getAttribute( Property.OPACITY.tag ) ) );
			}
		}
		Log.out( "Loaded " + this.unAssigned.size() + " colours from palette.");
	}
	
	public Colour getNext()
	{
		String out = unAssigned.getFirst();
		unAssigned.remove(out);
		return colours.get( out );
	}
}

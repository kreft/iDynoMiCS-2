package colour;

import java.awt.Color;

import org.w3c.dom.Element;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.XmlHandler;

public class Colour implements AspectInterface {

	/**
	 * These colours are all in HSB coding
	 */
	private float[] initialColour;
	private float[] targetColour1;
	private float[] targetColour2;
	private float[] targetColour3;
	private float opacity = 1.0f;
	
	/** Dials: Numbers between 0 and 1 returned by the Colour Manager deriving
	 *  from agent aspects. These are used to determine how far the output
	 *  colour shifts from the initial colour to the targets.
	 */
	private float dial1;
	private float dial2;
	private float dial3;

	/**
	 * This section in colourManager
	 * @param palette
	 */
	public Colour(String palette)
	{
		String palettePath = "colourPalettes/" + palette;
		this.init(palettePath);
	}
	
	/**
	 * This section in colourManager?
	 * @param path
	 */
	public void init(String path)
	{
		Element palette = XmlHandler.loadDocument(path);
		Collection <Element> colours = XmlHandler.getElements(palette, colourTag);
		//hasChild(shift)
		//hasChild(opacity)
		//gather, obtain
	}
	
	/**
	 * 
	 * @param dial1 - variable 1
	 * @param dial2 - variable 2
	 * @param dial3 - variable 3
	 * @param opacity - given opacity
	 * @return
	 */
	public float[] returnColour(float dial1, float dial2, float dial3, float opacity)
	{
		this.opacity = opacity;
		float[] HSBOOut = {0.0f, 0.0f, 0.0f, this.opacity};
		for (int i = 0; i < 3; i++)
		{
			HSBOOut[i] = initialColour[i] + 
					dial1*(targetColour1[i]-initialColour[i]) +
					dial2*(targetColour2[i]-initialColour[i]) +
					dial3*(targetColour3[i]-initialColour[i]);
		}
		return HSBOOut;
	}
	
	/**
	 * 
	 * @param dial1 - variable 1
	 * @param dial2 - variable 2
	 * @param dial3 - variable 3
	 * no given opacity (defaults to the opacity already defined in the Colour
	 * object, which is 1.0 at setup unless defined in the palette).
	 * @return
	 */
	public float[] returnColour(float dial1, float dial2, float dial3)
	{
		float[] HSBOOut = {0.0f, 0.0f, 0.0f, this.opacity};
		for (int i = 0; i < 3; i++)
		{
			HSBOOut[i] = initialColour[i] + 
					dial1*(targetColour1[i]-initialColour[i]) +
					dial2*(targetColour2[i]-initialColour[i]) +
					dial3*(targetColour3[i]-initialColour[i]);
		}
		return HSBOOut;
	}

	@Override
	public AspectReg reg() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

package colour;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import dataIO.Log;

public class Colour {
	
	public enum Format
	{
		HSB,
		RGB;
	}
	
	private Color jColor;
	
	public final float[] zeros = new float[3];
	
	private Format format;
	private String name;

	/**
	 * These colours are all in HSB coding
	 */
	private float[] initialColour;
	private List<float[]> gradients = new LinkedList<float[]>();
	private float opacity = 1.0f;
	
	/** Dials: Numbers between 0 and 1 returned by the Colour Manager deriving
	 *  from agent aspects. These are used to determine how far the output
	 *  colour shifts from the initial colour to the targets.
	 */
	private float[] dial = new float[3];

	public Colour(float[] baseColour, String format, String name)
	{
		this.format = Format.valueOf( format );
		if( Format.HSB.equals( this.format ))
			this.initialColour = baseColour;
		else
		{
			this.initialColour = Color.RGBtoHSB(
					Math.round( baseColour[0]*255 ),
					Math.round( baseColour[1]*255 ),
					Math.round( baseColour[2]*255 ), null);
		}
		this.name = name;
	}
	
	public void addGradient( float[] gradient )
	{
		if( gradients.size() >= 3)
		{
			Log.out("ERROR: can add a maximum of 3 gradients per colour.");
			return;
		}
		this.gradients.add( gradient );
	}
	
	public void addGradient( String first, String second, String third )
	{
		float[] out = new float[3];
		if( first == null )
			out[0] = 0.0f;
		else
			out[0] = Float.valueOf( first );
		if( second == null )
			out[1] = 0.0f;
		else
			out[1] = Float.valueOf( second );
		if( third == null )
			out[2] = 0.0f;
		else
			out[2] = Float.valueOf( third );
		this.addGradient( out );
	}
	
	/**
	 * 
	 * @param dial
	 * @param opacity - given opacity
	 * @return
	 */
	public float[] returnColourHSB(float[] dial, float opacity)
	{
		this.opacity = opacity;
		float[] HSBOOut = {0.0f, 0.0f, 0.0f, this.opacity};
		for (int i = 0; i < 3; i++)
		{
			HSBOOut[i] = initialColour[i] + 
					line(0,i) +	line(1,i) +	line(2,i);
		}
		return HSBOOut;
	}
	
	public float line(int gradient, int field)
	{
		float[] grad = ( this.gradients.size() > gradient ?
				this.gradients.get( gradient ) : zeros );
		return dial[gradient] * ( grad[field] - initialColour[field] );
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
	public float[] returnColourHSB(float[] dial)
	{
		float[] HSBOOut = {0.0f, 0.0f, 0.0f};
		for (int i = 0; i < 3; i++)
		{
			HSBOOut[i] = initialColour[i] + 
					line(0,i) +	line(1,i) +	line(2,i);
		}
		return HSBOOut;
	}
	
	public float[] returnColourRGB(float[] dial)
	{
		float[] hsb = returnColourHSB(dial);
		jColor = new Color(Color.HSBtoRGB( hsb[0], hsb[1], hsb[2] ));
		return new float[] { 
				jColor.getRed() / 255.0f, 
				jColor.getGreen() / 255.0f, 
				jColor.getBlue() / 255.0f };
	}

	public void setOpacity(Float opacity) 
	{
		this.opacity = opacity;
	}	
}

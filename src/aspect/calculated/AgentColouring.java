package aspect.calculated;

import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;
import utility.Helper;


/**
 * This event inspects the redComponent, blueComponent and greenComponent 
 * aspects of the agent, and uses them to create a String representing RGB that 
 * can be read by graphical exporters. The values of the RGB components should
 * be between 0 and 1 and are scaled to between 0 and 255.
 * 
 * TODO - Catch values in the wrong range
 * @author Tim Foster
 *
 */

public class AgentColouring extends Calculated {
	
	public String RED = AspectRef.redComponent;
	public String GREEN = AspectRef.greenComponent;
	public String BLUE = AspectRef.blueComponent;
	
	public AgentColouring()
	{
		setInput("red, green, blue");
	}

	/**
	@Override
	public Object get(AspectInterface aspectOwner)
	{
		String pigmentRGB = new String();
		int red = (int) Math.round(255 * 
				(double) Helper.setIfNone(aspectOwner.getValue(RED), 0.0));
		int green = (int) Math.round(255 * 
				(double) Helper.setIfNone(aspectOwner.getValue(GREEN), 0.0));
		int blue = (int) Math.round(255 * 
				(double) Helper.setIfNone(aspectOwner.getValue(BLUE), 0.0));
		
		pigmentRGB = "rgb(" + red + "," + green + "," + blue + ")";
		
		return pigmentRGB;
	}
	*/
	@Override
	public Object get(AspectInterface aspectOwner)
	{
		double red = 
				(double) Helper.setIfNone(aspectOwner.getValue(RED), 0.0);
		double green = 
				(double) Helper.setIfNone(aspectOwner.getValue(GREEN), 0.0);
		double blue = 
				(double) Helper.setIfNone(aspectOwner.getValue(BLUE), 0.0);
		
		double[] pigmentRGB = {red, green, blue};
		
		return pigmentRGB;
	}
}

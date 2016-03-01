/**
 * 
 */
package guiTools;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import utility.ExtraMath;

/**
 * \brief Class loading a splash screen for the Graphical User Interface (GUI). 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiSplash
{
	/**
	 * Where to find the splash screen logo.
	 */
	private final static String IMAGE_PATH = "icons/iDynoMiCS_logo.png";
	/**
	 * Scaling factor for the splash screen image, to give a little padding
	 * around the sides.
	 */
	private final static double IMAGE_SCALE_FACTOR = 0.9;
	
	/**
	 * \brief Load in the image from the {@link #IMAGE_PATH}.
	 * 
	 * @return The image, if possible.
	 */
	private static Image loadImage()
	{
		try
		{
			return ImageIO.read(new File(IMAGE_PATH));
		}
		catch (IOException e)
		{
			// TODO Safety
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * \brief Makes a {@code JFrame} component with the iDyoMiCS logo as a
	 * splash screen.
	 * 
	 * @param frameWidth Width of the master frame, in pixels.
	 * @param frameHeight Height of the master frame, in pixels.
	 * @return {@code JComponent} with the rescaled logo image.
	 */
	public static JComponent getSplashScreen(int frameWidth, int frameHeight)
	{
		/* Load the image. */
		Image img = loadImage();
		/* 
		 * Get the current aspect ratios of both the image and of the master
		 * frame.
		 */
		double imgAspect = ExtraMath.division(img.getWidth(null),
												img.getHeight(null)); 
		double frameAspect = ExtraMath.division(frameWidth, frameHeight);
		/*
		 * Calculate the new size for the image. By using a negative number for
		 * the unimportant dimension, we let img.getScaledInstance() work it
		 * out for us.
		 */
		int width, height;
		if ( imgAspect > frameAspect )
		{
			width = (int) (IMAGE_SCALE_FACTOR*frameWidth);
			height = -1;
		}
		else
		{
			width = -1;
			height = (int) (IMAGE_SCALE_FACTOR*frameHeight);
		}
		img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		/*
		 * Finally, package the image up into a JComponent and return.
		 */
		return new JLabel(new ImageIcon(img));
	}
}

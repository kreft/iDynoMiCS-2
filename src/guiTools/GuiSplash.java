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
	private final static String IMAGE_PATH = "icons/iDynoMiCS_logo_medium.png";
	
	protected static JComponent component = setComponent();
	
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
	 * @return {@code JComponent} with the logo image.
	 */
	public static JComponent setComponent()
	{
		/* Load the image. */
		Image img = loadImage();
		/*
		 * Package the image up into a JComponent and return.
		 */
		return new JLabel(new ImageIcon(img), JLabel.CENTER);
	}
	
	public static JComponent getSplashScreen()
	{
		return component;
	}
}

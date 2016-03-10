/**
 * 
 */
package guiTools;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public final class GuiConsole
{
	/**
	 * Box in the GUI that displays text like a console would.
	 */
	private static JTextPane console;
	/**
	 * Background color of the console pane.
	 * TODO move this to something like a "GuiStyle" class?
	 */
	public static Color consoleBackground = new Color(38, 45, 48);
	/**
	 * Text style for normal output messages.
	 */
	private static SimpleAttributeSet outStyle = defaultOutStyle();
	/**
	 * Text style for error output messages.
	 */
	private static SimpleAttributeSet errorStyle = defaultErrorStyle();
	
	private static boolean autoScroll = true;
	
	protected static JComponent component = setComponent();
	
	/*************************************************************************
	 * CONSTRUCTOR
	 ************************************************************************/
	
	public static JComponent getConsole()
	{
		return component;
	}
	
	public static JComponent setComponent()
	{
		console = new JTextPane();
		console.setBackground(consoleBackground);
		return new JScrollPane(console,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
	
	/*************************************************************************
	 * STYLES
	 ************************************************************************/
	
	/**
	 * \brief Helper method for all default text styles.
	 * 
	 * @return
	 */
	private static SimpleAttributeSet defaultStyle()
	{
		SimpleAttributeSet a = new SimpleAttributeSet();
		/*
		 * Go through possible attributes in alphabetical order. See
		 * https://docs.oracle.com/javase/7/docs/api/javax/swing/text/StyleConstants.html
		 */
		StyleConstants.setAlignment(a, StyleConstants.ALIGN_LEFT);
		/* Background not set here: see GuiLaunch.consoleBackground. */
		/* Bold not set here. */
		StyleConstants.setFontFamily(a, "consolas");
		StyleConstants.setFontSize(a, 15);
		/* Foreground not set here. */
		StyleConstants.setItalic(a, false);
		return a;
	}
	
	/**
	 * \brief Default style set for output text.
	 * 
	 * @return
	 */
	private static SimpleAttributeSet defaultOutStyle()
	{
		SimpleAttributeSet a = defaultStyle();
		StyleConstants.setBold(a, false);
		StyleConstants.setForeground(a, Color.LIGHT_GRAY);
		return a;
	}
	
	/**
	 * \brief Default style set for error text.
	 * 
	 * @return
	 */
	private static SimpleAttributeSet defaultErrorStyle()
	{
		SimpleAttributeSet a = defaultStyle();
		StyleConstants.setBold(a, true);
		StyleConstants.setForeground(a, Color.RED);
		return a;
	}
	
	// TODO Let the user change the styles?
	
	/*************************************************************************
	 * HANDLING TEXT
	 ************************************************************************/
	
	/**
	 * \brief Append an output message to the output text area and update the
	 * line position.
	 * 
	 * @param message {@code String} message to write to the text area.
	 */
  	public static void writeOut(String message)
	{
  		write(message, outStyle);
	}
  	
  	/**
  	 * \brief Append an error message to the output text area and update the
  	 * line position.
	 * 
	 * @param message {@code String} message to write to the text area.
  	 */
  	public static void writeErr(String message)
  	{
  		write(message, errorStyle);
  	}
  	
  	/**
  	 * \brief Helper method for writing messages to the GUI console.
  	 * 
  	 * @param message
  	 * @param a
  	 */
  	private static void write(String message, AttributeSet a)
  	{
  		Document doc =	console.getDocument();
  		try
  		{
  			doc.insertString(doc.getLength(), message, a);
  		}
  		catch ( BadLocationException e )
  		{
  			// TODO
  		}
  		if ( autoScroll )
  			console.setCaretPosition(doc.getLength());
  	}
	
	/**
	 * \brief User input in the GUI text area.
	 * 
	 * @param description
	 * @return
	 */
	public static String requestInput(String description)
	{
		JFrame frame = new JFrame();
		String s = (String) JOptionPane.showInputDialog(
		                    frame,
		                    description,
		                    "Customized Dialog",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null, null,
		                    "");

		return s;
	}
	
	
	public static JCheckBox autoScrollCheckBox()
	{
		JCheckBox out = new JCheckBox("Autoscroll");
		out.setSelected(autoScroll);
		out.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				int state = e.getStateChange();
				GuiConsole.autoScroll = (state == ItemEvent.SELECTED);
			}
		});
		return out;
	}
}
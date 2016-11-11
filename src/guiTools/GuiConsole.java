/**
 * 
 */
package guiTools;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

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

import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import utility.Helper;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public final class GuiConsole
{
	/**
	 * Box in the GUI that displays text like a console would.
	 */
	private static JTextPane _console = new JTextPane();
	
	/**
	 * Background color of the console pane.
	 * TODO move this to something like a "GuiStyle" class?
	 */
	private static Color _consoleBackground = new Color(38, 45, 48);
	
	/**
	 * Text style for normal output messages.
	 */
	private static SimpleAttributeSet _outStyle = defaultOutStyle();
	
	/**
	 * Text style for error output messages.
	 */
	private static SimpleAttributeSet _errorStyle = defaultErrorStyle();
	
	private static boolean _autoScroll = true;
	

	
	/*************************************************************************
	 * CONSTRUCTOR
	 ************************************************************************/

	
	public static JComponent getConsole()
	{
		_console.setBackground(_consoleBackground);
		
		/**
		 * based on
		 * http://stackoverflow.com/questions/811248/
		 * how-can-i-use-drag-and-drop-in-swing-to-get-file-path
		 * 
		 * TODO check whether this also works on mac and linux.
		 */
		_console.setDropTarget(new DropTarget() {
			private static final long serialVersionUID = -8965667461314634402L;

			@SuppressWarnings("unchecked")
			public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            List<File> droppedFiles = (List<File>)
		                evt.getTransferable().getTransferData(
		                		DataFlavor.javaFileListFlavor);
		            if ( droppedFiles.size() > 1 )
		            	Log.out(Tier.QUIET, "Unable to open multiple files at "
		            			+ "once");
		            else if ( Idynomics.simulator != null )
		            {
		            	if ( Helper.obtainInput("Are you sure you want to"
		            			+ "replace the current model state with: \n" + 
		            			droppedFiles.get(0).getName() + "?", false) )
		            		GuiActions.openFile( droppedFiles.get(0) );
		            }
		            else
	            		GuiActions.openFile( droppedFiles.get(0) );

		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		});
		
		return new JScrollPane(_console,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
	
	public static void displayConsole()
	{
		final JFrame frame = new JFrame ("console");
		frame.add(GuiConsole.getConsole());
		
		/* size the window */
		frame.setSize(300, 300);

		/* set the frame's initial position and make it visable */
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
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
  		write(message, _outStyle);
	}
  	
  	/**
  	 * \brief Append an error message to the output text area and update the
  	 * line position.
	 * 
	 * @param message {@code String} message to write to the text area.
  	 */
  	public static void writeErr(String message)
  	{
  		write(message, _errorStyle);
  	}
  	
  	/**
  	 * \brief Helper method for writing messages to the GUI console.
  	 * 
  	 * @param message
  	 * @param a
  	 */
  	private static void write(String message, AttributeSet a)
  	{
  		Document doc =	_console.getDocument();
  		try
  		{
  			doc.insertString(doc.getLength(), message, a);
  		}
  		catch ( BadLocationException e )
  		{
  			// TODO
  		}
  		if ( _autoScroll )
  			_console.setCaretPosition(doc.getLength());
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
	
	public static String requestInput(String[] options, String description)
	{
		JFrame frame = new JFrame();
		String s = (String) JOptionPane.showInputDialog(
		                    frame,
		                    description,
		                    "Customized Dialog",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null, options,
		                    "");
		return s;
	}
	
	/**
	 * 
	 * @param description
	 * @return
	 */
	public static boolean requestInputBoolean(String description)
	{
		JFrame frame = new JFrame();
		int i = JOptionPane.showConfirmDialog(
				frame,
				description,
				"Customized Dialog",
				JOptionPane.YES_NO_OPTION);
		return (i==JOptionPane.YES_OPTION);
	}
	
	public static JCheckBox autoScrollCheckBox()
	{
		JCheckBox out = new JCheckBox("Autoscroll");
		out.setSelected(_autoScroll);
		out.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				int state = e.getStateChange();
				GuiConsole._autoScroll = (state == ItemEvent.SELECTED);
			}
		});
		return out;
	}
}
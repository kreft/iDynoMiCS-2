package idynomics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import dataIO.Log;
import dataIO.Log.Tier;
import glRender.AgentMediator;
import glRender.CommandMediator;
import glRender.Render;
import utility.Helper;

/**
 * \brief General class to launch simulation from a Graphical User Interface
 * (GUI).
 * 
 * <p>User can select a protocol file from a window and launch the
 * simulator.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class GuiLaunch implements Runnable
{
	/**
	 * Box in the GUI that displays text like a console would.
	 */
	private static JTextPane console = new JTextPane();
	/**
	 * Background color of the console pane.
	 */
	private static Color consoleBackground = new Color(38, 45, 48);
	/**
	 * Text style for normal output messages.
	 */
	private static SimpleAttributeSet outStyle = defaultOutStyle();
	/**
	 * Text style for error output messages.
	 */
	private static SimpleAttributeSet errorStyle = defaultErrorStyle();
	
	/*************************************************************************
	 * DEFAULTS
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
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	/**
	 * \brief Launch with a Graphical User Interface (GUI).
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		new GuiLaunch();
	}
	
  	/**
  	 * \brief Construct the GUI and run it.
  	 */
	public GuiLaunch() 
	{
		run();
	}
			    	  
   /**
    * \brief The GUI is runnable otherwise it will become unresponsive until
    * the simulation finishes.
    */
	public void run()
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException | ClassNotFoundException 
			  | InstantiationException  | IllegalAccessException e)
		{
			// TODO? Or do nothing?
		}
		/* 
		 * When running in GUI we want dialog input instead of command line 
		 * input.
		 */
		Helper.gui = true;
		JFrame gui = new JFrame();
		/* 
		 * Set the output textArea.
		 */
		//console.setEditable(false);
		console.setBackground(consoleBackground);
		//console.setForeground(Color.LIGHT_GRAY);
		//guiTextArea.setLineWrap(true);
		//Font font = new Font("consolas", Font.PLAIN, 15);
		//console.setFont(font);
		/* 
		 * Set the window size, position, title and its close operation.
		 */
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle(Idynomics.fullDescription());
		gui.setSize(800, 800);
		gui.setLocationRelativeTo(null);
		gui.add(new JScrollPane(console, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		/* 
		 * Set an action for the button (run the simulation).
		 */
		JButton launchSim = new JButton("Run!");
		launchSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if ( Param.protocolFile != null )
					Idynomics.setupCheckLaunch(Param.protocolFile);
			}
		});
		gui.add(launchSim, BorderLayout.SOUTH);
		/* 
		 * Construct the menu bar.
		 */
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;
		/* 
		 * File menu.
		 */
		menuBar = new JMenuBar();
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("File options");
		menuBar.add(menu);
		/* 
		 * Open a protocol file.
		 */
		menuItem = new JMenuItem(new FileOpen());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open existing protocol file");
		menu.add(menuItem);
		/*
		 * Open render frame: draw the agents in a compartment.
		 */
		menuItem = new JMenuItem(new RenderThis());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Render a spatial compartment");
		menu.add(menuItem);
		/* 
		 * Template for further development: we can do switches or toggles
		 * later.
		 */
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("placeholder");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(cbMenuItem);
		/*
		 * Output level.
		 */
		menu.addSeparator();
		submenu = new JMenu("OutputLevel");
		submenu.setMnemonic(KeyEvent.VK_L);
		ButtonGroup group = new ButtonGroup();
		for ( Log.Tier t : Log.Tier.values() )
		{
			rbMenuItem = new JRadioButtonMenuItem(new LogTier(t));
			group.add(rbMenuItem);
			submenu.add(rbMenuItem);
		}
		menu.add(submenu);
		/* 
		 * Add the menu bar to the GUI and make everything visible.
		 */
		gui.setJMenuBar(menuBar);
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(0, 0));
		gui.add(p, BorderLayout.NORTH);
		keyBindings(p,gui);
		gui.setVisible(true);
	}
	
  	private static void keyBindings(JPanel p, JFrame frame) 
  	{
  		ActionMap actionMap = p.getActionMap();
  		InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

  		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "run");
  		actionMap.put("run", new AbstractAction()
  		{
  			private static final long serialVersionUID = 346448974654345823L;

  			@Override
  			public void actionPerformed(ActionEvent a)
  			{
  				if ( Param.protocolFile != null )
  					Idynomics.setupCheckLaunch(Param.protocolFile);
  			}
  		});
  	}
	
	/**
	 * \brief Method to select protocol files from a file selection dialog
	 * 
	 * @return XML file selected from the dialog box.
	 */
	public static File chooseFile() 
	{
		/* Open a FileChooser window in the current directory. */
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir")+"/protocol");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(false);
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION )
		{
			return chooser.getSelectedFile();
		}
		else
		{
			return null;
		}
	}
	
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
  		// TODO disable this if the user tries to scroll up?
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
	
	/*************************************************************************
	 * BUTTON ACTIONS
	 ************************************************************************/
	
	public class FileOpen extends AbstractAction
	{
		private static final long serialVersionUID = 2247122248926681550L;
		
		/**
		 * Action for the file open sub-menu.
		 */
		public FileOpen()
		{
	        super("Open..");
		}
		
	    public void actionPerformed(ActionEvent e)
	    {
	    	File f = chooseFile();
	    	/* Don't crash if the user has clicked cancel. */
	    	if ( f == null )
	    	{
	    		Param.protocolFile = null;
	    		writeOut("Please choose a protocol file\n");
	    	}
	    	else
	    	{
	    		Param.protocolFile = f.getAbsolutePath();
	    		writeOut(Param.protocolFile + " \n");
	    	}
	    }
	}
	
	public class RenderThis extends AbstractAction
	{
		private static final long serialVersionUID = 974971035938028563L;

		/**
		 * Create a new {@code Render} object and invoke it.
		 * 
		 *  <p>The {@code Render} object handles its own {@code JFrame}.</p>
		 */
		public RenderThis()
		{
	        super("Render");
		}
	
	    public void actionPerformed(ActionEvent e)
	    {
	    	if ( Idynomics.simulator == null || 
	    					! Idynomics.simulator.hasSpatialCompartments() )
	    	{
	    		//guiTextArea.append();
	    		writeErr("No spatial compartments available!\n");
	    	}
	    	else
	    	{
	    		Compartment c = Idynomics.simulator.get1stSpatialCompartment();
	    		CommandMediator cm = new AgentMediator(c.agents);
	    		Render myRender = new Render(cm);
				EventQueue.invokeLater(myRender);
	    	}
	    }
	}
	
	public class LogTier extends AbstractAction
	{
		private static final long serialVersionUID = 2660256074849177100L;
		
		/**
		 * The output level {@code Tier} for the log file that this button
		 * represents.
		 */
		private Tier _tier;
		
		/**
		 * Action for the set Log Tier sub-menu.
		 */
		public LogTier(Log.Tier tier)
		{
			super(tier.toString());
			this._tier = tier;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Log.set(this._tier);
		}
	}
}

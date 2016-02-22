package idynomics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
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
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


import dataIO.Log;
import dataIO.Log.tier;
import glRender.AgentMediator;
import glRender.CommandMediator;
import glRender.Render;
import utility.Helper;

/**
 * 
 * @author baco
 *
 */
public class GuiLaunch implements Runnable {
	
	private static JTextArea guiTextArea = new JTextArea(15, 60);

	/**
	 * Launch with gui
	 * @param args
	 */
	public static void main(String[] args) 
	{
		new GuiLaunch();
	}
  
	/**
	 * append a message to the output textarea and update the line position
	 * @param message
	 */
  	public static void guiWrite(String message)
	{
  		guiTextArea.append(message);
  		guiTextArea.setCaretPosition(guiTextArea.getText().length());
  		guiTextArea.update(GuiLaunch.guiTextArea.getGraphics());
	}
  
  	/**
  	 * create the gui and run it
  	 */
	public GuiLaunch() 
	{
		run();
	}
			    	  
   /**
    * The gui is runnable otherwise it will become unresponsive until the
    * simulation finishes
    */
	public void run()
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException | ClassNotFoundException 
			  | InstantiationException  | IllegalAccessException e) {
		}
		
		/* when running in gui we want dialog input instead of command line 
		 * input */
		Helper.gui = true;
		JFrame gui = new JFrame();
		
		/* set the output textArea */
		guiTextArea.setEditable(false);
		guiTextArea.setBackground(new Color(38, 45, 48));
		guiTextArea.setForeground(Color.LIGHT_GRAY);
		guiTextArea.setLineWrap(true);
		Font font = new Font("consolas", Font.PLAIN, 15);
		guiTextArea.setFont(font);
		
		/* set the window size, position, title and its close operation */
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("iDynoMiCS 2.0");
		gui.setSize(800,800);
		gui.setLocationRelativeTo(null);
		
		/* add the text area and button to the gui */
		gui.add(new JScrollPane(guiTextArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		JButton launchSim = new JButton("Run!");
		
		
		/* set an cation for the button (run the simulation */
		launchSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if(Param.protocolFile != null)
				{
					Timer._now = 0.0;
					ConsoleLaunch.runXml();
				}
			}
		});
		gui.add(launchSim,BorderLayout.SOUTH);

		/* construct the menu bar */
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;

		/* File menu */
		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("File options");
		menuBar.add(menu);

		/* file open */
		menuItem = new JMenuItem(new FileOpen());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open existing protocol file");
		menu.add(menuItem);

		/* open render frame */
		menuItem = new JMenuItem(new RenderThis());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open existing protocol file");
		menu.add(menuItem);

		/* we can do switches or toggles later */
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("placeholder");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(cbMenuItem);

		/* output level */
		menu.addSeparator();
		submenu = new JMenu("OutputLevel");
		submenu.setMnemonic(KeyEvent.VK_L);

		ButtonGroup group = new ButtonGroup();
		for(Log.tier t : Log.tier.values())
		{
			rbMenuItem = new JRadioButtonMenuItem(new LogTier(t));
			group.add(rbMenuItem);
			submenu.add(rbMenuItem);
		}

		menu.add(submenu);
		
		/* at the menu bar to the gui and make everything visible */
		gui.setJMenuBar(menuBar);
		
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(0,0));
		gui.add(p, BorderLayout.NORTH);
		
		keyBindings(p,gui);
		gui.setVisible(true);
	}
	
	private static void keyBindings(JPanel p, JFrame frame) 
	{
		ActionMap actionMap = p.getActionMap();
		InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "run");
		actionMap.put("run", new AbstractAction(){

			private static final long serialVersionUID = 346448974654345823L;

			@Override
			public void actionPerformed(ActionEvent a) {
				if(Param.protocolFile != null)
				{
					Timer._now = 0.0;
					ConsoleLaunch.runXml();
				}
			}
		});
	}

	
	/**
	 * action for the file open button
	 */
	public class FileOpen extends AbstractAction {
	
		public FileOpen() {
	        super("Open..");
		}
	
	    public void actionPerformed(ActionEvent e) {
	    	Param.protocolFile = chooseFile().getAbsolutePath();

	    	guiTextArea.setText(Param.protocolFile + " \n");
	    }
	}
	
	/**
	 * create a new Render object and invoke it (the Render object handles it's
	 * own JFrame)
	 */
	public class RenderThis extends AbstractAction {
		
		public RenderThis() {
	        super("Render");
		}
	
	    public void actionPerformed(ActionEvent e) {
	    	if(Idynomics.simulator._compartments.keySet().size() == 0)
	    		guiTextArea.append("no compartments available \n");
	    	else
	    	{
			Render myRender = new Render((CommandMediator) new AgentMediator(
					Idynomics.simulator._compartments.get(Idynomics.simulator.
							_compartments.keySet().toArray()[0]).agents));
			
			EventQueue.invokeLater(myRender);
	    	}
	    }
	}
	
	/**
	 * Action for the set Log tier buttons
	 */
	public class LogTier extends AbstractAction {
		private tier _tier;

		public LogTier(Log.tier tier) {
			super(tier.toString());
			this._tier = tier;
		}
		
		public void actionPerformed(ActionEvent e) {
			Log.set(_tier);
		}
	}
  
	/**
	 * \brief Method to select protocol files from a file selection dialog
	 * 
	 * @return XML file selected from the dialog box.
	 */
	public static File chooseFile() 
	{
		// Open a FileChooser window in the current directory
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir")+"/protocol");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(false);
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ) { }
		return chooser.getSelectedFile();
	}
	
	/**
	 * Gui user input
	 * @param description
	 * @return
	 */
	public static String requestInput(String description)
	{
		JFrame frame = new JFrame();
		String s = (String)JOptionPane.showInputDialog(
		                    frame,
		                    description,
		                    "Customized Dialog",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null, null,
		                    "");

		return s;
	}
	  
}

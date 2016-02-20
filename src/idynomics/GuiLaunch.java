package idynomics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.KeyEvent;

import java.io.File;


import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


import dataIO.Log;
import dataIO.Log.tier;
import utility.Helper;


public class GuiLaunch {
	
	private static JTextArea guiTextArea = new JTextArea(15, 60);

	public static void main(String[] args) 
	{
		new GuiLaunch();
	}
  
  	public static void guiWrite(String message)
	{
  		guiTextArea.append(message);
  		guiTextArea.setCaretPosition(guiTextArea.getText().length());
  		guiTextArea.update(GuiLaunch.guiTextArea.getGraphics());
	}
  
	public GuiLaunch() {
		openGui();
	}
			    	  
   
	public void openGui()
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException | ClassNotFoundException 
			  | InstantiationException  | IllegalAccessException e) {
		}
		
		Helper.gui = true;
		JFrame gui = new JFrame();
		
		guiTextArea.setEditable(false);
		guiTextArea.setBackground(new Color(38, 45, 48));
		guiTextArea.setForeground(Color.LIGHT_GRAY);
		guiTextArea.setLineWrap(true);
		Font font = new Font("consolas", Font.PLAIN, 15);
		guiTextArea.setFont(font);
		
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle("iDynoMiCS 2.0");
		gui.setSize(800,800);
		gui.setLocationRelativeTo(null);
		gui.add(new JScrollPane(guiTextArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		JButton launchSim = new JButton("Run!");
		
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

		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;

		/* menu bar */
		menuBar = new JMenuBar();

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("File options");
		menuBar.add(menu);

		menuItem = new JMenuItem(new FileOpen());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open existing protocol file");
		menu.add(menuItem);

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
		gui.setJMenuBar(menuBar);
		gui.setVisible(true);
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

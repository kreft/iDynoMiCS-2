package idynomics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import dataIO.Log;
import guiTools.GuiActions;
import guiTools.GuiConsole;
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
		 * Set the window size, position, title and its close operation.
		 */
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setTitle(Idynomics.fullDescription());
		gui.setSize(800, 800);
		gui.setLocationRelativeTo(null);
		gui.add(new JScrollPane(GuiConsole.getConsole(), 
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
		menuItem = new JMenuItem(new GuiActions.FileOpen());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open existing protocol file");
		menu.add(menuItem);
		/*
		 * Open render frame: draw the agents in a compartment.
		 */
		menuItem = new JMenuItem(new GuiActions.RenderThis());
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
			rbMenuItem = new JRadioButtonMenuItem(new GuiActions.LogTier(t));
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
		keyBindings(p, gui);
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
 }

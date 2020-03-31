package idynomics.launchable;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.GuiActions;
import gui.GuiButtons;
import gui.GuiEditor;
import gui.GuiMain;
import gui.GuiMenu;
import idynomics.Idynomics;
import utility.Helper;

/**
 * \brief General class to launch simulation from a Graphical User Interface
 * (GUI).
 * 
 * <p>User can select a protocol file from a window and launch the
 * simulator.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public strictfp class GuiLaunch implements Runnable, Launchable
{
	/**
	 * 
	 */
	private static JFrame _masterFrame;

	/**
	 * System file path to the iDynoMiCS logo.
	 */
	private final static String ICON_PATH = "icons/iDynoMiCS_logo_icon.png";

	/**
	 * \brief Launch with a Graphical User Interface (GUI).
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{
		GuiLaunch gui = new GuiLaunch();
		gui.run();
	}
	
	public void initialize(String[] args)
	{
		run();
	}

	
	public static boolean classicGui()
	{
		return (_masterFrame != null);
	}

	/**
	 * \brief The GUI is runnable otherwise it will become unresponsive until
	 * the simulation finishes.
	 */
	public void run()
	{
		_masterFrame = new JFrame();
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException | ClassNotFoundException 
				| InstantiationException  | IllegalAccessException e)
		{
			/* do nothing, if we cannot set the system look and feel we should 
			 * get the java default look and feel which should still work.  */
		}
		/* 
		 * When running in GUI we want dialog input instead of command line 
		 * input.
		 */
		Helper.isSystemRunningInGUI = true;
		/* 
		 * Set the window size, position, title and its close operation.
		 */
		_masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon img = new ImageIcon(ICON_PATH);
		_masterFrame.setIconImage(img.getImage());
		_masterFrame.setTitle(Idynomics.fullDescription());
		_masterFrame.setMinimumSize(new Dimension(520,60));
		_masterFrame.setPreferredSize(new Dimension(800,800));
		_masterFrame.setLocationByPlatform(true);

		/* set layout and menubar */
		_masterFrame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		_masterFrame.setJMenuBar(GuiMenu.getMenuBar());

		/* Set, size and scale upper part part */
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		_masterFrame.add(GuiButtons.getButtons(),c);

		/* Set, size and scale lower part part */
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.CENTER; 
		c.gridx = 0;
		c.gridy = 1;
		_masterFrame.add(GuiMain.getConstructor(),c);
		
		/* Set, size and scale lower part part */
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.PAGE_END; 
		c.gridx = 0;
		c.gridy = 2;
		_masterFrame.add(GuiMain.newStatusBar(),c);
		
		

		/* Bind keys and display window */
		keyBindings(_masterFrame.getRootPane());
		_masterFrame.pack();		
		_masterFrame.setVisible(true);

	}

	/**
	 * KeyBindings hosts a collection of actions which are initiate on key press
	 * @param JComponent p, the component the keybingings will be asociated with
	 * @param frame
	 */
	@SuppressWarnings("serial")
	public static void keyBindings(JComponent p) 
	{
		ActionMap actionMap = p.getActionMap();
		InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		/* Run simulation */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "run");
		actionMap.put("run", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent a)
			{
				GuiActions.runSimulation();
			}
		});
	}
}

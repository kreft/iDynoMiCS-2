package idynomics;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import gui.GuiActions;
import gui.GuiButtons;
import gui.GuiMain;
import gui.GuiMenu;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
public strictfp class GuiLaunch implements Runnable
{
	/**
	 * 
	 */
	private static JFrame _masterFrame;
	
	/**
	 * 
	 */
	public static JComponent currentView;
	
	/**
	 * 
	 */
	private static GroupLayout _layout;
	
	/**
	 * 
	 */
	private static SequentialGroup _verticalLayoutGroup;
	
	/**
	 * 
	 */
	private static ParallelGroup _horizontalLayoutGroup;
	
	/**
	 * Flag telling this GUI whether to be full screen (true) or not (false).
	 */
	private static boolean _isFullScreen = false;

	/**
	 * System file path to the iDynoMiCS logo.
	 */
	private final static String ICON_PATH = "icons/iDynoMiCS_logo_icon.png";
	
	/**
	 * 
	 */
	public static JPanel contentPane;
	
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
		_masterFrame = new JFrame();
		contentPane = new JPanel();
		_layout = new GroupLayout(contentPane);
		contentPane.setLayout(_layout);
		 
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
			/* do nothing, if we cannot set the system look and feel we should 
			 * get the java default look and feel which should still work.  */
		}
		/* 
		 * When running in GUI we want dialog input instead of command line 
		 * input.
		 */
		Helper.gui = true;
		/* 
		 * Set the window size, position, title and its close operation.
		 */
		_masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_masterFrame.setTitle(Idynomics.fullDescription());
		if ( _isFullScreen )
			_masterFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		else
			_masterFrame.setSize(800,800);
		_masterFrame.setLocationRelativeTo(null);
		
		ImageIcon img = new ImageIcon(ICON_PATH);

		_masterFrame.setIconImage(img.getImage());
		
		/* 
		 * Add the menu bar. This is independent of the layout of the rest of
		 * the GUI.
		 */
		_masterFrame.setJMenuBar(GuiMenu.getMenuBar());
		/*
		 * Set up the layout manager and its groups.
		 */
		contentPane.removeAll();
		_layout.setAutoCreateGaps(true);
		_layout.setAutoCreateContainerGaps(true);
		_verticalLayoutGroup = _layout.createSequentialGroup();
		_horizontalLayoutGroup = _layout.createParallelGroup();
		
		currentView = GuiMain.getConstructor();
		
		_horizontalLayoutGroup.addComponent(currentView, 
				GroupLayout.DEFAULT_SIZE, 
				GroupLayout.DEFAULT_SIZE,
				Short.MAX_VALUE);
		
		_verticalLayoutGroup.addComponent(currentView, 
						GroupLayout.DEFAULT_SIZE, 
						GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE);
		/* 
		* Apply the layout and build the GUI.
		*/
		_layout.setVerticalGroup(_verticalLayoutGroup);
		_layout.setHorizontalGroup(_horizontalLayoutGroup);
		
		/* Bas: quick fix, coupled this to contentPane for now since old
		 * structure is gone.
		 */
		keyBindings(contentPane);
		
		/* 
		 * Checked this and works correctly, masterFrame stays at one component
		 * the contentPane
		 */
        //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(GuiButtons.getButtons());
        splitPane.setBottomComponent(contentPane);
        splitPane.setDividerLocation(28); 
        splitPane.setDividerSize(0);
        splitPane.setEnabled( false );
		_masterFrame.add(splitPane);
		
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

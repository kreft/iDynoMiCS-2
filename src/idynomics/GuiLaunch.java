package idynomics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import gui.GuiActions;
import gui.GuiConsole;
import gui.GuiEditor;
import gui.GuiMain;
import gui.GuiMenu;
import gui.GuiSimControl;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
	 * 
	 */
	private static JProgressBar _progressBar;
	
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
			// TODO? Or do nothing?
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

		drawButtons();
		
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
		_masterFrame.add(contentPane);
		
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
	
	/**
	 * TODO clean-up and comment
	 */
	public static void drawButtons() 
	{
		/*
		 * Just below the menu bar, make the bar of simulation control buttons.
		 */
		SequentialGroup buttonHoriz = _layout.createSequentialGroup();
		ParallelGroup buttonVert = _layout.createParallelGroup();
		JButton button;
		
		/* Check the simulation. */
		button = GuiSimControl.openButton();
		buttonHoriz.addComponent(button);
		buttonVert.addComponent(button);
		
		/* new simulation */
		button = new JButton("new");
		{
			button.addActionListener(new ActionListener()
			{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Idynomics.simulator = new Simulator();
				Idynomics.global = new Param();
				GuiMain.getConstructor();
				GuiEditor.addComponent(Idynomics.simulator.getModule(), GuiMain.tabbedPane);
			}
		}
		);
		}
		buttonHoriz.addComponent(button);
		buttonVert.addComponent(button);
		
		/* Run the simulation. */
		button = GuiSimControl.runButton();
		buttonHoriz.addComponent(button);
		buttonVert.addComponent(button);
		/* Pause the simulation. */
		// TODO This doesn't work yet...
		//button = GuiSimControl.pauseButton();
		//buttonHoriz.addComponent(button);
		//buttonVert.addComponent(button);
		/* Stop the simulation. */
		button = GuiSimControl.stopButton();
		buttonHoriz.addComponent(button);
		buttonVert.addComponent(button);

		///////////////////////////////////////////////////////////////////////
		/* Add a progress bar to the button row. */
		_progressBar  = new JProgressBar();
		_progressBar.setStringPainted(true);
		buttonHoriz.addComponent(_progressBar);
		buttonVert.addComponent(_progressBar);
		/* Add a checkbox for the GuiConsole autoscrolling. */
		JCheckBox autoscroll = GuiConsole.autoScrollCheckBox();
		buttonHoriz.addComponent(autoscroll);
		buttonVert.addComponent(autoscroll);
		/* Add these to the layout. */
		_verticalLayoutGroup.addGroup(buttonVert);
		_horizontalLayoutGroup.addGroup(buttonHoriz);
	}
	
	/**
	 * \brief Reset the simulation progress bar to 0%.
	 */
	public static void resetProgressBar()
	{
		_progressBar.setMinimum(Idynomics.simulator.timer.getCurrentIteration());
		_progressBar.setValue(Idynomics.simulator.timer.getCurrentIteration());
		_progressBar.setMaximum(Idynomics.simulator.timer.estimateLastIteration());
	}
	
	/**
	 * \brief Move the simulation progress bar along with the Timer. 
	 */
	public static void updateProgressBar()
	{
		_progressBar.setValue(Idynomics.simulator.timer.getCurrentIteration());
	}
 }

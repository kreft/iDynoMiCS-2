package idynomics;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
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

import guiTools.GuiActions;
import guiTools.GuiConsole;
import guiTools.GuiEditor;
import guiTools.GuiMain;
import guiTools.GuiMenu;
import guiTools.GuiSimControl;
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
	 * 
	 */
	private static JFrame masterFrame;
	
	/**
	 * 
	 */
	public static JComponent currentView;
	
	/**
	 * 
	 */
	private static GroupLayout layout;
	
	/**
	 * 
	 */
	public static SequentialGroup verticalLayoutGroup;
	
	/**
	 * 
	 */
	public static ParallelGroup horizontalLayoutGroup;
	
	/**
	 * 
	 */
	private static JProgressBar progressBar;
	
	/**
	 * Flag telling this GUI whether to be full screen (true) or not (false).
	 */
	private static boolean isFullScreen = false;
	
	/**
	 * For storing the dimension of the gui window in order to toggle between
	 * full screen and previous size TODO seems the related code has been
	 * removed, is this feature unwanted or should we restore?
	 */
	private static Dimension windowedDimension;
	
	/**
	 * For storing the position of the gui window in order to toggle between
	 * full screen and previous state TODO seems the related code has been
	 * removed, is this feature unwanted or should we restore?
	 */
	private static Point point = new Point(0,0);
	
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
		masterFrame = new JFrame();
		contentPane = new JPanel();
		layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		
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
		masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		masterFrame.setTitle(Idynomics.fullDescription());
		if ( isFullScreen )
			masterFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		else
			masterFrame.setSize(800,800);
		masterFrame.setLocationRelativeTo(null);
		
		ImageIcon img = new ImageIcon(ICON_PATH);

		masterFrame.setIconImage(img.getImage());
		
		/* 
		 * Add the menu bar. This is independent of the layout of the rest of
		 * the GUI.
		 */
		masterFrame.setJMenuBar(GuiMenu.getMenuBar());
		/*
		 * Set up the layout manager and its groups.
		 */
		contentPane.removeAll();
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		verticalLayoutGroup = layout.createSequentialGroup();
		horizontalLayoutGroup = layout.createParallelGroup();

		drawButtons();
		
		currentView = GuiMain.getConstructor();
		
		horizontalLayoutGroup.addComponent(currentView, 
				GroupLayout.DEFAULT_SIZE, 
				GroupLayout.DEFAULT_SIZE,
				Short.MAX_VALUE);
		
		verticalLayoutGroup.addComponent(currentView, 
						GroupLayout.DEFAULT_SIZE, 
						GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE);
		/* 
		* Apply the layout and build the GUI.
		*/
		layout.setVerticalGroup(verticalLayoutGroup);
		layout.setHorizontalGroup(horizontalLayoutGroup);
		
		/* Bas: quick fix, coupled this to contentPane for now since old
		 * structure is gone.
		 */
		keyBindings(contentPane);
		
		/* 
		 * Checked this and works correctly, masterFrame stays at one component
		 * the contentPane
		 */
		masterFrame.add(contentPane);
		
		masterFrame.setVisible(true);

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
		SequentialGroup buttonHoriz = layout.createSequentialGroup();
		ParallelGroup buttonVert = layout.createParallelGroup();
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
				GuiEditor.addComponent(Idynomics.simulator.getNode(), GuiMain.tabbedPane);
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
		progressBar  = new JProgressBar();
		progressBar.setStringPainted(true);
		buttonHoriz.addComponent(progressBar);
		buttonVert.addComponent(progressBar);
		/* Add a checkbox for the GuiConsole autoscrolling. */
		JCheckBox autoscroll = GuiConsole.autoScrollCheckBox();
		buttonHoriz.addComponent(autoscroll);
		buttonVert.addComponent(autoscroll);
		/* Add these to the layout. */
		verticalLayoutGroup.addGroup(buttonVert);
		horizontalLayoutGroup.addGroup(buttonHoriz);
	}
	
	/**
	 * \brief Reset the simulation progress bar to 0%.
	 */
	public static void resetProgressBar()
	{
		progressBar.setMinimum(Idynomics.simulator.timer.getCurrentIteration());
		progressBar.setValue(Idynomics.simulator.timer.getCurrentIteration());
		progressBar.setMaximum(Idynomics.simulator.timer.estimateLastIteration());
	}
	
	/**
	 * \brief Move the simulation progress bar along with the Timer. 
	 */
	public static void updateProgressBar()
	{
		progressBar.setValue(Idynomics.simulator.timer.getCurrentIteration());
	}
 }

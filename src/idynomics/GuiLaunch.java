package idynomics;

import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import guiTools.GuiConsole;
import guiTools.GuiMenu;
import guiTools.GuiProtocol;
import guiTools.GuiSimBuilder;
import guiTools.GuiSimControl;
import guiTools.GuiSplash;
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
	public enum ViewType
	{
		SPLASH,
		
		CONSOLE,
		
		RENDER,
		
		PROTOCOLMAKER,
		
		SIMULATIONMAKER,
		
		GRAPH
	}
	
	private static JFrame masterFrame;
	
	private static HashMap<ViewType,JComponent> views;
	
	private static JComponent currentView;
	
	private static GroupLayout layout;
	
	private static JProgressBar progressBar;
	
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
		layout = new GroupLayout(masterFrame.getContentPane());
		masterFrame.getContentPane().setLayout(layout);
		
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
		masterFrame.setSize(800,800);
		masterFrame.setLocationRelativeTo(null);
		/* 
		 * Add the menu bar. This is independent of the layout of the rest of
		 * the GUI.
		 */
		masterFrame.setJMenuBar(GuiMenu.getMenuBar());
		/*
		 * Set up the layout manager and its groups.
		 */
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		SequentialGroup verticalLayoutGroup = layout.createSequentialGroup();
		ParallelGroup horizontalLayoutGroup = layout.createParallelGroup();
		/*
		 * Just below the menu bar, make the bar of simulation control buttons.
		 */
		SequentialGroup buttonHoriz = layout.createSequentialGroup();
		ParallelGroup buttonVert = layout.createParallelGroup();
		JButton button;
		/* Check the simulation. */
		button = GuiSimControl.checkButton();
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
		/*
		 * Construct the views and add them to the HashMap.
		 */
		views = new HashMap<ViewType,JComponent>();
		views.put(ViewType.SPLASH, GuiSplash.getSplashScreen());
		views.put(ViewType.CONSOLE, GuiConsole.getConsole());
		/*
		 * Use the splash view to start with.
		 */
		currentView = views.get(ViewType.SPLASH);
		/*
		 * Add this to the layout.
		 */
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
		masterFrame.setVisible(true);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param vType
	 */
	public static void setView(ViewType vType)
	{
		if ( ! views.containsKey(vType) )
		{
			switch (vType)
			{
			case PROTOCOLMAKER:
				views.put(ViewType.PROTOCOLMAKER, GuiProtocol.getProtocolEditor());
				break;
			case SIMULATIONMAKER:
				views.put(ViewType.SIMULATIONMAKER, GuiSimBuilder.getSimulationBuilder());
				break;
			// TODO 
			default:
				return;
			}
		}
		GroupLayout l = (GroupLayout) masterFrame.getContentPane().getLayout();
		l.replace(currentView, views.get(vType));
		currentView = views.get(vType);
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

package idynomics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import guiTools.GuiActions;
import guiTools.GuiConsole;
import guiTools.GuiMenu;
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
		
		GRAPH
	}
	
	private static JFrame masterFrame;
	
	private static HashMap<ViewType,JComponent> views;
	
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
		masterFrame = new JFrame();
		/* 
		 * Set the window size, position, title and its close operation.
		 */
		masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		masterFrame.setTitle(Idynomics.fullDescription());
		masterFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		masterFrame.setLocationRelativeTo(null);
		/*
		 * Construct the views and add them to the HashMap.
		 * Need to make the frame visible already, so we can find the size.
		 */
		masterFrame.setVisible(true);
		int width = masterFrame.getWidth();
		int height = masterFrame.getHeight();
		views = new HashMap<ViewType,JComponent>();
		views.put(ViewType.SPLASH, GuiSplash.getSplashScreen(width, height));
		views.put(ViewType.CONSOLE, GuiConsole.getConsole());
		setView(ViewType.SPLASH);
		/* 
		 * Set an action for the button (run the simulation).
		 */
		masterFrame.add(GuiActions.runButton(), BorderLayout.SOUTH);
		/* 
		 * Add the menu bar to the GUI and make everything visible.
		 */
		masterFrame.setJMenuBar(GuiMenu.getMenuBar());
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(0, 0));
		masterFrame.add(p, BorderLayout.NORTH);
		GuiActions.keyBindings(p, masterFrame);
		masterFrame.setVisible(true);
	}
	
	public static void setView(ViewType vType)
	{
		masterFrame.add(views.get(vType));
	}
 }

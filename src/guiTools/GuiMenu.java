/**
 * 
 */
package guiTools;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiMenu
{
	private static JMenuBar menuBar;
	
	public static JMenuBar getMenuBar()
	{
		menuBar = new JMenuBar();
		menuBar.add(fileMenu());
		return menuBar;
	}
	
	
	private static JMenu fileMenu()
	{
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		/* 
		 * Set up the File menu.
		 */
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("File options");
		/*
		 * Add the option of making a new simulation.
		 */
		menuItem = new JMenuItem(new GuiMenu.NewSimulation());
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Make a new simulation");
		menu.add(menuItem);
		/*
		 * Add the option of opening a protocol file.
		 */
		menuItem = new JMenuItem(new GuiMenu.FileOpen());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open existing protocol file");
		menu.add(menuItem);
		/*
		 * Add the option of rendering a compartment.
		 */
		menuItem = new JMenuItem(new GuiMenu.RenderThis());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Render a spatial compartment");
		menu.add(menuItem);
		/*
		 * Output level.
		 */
		menu.addSeparator();
		submenu = new JMenu("OutputLevel");
		submenu.setMnemonic(KeyEvent.VK_L);
		ButtonGroup group = new ButtonGroup();
		for ( Log.Tier t : Log.Tier.values() )
		{
			rbMenuItem = new JRadioButtonMenuItem(new GuiMenu.LogTier(t));
			group.add(rbMenuItem);
			submenu.add(rbMenuItem);
		}
		menu.add(submenu);
		/*
		 * Finally, return the File menu.
		 */
		return menu;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	public static class NewSimulation extends AbstractAction
	{
		private static final long serialVersionUID = 8401347291057616616L;
		
		/**
		 * Action for the file open sub-menu.
		 */
		public NewSimulation()
		{
			super("New simulation");
		}
		
		public void actionPerformed(ActionEvent e)
		{
			// TODO 
			//GuiActions.newSimulation();
		}
	}
	
	public static class FileOpen extends AbstractAction
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
	    	GuiActions.chooseFile();
	    }
	}
	
	public static class RenderThis extends AbstractAction
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
	    	GuiActions.render();
	    }
	}
	
	public static class LogTier extends AbstractAction
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
	
//	public static class GuiView extends AbstractAction
//	{
//		private static final long serialVersionUID = 8725075624293930079L;
//		
//		private ViewType _view;
//		
//		public GuiView(ViewType view)
//		{
//			super(view.toString());
//			this._view = view;
//		}
//		
//		public void actionPerformed(ActionEvent e)
//		{
//			GuiLaunch.setView(this._view);
//		}
//	}
}

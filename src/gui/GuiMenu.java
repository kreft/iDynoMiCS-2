/**
 * 
 */
package gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import analysis.FilteredTable;
import analysis.quantitative.Raster;
import dataIO.Diagram;
import dataIO.DrawMediator;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import idynomics.Simulator;
import idynomics.launchable.SamplerLaunch;
import utility.Helper;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiMenu
{
	private static JMenuBar _menuBar;
	
	public static JMenuBar getMenuBar()
	{
		_menuBar = new JMenuBar();
		_menuBar.add(fileMenu());
		_menuBar.add(interactionMenu());
		return _menuBar;
	}
	
	
	private static JMenu fileMenu()
	{
		JMenu menu, levelMenu;
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
		menuItem = new JMenuItem(new GuiMenu.NewFile());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
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
		
		
		menuItem = new JMenuItem(new GuiMenu.FileDownload());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Download protocol file");
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
		 * NOTE this will not work through the menu bar, instead edit trough
		 * simulation state.		 
			menu.addSeparator();
			levelMenu = new JMenu("OutputLevel");
			levelMenu.setMnemonic(KeyEvent.VK_L);
			ButtonGroup group = new ButtonGroup();
			for ( Log.Tier t : Log.Tier.values() )
			{
				rbMenuItem = new JRadioButtonMenuItem(new GuiMenu.LogTier(t));
				group.add(rbMenuItem);
				levelMenu.add(rbMenuItem);
			}
			menu.add(levelMenu);
		*/
		
		/*
		 * Master protocol sampling
		 */
		menu.addSeparator();
		menuItem = new JMenuItem(new GuiMenu.Sampling());
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Sample master protocol file");
		menu.add(menuItem);
		
		/*
		 * Finally, return the File menu.
		 */
		return menu;
	}
	
	
	private static JMenu interactionMenu()
	{
		JMenu menu;
		JMenuItem menuItem;
		/* 
		 * Set up the File menu.
		 */
		menu = new JMenu("Interact");
		menu.setMnemonic(KeyEvent.VK_G);
		menu.getAccessibleContext().setAccessibleDescription("Interactive");
		/*
		 * Add the option of rendering a compartment.
		 */
		menuItem = new JMenuItem(new GuiMenu.Current());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Edit simulation state");
		menu.add(menuItem);
		/*
		 * Draw to graphics file
		 */
		menuItem = new JMenuItem(new GuiMenu.Draw());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Draw to file");
		menu.add(menuItem);
		/*
		 * Draw raster
		 */
		menuItem = new JMenuItem(new GuiMenu.StructureAnalysis());
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Draw raster to file");
		menu.add(menuItem);
		/*
		 * Draw species diagram
		 */
		menuItem = new JMenuItem(new GuiMenu.SpeciesDiagram());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Request agent information");
		menu.add(menuItem);
		/*
		 * Draw reaction diagram
		 */
		menuItem = new JMenuItem(new ReactionDiagram());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Request agent information");
		menu.add(menuItem);
		/*
		 * Query some agents
		 */
		menuItem = new JMenuItem(new GuiMenu.Query());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Request agent information");
		menu.add(menuItem);
		/*
		 * Query some agents
		 */
		menuItem = new JMenuItem(new GuiMenu.QueryToFile());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Request agent information and store in output folder");
		menu.add(menuItem);
		/*
		 * Finally, return the File menu.
		 */
		return menu;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	public static class NewFile extends AbstractAction
	{
		private static final long serialVersionUID = 8931286266304166474L;
		
		/**
		 * Action for the file open sub-menu.
		 */
		public NewFile()
		{
			super("Make new protocol");
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Idynomics.simulator = new Simulator();
			GuiActions.loadCurrentState();
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
	
	public static class FileDownload extends AbstractAction
	{
		private static final long serialVersionUID = 2247122248926681550L;
		
		/**
		 * Action for the file open sub-menu.
		 */
		public FileDownload()
		{
	        super("Download..");
		}
		
	    public void actionPerformed(ActionEvent e)
	    {
	    	GuiActions.downloadFile(null);
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
	
	public static class SpeciesDiagram extends AbstractAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3011117385035501302L;

		public SpeciesDiagram()
		{
	        super("Species Diagram");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Helper.compartmentAvailable())
			{
				String fileName = "speciesDiagram";
				Diagram diag = new Diagram();
				diag.createCustomFile(fileName);
				diag.speciesDiagram();
				diag.closeFile();
				Log.printToScreen("species diagram created.", false);
			}
		}
		
	}
	
	public static class ReactionDiagram extends AbstractAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3011117385035501302L;

		public ReactionDiagram()
		{
	        super("Reaction Diagram");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Helper.compartmentAvailable())
			{
				String fileName = "reactionDiagram";
				Diagram diag = new Diagram();
				diag.createCustomFile(fileName);
				diag.reactionDiagram( Helper.selectCompartment() );
				diag.closeFile();
				Log.printToScreen("reaction diagram created.", false);
			}
		}
		
	}
	
	public static class Query extends AbstractAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3011117385035501302L;

		public Query()
		{
	        super("Query");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Helper.compartmentAvailable())
			{
				String table = GuiConsole.requestInput("Table logic");
				table = table.replaceAll("\\s+","");
				FilteredTable tab = new FilteredTable(table);
				Log.printToScreen(tab.display(), false);
			}
		}
		
	}
	
	public static class QueryToFile extends AbstractAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3011117385035501302L;

		public QueryToFile()
		{
	        super("QueryToFile");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Helper.compartmentAvailable())
			{
				String table = GuiConsole.requestInput("Table logic");
				table = table.replaceAll("\\s+","");
				FilteredTable tab = new FilteredTable(table);
				tab.toFile();
			}
		}
		
	}
	
	public static class StructureAnalysis extends AbstractAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3011117385035501302L;

		public StructureAnalysis()
		{
	        super("StructureAnalysis");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Helper.compartmentAvailable())
			{
				if ( Helper.selectSpatialCompartment() == null )
					Log.printToScreen("No spatial compartment available.", 
							false );
				{
					Raster raster = new Raster( 
							Helper.selectSpatialCompartment(), true );
					raster.rasterize( Double.valueOf( 
							Helper.obtainInput( null, "Raster scale" ) ) );
					raster.plot( raster.agentMap(), 1.0, 
							Helper.obtainInput( null, "filename") );
				}
			}
		}
		
	}
	
	public static class Draw extends AbstractAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3011117385035501302L;

		public Draw()
		{
	        super("Draw to file");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			DrawMediator.drawState();
		}
		
	}
	
	public static class Current extends AbstractAction
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 3011117385035501302L;

		public Current()
		{
	        super("Edit simulation");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Helper.compartmentAvailable())
				GuiActions.loadCurrentState();
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
	
	public static class Sampling extends AbstractAction
	{
		
		private SamplerLaunch smp = new SamplerLaunch();
		
		public Sampling()
		{
	        super("Sample master");
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			smp.initialize(null);
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

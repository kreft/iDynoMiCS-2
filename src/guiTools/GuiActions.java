/**
 * 
 */
package guiTools;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import dataIO.Log;
import dataIO.Log.Tier;
import glRender.AgentMediator;
import glRender.CommandMediator;
import glRender.Render;
import idynomics.Compartment;
import idynomics.GuiLaunch;
import idynomics.GuiLaunch.ViewType;
import idynomics.Idynomics;
import idynomics.Param;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public final class GuiActions
{
	/*************************************************************************
	 * DEALING WITH FILES
	 ************************************************************************/
	
	/**
	 * \brief Method to select protocol files from a file selection dialog
	 * 
	 * @return XML file selected from the dialog box.
	 */
	public static File chooseFile() 
	{
		/* Open a FileChooser window in the current directory. */
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir")+"/protocol");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(false);
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION )
		{
			return chooser.getSelectedFile();
		}
		else
		{
			return null;
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
	    	File f = chooseFile();
	    	/* Don't crash if the user has clicked cancel. */
	    	if ( f == null )
	    	{
	    		Param.protocolFile = null;
	    		GuiConsole.writeOut("Please choose a protocol file\n");
	    	}
	    	else
	    	{
	    		Param.protocolFile = f.getAbsolutePath();
	    		GuiConsole.writeOut(Param.protocolFile + " \n");
	    	}
	    }
	}
	
	/*************************************************************************
	 * RENDERING IMAGES
	 ************************************************************************/
	
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
	    	if ( Idynomics.simulator == null || 
	    					! Idynomics.simulator.hasSpatialCompartments() )
	    	{
	    		GuiConsole.writeErr("No spatial compartments available!\n");
	    	}
	    	else
	    	{
	    		Compartment c = Idynomics.simulator.get1stSpatialCompartment();
	    		CommandMediator cm = new AgentMediator(c.agents);
	    		Render myRender = new Render(cm);
				EventQueue.invokeLater(myRender);
	    	}
	    }
	}
	
	/*************************************************************************
	 * CHOOSING LOG OUTPUT LEVEL
	 ************************************************************************/
	
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
	
	/*************************************************************************
	 * RUNNING SIMULATION
	 ************************************************************************/
	
	public static JButton runButton()
	{
		JButton launchSim = new JButton("Run!");
		launchSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				GuiLaunch.setView(ViewType.CONSOLE);
				if ( Param.protocolFile != null )
					Idynomics.setupCheckLaunch(Param.protocolFile);
			}
		});
		return launchSim;
	}
	
	// TODO What does this do? When I click enter, nothing happens...
	public static void keyBindings(JPanel p, JFrame frame) 
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

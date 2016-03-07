/**
 * 
 */
package guiTools;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import glRender.AgentMediator;
import glRender.CommandMediator;
import glRender.Render;
import idynomics.Compartment;
import idynomics.GuiLaunch;
import idynomics.Idynomics;
import idynomics.Param;
import idynomics.GuiLaunch.ViewType;

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
	public static void chooseFile() 
	{
		/* Open a FileChooser window in the current directory. */
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir")+"/protocol");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(false);
		File f = null;
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION )
			f = chooser.getSelectedFile();
		
		GuiLaunch.setView(ViewType.CONSOLE);
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
//    		Idynomics.simulator.timer.reset();
    	}
	}
	
	public static void checkProtocol()
	{
		GuiLaunch.setView(ViewType.CONSOLE);
		if ( Param.protocolFile == null )
		{
			GuiConsole.writeErr("Please open a protocol file to check");
		}
		else
		{
			Idynomics.setupSimulator(Param.protocolFile);
			if ( Idynomics.simulator.isReadyForLaunch() )
				GuiConsole.writeOut("Protocol is ready to launch...");
			else
				GuiConsole.writeErr("Problem in protocol file!");
		}
	}
	
	/*************************************************************************
	 * SIMULATION CONTROL
	 ************************************************************************/
	
	public static void runSimulation()
	{
		GuiLaunch.setView(ViewType.CONSOLE);
		if ( Param.protocolFile != null )
			Idynomics.setupCheckLaunch(Param.protocolFile);
	}
	
	public static void pauseSimulation()
	{
		if ( Idynomics.simulator == null )
			return;
		try
		{
			// TODO This doesn't work yet...
			Idynomics.simulator.wait();
		} 
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void stopSimulation()
	{
		if ( Idynomics.simulator == null )
			return;
		// TODO this can probably be made a lot cleaner!
		Idynomics.simulator.timer.setEndOfSimulation(
				Idynomics.simulator.timer.getEndOfCurrentIteration());
	}
	
	/*************************************************************************
	 * RENDERING IMAGES
	 ************************************************************************/
	
	public static void render()
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
	
	/*************************************************************************
	 * MISC
	 ************************************************************************/
	
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

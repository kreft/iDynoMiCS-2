/**
 * 
 */
package gui;

import java.awt.EventQueue;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;

import dataIO.Log;
import idynomics.Compartment;
import idynomics.GuiLaunch;
import idynomics.Idynomics;
import idynomics.Settings;
import idynomics.Simulator;
import render.AgentMediator;
import render.Render;
import utility.ExtraMath;
import utility.Helper;

/**
 * 
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
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
		
    	openFile(f);		
	}
	
	public static void openFile(File f) 
	{
		Idynomics.simulator = new Simulator();
		Idynomics.global = new Settings();
		GuiMain.getConstructor();
    	/* load content if a protocol file has been selected */
    	if ( f == null )
    	{
    		Idynomics.global.protocolFile = null;
    		GuiConsole.writeOut("No protocol file selected.\n");
    	}
    	else
    	{
    		Idynomics.global.protocolFile = f.getAbsolutePath();
    		GuiConsole.writeOut(Idynomics.global.protocolFile + " \n");
    		checkProtocol();
    		GuiLaunch.resetProgressBar();
    	}    		
	}
	
	public static void checkProtocol()
	{
		if ( Idynomics.global.protocolFile == null )
		{
			GuiConsole.writeErr("No protocol file specified.\n");
		} 
		else
		{
			Idynomics.setupSimulator(Idynomics.global.protocolFile);
			if ( Idynomics.simulator.isReadyForLaunch() )
				GuiConsole.writeOut("Protocol is ready to launch...\n");
			else
				GuiConsole.writeErr("Problem in protocol file!\n");
		}
	}
	
	public static void loadCurrentState()
	{	
		GuiMain.update();
	}
	
	/*************************************************************************
	 * SIMULATION CONTROL
	 ************************************************************************/
	
	public static void runSimulation()
	{
		GuiEditor.setAttributes();
		if ( Idynomics.simulator == null )
			Log.printToScreen( "no simulation set.", true);
		else
		{
			Idynomics.simulator.setNode();
			GuiLaunch.resetProgressBar();
			Idynomics.launchSimulator();
		}
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
	 * RENDERING 3D SCENE
	 ************************************************************************/
	
	public static void render()
	{
		/* is the simulator set? */
		if ( Idynomics.simulator == null )
			Log.printToScreen("No simulator available.", false);
		else
		{
			/* identify the spatial compartments */
			List<String> compartments = 
					Idynomics.simulator.getSpatialCompartmentNames();
			Compartment c = null;
			if ( compartments.isEmpty() )
				/* abort if no compartment is available */
				Log.printToScreen("No spatial compartments available.", false);
			else if ( compartments.size() == 1 )
				/* render directly if only 1 compartment is available */
				c = Idynomics.simulator.getCompartment(compartments.get(0));
			else
			{
				/* otherwise ask for user input */
				String s = Helper.obtainInput(compartments, 
						"select compartment for rendering", false);
				c = Idynomics.simulator.getCompartment(s);
			}
			/* create and invoke the renderer */
			Render myRender = new Render( new AgentMediator(c) );
			EventQueue.invokeLater(myRender);
		}
	}
}

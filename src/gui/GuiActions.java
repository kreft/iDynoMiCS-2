/**
 * 
 */
package gui;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dataIO.FileHandler;
import dataIO.FolderOperations;
import dataIO.Log;
import dataIO.Log.Tier;
import expression.Expression;
import expression.arithmetic.Unit;
import idynomics.Global;
import idynomics.Idynomics;
import idynomics.PostProcess;
import idynomics.Simulator;
import render.AgentMediator;
import render.Render;
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
		
	public static File saveFile() 
	{
		boolean confirm = true;
		
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir"));
		File file = new File(System.getProperty("user.dir")+ "/filename");
		chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
		chooser.setCurrentDirectory(file);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(false);
		File f = null;	
		if ( chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION )
			f = chooser.getSelectedFile();
	    if( f.exists() ) 
	    	confirm = Helper.confirmation("Would you like to overwrite: " + 
	    			f.getName());
	    if( confirm )
	    	return f;
	    else
	    	return null;
	}
	
	public static File chooseFile(String relPath, String description) 
	{
		/* Open a FileChooser window in the current directory. */
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir")+"/"+relPath);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(false);
		chooser.setToolTipText(description);
		chooser.setDialogTitle(description);
		File f = null;
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION )
			f = chooser.getSelectedFile();
		return f;	
	}
	
	public static File chooseFolder(String description) 
	{
		/* Open a FileChooser window in the current directory. */
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(false);
		chooser.setToolTipText(description);
		chooser.setDialogTitle(description);
		File f = null;
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION )
			f = chooser.getSelectedFile();
    	return f;
	}
	
	public static File[] chooseMulitple(String description) 
	{
		/* Open a FileChooser window in the current directory. */
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(true);
		chooser.setToolTipText(description);
		chooser.setDialogTitle(description);
		File[] f = null;
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION )
			f = chooser.getSelectedFiles();
		return f;
	}
	
	public static File[] chooseFilesAndFolders(String description) 
	{
		/* Open a FileChooser window in the current directory. */
		JFileChooser chooser = new JFileChooser("" +
				System.getProperty("user.dir"));
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// TODO Allow the user to select multiple files.
		chooser.setMultiSelectionEnabled(true);
		chooser.setToolTipText(description);
		chooser.setDialogTitle(description);
		File[] f = null;
		if ( chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION )
			f = chooser.getSelectedFiles();
		return f;
	}
	
	public static void postProcess()
	{
		File script = chooseFile( "postprocessing", 
				"Select post-processing script" );
		File[] files = chooseFilesAndFolders(
				"Select simulation state files (.exi or .xml)" );
		List<File> finalFiles = null;
		if( FolderOperations.includesfolders(files))
		{
			if( Helper.obtainInput(
					"Would you like to include sub-folders?", false) )
				/* note do look into the first line of folders */
				finalFiles = FolderOperations.getFiles(true, files);
			else
				finalFiles = FolderOperations.getFiles(files);
		}
		else
		{
			finalFiles = FolderOperations.getFiles(true, files);
		}
		if( Helper.obtainInput( "Would you like to continue processing " +
				finalFiles.size() + " files?", false) )
		{
			Idynomics.postProcess = new PostProcess(script, finalFiles);
			Idynomics.runPostProcess();
		}
		else
		{
			Log.out("post-processing cancelled by user");
		}
	}
	
	public static void openFile(File f) 
	{
		Idynomics.simulator = new Simulator();
		Idynomics.global = new Global();
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
    		GuiButtons.resetProgressBar();
    		GuiActions.loadCurrentState();
    		GuiMain.setStatus( Idynomics.global.protocolFile );
    	}    		
	}
	
	public static String inputUrl()
	{  
		JFrame f;  
	    f=new JFrame();   
	    return JOptionPane.showInputDialog(f,"Enter URL");
	}
	
	public static void downloadFile(String url)
	{
		String local = "tmp_dl.xml";
		FileHandler handler = new FileHandler();
		
		if (url == null)
			url = inputUrl();
		
		Log.out("Downloading file: " + url + " -> " + local );
		handler.fnew(local);
		InputStream webIS = null;
		FileOutputStream fo = null;
		URL urly = null;
		
		try {
			urly = new URL(url); 
		} catch ( MalformedURLException e) {
			Log.out(Tier.NORMAL, "unable to aquire protocol from url: " + url);
		}
		
		
		try {
			webIS = urly.openStream();
			int c = 0;
			do {
			    c = webIS.read();
			    if (c !=-1) {
			    	handler.write((byte) c);
			    }
			} while(c != -1);
			webIS.close();
			handler.fclose();
			Log.out("finished Download");
			File in = new File(local);
			openFile(in);
		} catch (IOException | NullPointerException e) {
			Log.out(Tier.NORMAL, "File download failed");
		}
	}
	
		public static void saveToFile(File f) 
	{
    	if ( f == null )
    	{
    		GuiConsole.writeOut("Saving canceled.\n");
    	}
    	else
    	{
    		Idynomics.simulator.saveSimulationState(f.getAbsolutePath(), 
    				Helper.confirmation("Would you like to compress to exi format?"));
    	}    		
	}
	
	public static void convertFiles() 
	{
		File[] files = chooseFilesAndFolders(
				"Select simulation state files (.exi or .xml)" );
		List<File> finalFiles = null;
		if( FolderOperations.includesfolders(files))
		{
			if( Helper.obtainInput(
					"Would you like to include sub-folders?", false) )
				/* note do look into the first line of folders */
				finalFiles = FolderOperations.getFiles(true, files);
			else
				finalFiles = FolderOperations.getFiles(files);
		}
		else
		{
			finalFiles = FolderOperations.getFiles(true, files);
		}
		if( Helper.obtainInput( "Would you like to continue processing " +
				finalFiles.size() + " files?", false) )
		{
			for( File f : finalFiles )
			{
				boolean exi = false;
				String out = null;
				String path = f.getAbsolutePath();
				if( path.toLowerCase().contains(".xml"))
				{
					exi = true;
					out = path.toLowerCase().replaceAll(".xml", ".exi");
				} else if( path.toLowerCase().contains(".exi"))
				{
					exi = false;
					out = path.toLowerCase().replaceAll(".exi", ".xml");
				}
				Idynomics.setupSimulator( f.getAbsolutePath() );
				Idynomics.simulator.saveSimulationState(out, exi);
				Idynomics.simulator = new Simulator();
			}
		}
		else
		{
			Log.out("post-processing cancelled by user");
		} 		
	}

	public static void convertUnits()
	{
		Log.out( " -- Unit conversion assistant --");
		String input = Helper.obtainInput("", "input value (fx: 1 [m/s])", false, true);
		if( Helper.isNullOrEmpty(input))
		{
			Log.out( "Canceled by user");
			return;
		}
		Expression e = new Expression(input);
		Log.out("Input: \t\t"+ input);
		Log.out("SI: \t\t"+ e.getValue(true) * e.getUnit().modifier() + " [" + e.getUnit().unit() + "]" );
		Log.out("iDynoMiCS base units: \t" + e.getValue(false) );
		String format = Helper.obtainInput( "", "output format (fx: um/min)", false, true);
		if( !Helper.isNullOrEmpty(format))
			Log.out( e.getValue( format ) + " " + format );
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
				GuiConsole.writeOut("Protocol loaded successfully.\n");
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
			GuiButtons.resetProgressBar();
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
		Log.out("simulation stopping by user request...");
		Idynomics.simulator.stopAction = true;
	}
	
	/*************************************************************************
	 * RENDERING 3D SCENE
	 ************************************************************************/
	
	public static void render()
	{
		/* is the simulator set? */
		if ( Idynomics.simulator == null )
			Log.printToScreen("No simulator available.", false);
		else if ( Helper.selectSpatialCompartment() == null )
			Log.printToScreen("No spatial compartment available.", false);
		else
		{
			/* create and invoke the renderer */
			Render myRender = new Render( 
					new AgentMediator( Helper.selectSpatialCompartment() ) );
			EventQueue.invokeLater(myRender);
		}
	}
}

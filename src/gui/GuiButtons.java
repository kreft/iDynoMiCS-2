package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import idynomics.Global;
import idynomics.Idynomics;
import idynomics.Simulator;
import referenceLibrary.XmlRef;
import utility.Helper;

/**
 * 	
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class GuiButtons {
	
	private static JPanel buttonPane = new JPanel();

	private static JProgressBar _progressBar;
	
	/**
	 * TODO clean-up and comment
	 */
	public static void drawButtons() 
	{
		JButton button;
		/* Check the simulation. */
		button = GuiSimControl.openButton();
		buttonPane.add(button);
		
		/* new simulation */
		button = new JButton("new");
		{
			button.addActionListener(new ActionListener()
			{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Idynomics.simulator = new Simulator();
				Idynomics.global = new Global();
				GuiMain.getConstructor();
				if ( ! Idynomics.global.ignore_protocol_out )
				{
					Idynomics.global.outputRoot = Helper.obtainInput(
							Idynomics.global.outputRoot, "Required " + 
							XmlRef.outputFolder, true);
				}
				GuiEditor.addComponent( Idynomics.simulator.getModule(), 
						GuiMain.tabbedPane);
			}
		}
		);
		}
		buttonPane.add(button);
		
		/* Run the simulation. */
		button = GuiSimControl.runButton();
		buttonPane.add(button);

		/* Stop the simulation. */
		button = GuiSimControl.stopButton();

		buttonPane.add(button);

		///////////////////////////////////////////////////////////////////////
		/* Add a progress bar to the button row. */
		_progressBar  = new JProgressBar();
		_progressBar.setStringPainted(true);
		buttonPane.add(_progressBar);
		
		/* Add a checkbox for the GuiConsole autoscrolling. */
		JCheckBox autoscroll = GuiConsole.autoScrollCheckBox();
		buttonPane.add(autoscroll);

	}
	
	/**
	 * \brief Reset the simulation progress bar to 0%.
	 */
	public static void resetProgressBar()
	{
		_progressBar.setMinimum(0);
		_progressBar.setValue((int) Idynomics.simulator.timer.getCurrentTime());
		_progressBar.setMaximum((int) Idynomics.simulator.timer.getEndOfSimulation());
	}
	
	/**
	 * \brief Move the simulation progress bar along with the Timer. 
	 */
	public static void updateProgressBar()
	{
		_progressBar.setValue((int) Idynomics.simulator.timer.getCurrentTime());
	}
	
	public static JPanel getButtons()
	{
		drawButtons();
		return buttonPane;
	}
}

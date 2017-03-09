package gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import idynomics.Idynomics;
import idynomics.Settings;
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
		GroupLayout _layout = new GroupLayout(buttonPane);
		_layout.setAutoCreateGaps(true);
		_layout.setAutoCreateContainerGaps(true);
		SequentialGroup _verticalLayoutGroup = _layout.createSequentialGroup();
		ParallelGroup _horizontalLayoutGroup = _layout.createParallelGroup();

		/*
		 * Just below the menu bar, make the bar of simulation control buttons.
		 */
		SequentialGroup buttonHoriz = _layout.createSequentialGroup();
		ParallelGroup buttonVert = _layout.createParallelGroup();
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
				Idynomics.global = new Settings();
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
		buttonHoriz.addComponent(button);
		buttonVert.addComponent(button);
		
		/* Run the simulation. */
		button = GuiSimControl.runButton();
		buttonHoriz.addComponent(button);
		buttonVert.addComponent(button);

		/* Stop the simulation. */
		button = GuiSimControl.stopButton();
		buttonHoriz.addComponent(button);
		buttonVert.addComponent(button);

		///////////////////////////////////////////////////////////////////////
		/* Add a progress bar to the button row. */
		_progressBar  = new JProgressBar();
		_progressBar.setStringPainted(true);
		buttonHoriz.addComponent(_progressBar);
		buttonVert.addComponent(_progressBar);
		/* Add a checkbox for the GuiConsole autoscrolling. */
		JCheckBox autoscroll = GuiConsole.autoScrollCheckBox();
		buttonHoriz.addComponent(autoscroll);
		buttonVert.addComponent(autoscroll);
		/* Add these to the layout. */
		_verticalLayoutGroup.addGroup(buttonVert);
		_horizontalLayoutGroup.addGroup(buttonHoriz);
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

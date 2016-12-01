/**
 * 
 */
package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;


/**
 * \brief Helper class of buttons for the GUI.
 * 
 * <p>No button actions should be specified here. Instead, button actions
 * should call on the corresponding methods in GuiActions. This is so that we
 * can have identical actions in buttons and in the menu bar.</p>
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiSimControl
{
	public static JButton openButton()
	{
		JButton openProtocol = new JButton("Open");
		openProtocol.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				GuiActions.chooseFile();
			}
		});
		return openProtocol;
		
	}
	
	public static JButton runButton()
	{
		JButton launchSim = new JButton("Run!");
		launchSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				GuiActions.runSimulation();	
			}
		});
		return launchSim;
	}
	
	public static JButton pauseButton()
	{
		JButton pauseSim = new JButton("Pause");
		pauseSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				GuiActions.pauseSimulation();
			}
		});
		return pauseSim;
	}
	
	public static JButton stopButton()
	{
		JButton stopSim = new JButton("Stop");
		stopSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				GuiActions.stopSimulation();
			}
		});
		return stopSim;
	}
}

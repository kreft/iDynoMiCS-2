/**
 * 
 */
package guiTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import idynomics.GuiLaunch;
import idynomics.GuiLaunch.ViewType;
import idynomics.Idynomics;
import idynomics.Param;
import idynomics.Timer;

/**
 * @author cleggrj
 *
 */
public final class GuiSimControl
{
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
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
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public static JButton pauseButton()
	{
		JButton pauseSim = new JButton("Pause");
		pauseSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
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
		});
		return pauseSim;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public static JButton stopButton()
	{
		JButton stopSim = new JButton("Stop");
		stopSim.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if ( Idynomics.simulator == null )
					return;
				Timer.setEndOfSimulation(Timer.getEndOfCurrentIteration());
			}
		});
		return stopSim;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public static JButton checkButton()
	{
		JButton checkProtocol = new JButton("Check");
		checkProtocol.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
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
		});
		return checkProtocol;
	}
}

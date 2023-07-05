package gui;


import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import idynomics.Idynomics;
import idynomics.launchable.GuiLaunch;


/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiMain
{
	public static JTabbedPane tabbedPane = GuiComponent.newPane();
	
	public static JLabel _statusLabel = null;
		
	final static int CONSOLEPANE = 0;
	
	public static JComponent getConstructor() 
	{
		return (JComponent) GuiComponent.setPane(tabbedPane);
	}
		
	public static void update()
	{
		if (GuiLaunch.classicGui())
		{
			GuiMain.getConstructor();
		}
		GuiEditor.addComponent(Idynomics.simulator.getModule(), 
				GuiMain.tabbedPane);
	}
	
	public static JPanel newStatusBar()
	{

		// create the status bar panel and shove it down the bottom of the frame
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 3, 3));
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		_statusLabel = new JLabel("...");
		_statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(_statusLabel);
		
		return statusPanel;
	}

	public static void setStatus( String status )
	{
		_statusLabel.setText( status );
	}
}

package guiTools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import idynomics.GuiLaunch;
import idynomics.Idynomics;
import idynomics.Simulator;
import idynomics.GuiLaunch.ViewType;


public class GuiConstruct {

	public static JComponent getConstructor() {

		
		/* The tabs pane */
		JPanel panel = new JPanel();
		JTabbedPane tabbedPane = new JTabbedPane();
		
		/* simulator pane */
		JPanel simulatorPane = new JPanel();
		
		/* compartments */
		JPanel compartmentPane = new JPanel();
		compartmentPane.add(textPanel("compartments"));
				
		/* start pane */
		JPanel startPane = new JPanel();
		
		/* simulation pane */
		simulatorPane.add(textPanel("Timer settings"));
		
		JTextArea timestep = new JTextArea();
		simulatorPane.add(inputPanel("time step size", timestep));
		
		JTextArea timeend = new JTextArea();
		simulatorPane.add(inputPanel("end of simulation", timeend));
		
		simulatorPane.add(actionButton("set timer", "set", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Idynomics.simulator.timer.setTimeStepSize(Double.valueOf(
						timestep.getText()));
				Idynomics.simulator.timer.setEndOfSimulation(Double.valueOf(
						timeend.getText()));
			}
		}
		));
		
		simulatorPane.add(actionButton("load current timer settings", "load", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				try
				{
					timestep.setText(String.valueOf(Idynomics.simulator.timer.getTimeStepSize()));
					timeend.setText(String.valueOf(Idynomics.simulator.timer.getEndOfSimulation()));
				}
				catch(NullPointerException e)
				{
//					timestep.setText("1");
//					timeend.setText("100");
				}	
			}
		}
		));
		
		
		/* compartment pane content */
		
		JComboBox box = new JComboBox(new String[]{});
		compartmentPane.add(selectPanel(box));
		
		JTextArea comp = new JTextArea();
		compartmentPane.add(inputPanel("compartment name", comp));
		
		compartmentPane.add(actionButton("add compartment", "add", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Idynomics.simulator.addCompartment(comp.getText());
				box.insertItemAt(comp.getText(), box.getItemCount());
				box.setSelectedIndex(box.getItemCount()-1);
			}
		}
		));
		
		compartmentPane.add(actionButton("refresh compartment list", "refresh", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				try
				{
					int i = 0;
					box.removeAllItems();
					for(String name : Idynomics.simulator.getCompartmentNames())
						box.insertItemAt(name, i++);
				}
				catch(NullPointerException e)
				{

				}	
			}
		}
		));
		
		/* start pane content */
		
		startPane.add(actionButton("Open from file", "open", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				GuiActions.chooseFile();
				try
				{
				tabbedPane.setEnabledAt(1, true);
				timestep.setText(String.valueOf(Idynomics.simulator.timer.getTimeStepSize()));
				timeend.setText(String.valueOf(Idynomics.simulator.timer.getEndOfSimulation()));
				tabbedPane.setEnabledAt(2, true);
				tabbedPane.setEnabledAt(3, true);
				int i = 0;
				for(String name : Idynomics.simulator.getCompartmentNames())
					box.insertItemAt(name, i++);
				box.setSelectedIndex(0);
				GuiLaunch.setView(ViewType.SIMCONSTRUCT);
			}
			catch(NullPointerException e)
			{

			}	
			}
		}
		));
	
		startPane.add(actionButton("Construct new simulation", "construct", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Idynomics.simulator = new Simulator();
				tabbedPane.setEnabledAt(1, true);
				tabbedPane.setEnabledAt(2, true);
				tabbedPane.setEnabledAt(3, true);
			}
		}
		));

		/* the tabs */
		
		tabbedPane.addTab("start", null, startPane,
                "Does nothing");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		tabbedPane.addTab("Simulator", null, simulatorPane,
		                  "Does nothing");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.setEnabledAt(1, false);

		JComponent panel2 = textPanel("Species Library");
		tabbedPane.addTab("Species Library", null, panel2,
		                  "Does twice as much nothing");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		tabbedPane.setEnabledAt(2, false);

		tabbedPane.addTab("Compartments", null, compartmentPane,
		                  "Still does nothing");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		tabbedPane.setEnabledAt(3, false);
		
		panel.add(tabbedPane);
		return (JComponent) tabbedPane;
	}
	
	protected static JPanel inputPanel(String description, JTextArea inputArea)
	{
		JPanel panel = new JPanel();
		panel.setBounds(20, 10, 20, 10);
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setPreferredSize(new Dimension(200,30));
		panel.add(descriptionLabel,BorderLayout.WEST);
		
		inputArea.setPreferredSize(new Dimension(400,30));
		panel.add(inputArea,BorderLayout.EAST);
		return panel;
	}
	
	protected static JComponent textPanel(String text) {
        JPanel panel = new JPanel();
        panel.setBounds(20, 10, 20, 10);
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
        JLabel filler = new JLabel(text);
        filler.setPreferredSize(new Dimension(600,30));
        panel.add(filler,BorderLayout.CENTER);
        return panel;
    }
	
	protected static JComponent selectPanel(JComboBox box) {
        JPanel panel = new JPanel();
        panel.setBounds(20, 10, 20, 10);
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		box.setPreferredSize(new Dimension(600,30));
        panel.add(box,BorderLayout.CENTER);
        return panel;
    }
	
	protected static JComponent actionButton(String description, String text, ActionListener actionListner)
	{
		JPanel panel = new JPanel();
        panel.setBounds(20, 10, 20, 10);
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel filler = new JLabel(description);
        filler.setPreferredSize(new Dimension(500,30));
        panel.add(filler,BorderLayout.WEST);
        
		JButton action = new JButton(text);
		action.setPreferredSize(new Dimension(100,30));
		action.addActionListener(actionListner);
		panel.add(action,BorderLayout.EAST);
		return panel;
	}

}
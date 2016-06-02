package guiTools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import idynomics.Compartment;
import idynomics.Idynomics;
import shape.Shape;

/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiSimConstruct
{
	
	protected static boolean _insidePanel = false;
	
	protected static JComponent _component = setComponent();
	
	protected static JTabbedPane _tabbedPane;
	
	final static int CONSOLEPANE = 0;
	
	final static int STARTPANE = 1;
	
	final static int SIMULATORPANE = 2;
	
	final static int SPECIESPANE = 3;
	
	final static int COMPARTMENTPANE = 4;

	public static JComponent getConstructor() 
	{
		return _component;
	}
	/*
	 * The JComponent set in the gui
	 */
	public static JComponent setComponent() {
		
		/* The tabs pane */
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		_tabbedPane = new JTabbedPane();

		/* simulator pane */
		JPanel simulatorPane = new JPanel();
		simulatorPane.setLayout(new WrapLayout(FlowLayout.CENTER, 5, 5));
		
		/* species pane */
		JPanel speciesPane = new JPanel();
		speciesPane.setLayout(new WrapLayout(FlowLayout.CENTER, 5, 5));
		
		/* compartments */
		JPanel compartmentPane = new JPanel();
		compartmentPane.setLayout(new WrapLayout(FlowLayout.CENTER, 5, 5));

		
		/*
		 *  simulation pane content 
		 */
		simulatorPane.add(textPanel("Timer settings"));
		
		JTextArea timestep = new JTextArea();
		simulatorPane.add(inputPanel("time step size", timestep));
		
		JTextArea timeend = new JTextArea();
		simulatorPane.add(inputPanel("end of simulation", timeend));
		
		simulatorPane.add(actionButton("set timer", new JButton("set"), new ActionListener()
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
		
		simulatorPane.add(actionButton("load current timer settings", new JButton("load"), new ActionListener()
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
		
		/* 
		 * species lib pane content 
		 */
		JComboBox species = new JComboBox();
		speciesPane.add(selectPanel(species));
		
		speciesPane.add(actionButton("refresh species list", new JButton("refresh"), new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				try
				{
					int i = 0;
					species.removeAllItems();
					for(String name : Idynomics.simulator.speciesLibrary.getAllSpeciesNames())
						species.insertItemAt(name, i++);
				}
				catch(NullPointerException e)
				{

				}	
			}
		}
		));
		
		
		/* 
		 * compartment pane content 
		 */
		compartmentPane.add(textPanel("compartments"));
		
		JComboBox box = new JComboBox(new String[]{});
		compartmentPane.add(selectPanel(box));
		
		
		compartmentPane.add(textPanel("Adding compartments"));
		JTextArea comp = new JTextArea("compartment name");
		compartmentPane.add(inputPanel("", comp));

		JComboBox shape = new JComboBox(Shape.getAllOptions());
		compartmentPane.add(selectPanel(shape));
		
		compartmentPane.add(actionButton("add compartment", new JButton("add"), new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Compartment c = Idynomics.simulator.addCompartment(comp.getText());
				c.setShape(Shape.getNewInstance(shape.getSelectedItem().toString()));
				box.insertItemAt(comp.getText(), box.getItemCount());
				box.setSelectedIndex(box.getItemCount()-1);
			}
		}
		));
		
		compartmentPane.add(actionButton("", new JButton("refresh"), new ActionListener()
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
		

		/* 
		 * the tabs 
		 * TODO: disable tabs while simulation is running
		 */
		// TODO alternative to having views hidden in menu bar
		_tabbedPane.addTab("console", null, GuiConsole.getConsole(),
              "The Console");

		_tabbedPane.addTab("Simulator", null, simulatorPane,
		                  "Simulator settings");

		_tabbedPane.addTab("Species Library", null, speciesPane,
		                  "Species Library");

		_tabbedPane.addTab("Compartments", null, compartmentPane,
		                  "The compartments");
		
		tabEnabled(_tabbedPane, simulatorPane, false);
		tabEnabled(_tabbedPane, speciesPane, false);
		tabEnabled(_tabbedPane, compartmentPane, false);
		
		panel.add(_tabbedPane);
		if ( _insidePanel )
			return panel;
		else
			return (JComponent) _tabbedPane;
		
	}
	
	public static void togglePane(int paneNumber)
	{
		_tabbedPane.setSelectedIndex(paneNumber);
	}
	
	public static void tabEnabled(int paneNumber, boolean bool)
	{
		_tabbedPane.setEnabledAt(paneNumber, bool);
	}
	
	public static void tabEnabled(JTabbedPane tabbedPane, Component component, boolean bool)
	{
		tabbedPane.setEnabledAt(findComponentIndex(tabbedPane, component), bool);
	}
	
	public static int findComponentIndex(JTabbedPane tabbedPane, Component component)
	{
		int totalTabs = tabbedPane.getTabCount();
		for(int i = 0; i < totalTabs; i++)
		{
		   Component c = tabbedPane.getComponentAt(i);
		   if(c.equals(component))
			   return i;
		}
		return -1;
	}
	
	/*
	 * return a formated JPanel with textPanel and with description
	 */
	protected static JPanel inputPanel(String description, JTextArea inputArea)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setPreferredSize(new Dimension(200,30));
		panel.add(descriptionLabel,BorderLayout.WEST);
		
		inputArea.setPreferredSize(new Dimension(400,30));
		panel.add(inputArea,BorderLayout.EAST);
		return panel;
	}
	
	/*
	 * return a formated JPanel with JLabel
	 */
	protected static JComponent textPanel(String text) {
        JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
        JLabel filler = new JLabel(text);
        filler.setPreferredSize(new Dimension(600,30));
        panel.add(filler,BorderLayout.CENTER);
        return panel;
    }
	
	/*
	 * return a formated JPanel with Combobox
	 */
	protected static JComponent selectPanel(JComboBox box) {
        JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		box.setPreferredSize(new Dimension(600,30));
        panel.add(box,BorderLayout.CENTER);
        return panel;
    }
	
	/*
	 * return a formated JPanel with JButton, eventListner and description
	 */
	protected static JComponent actionButton(String description, JButton actionButton, ActionListener actionListner)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel filler = new JLabel(description);
        filler.setPreferredSize(new Dimension(500,30));
        panel.add(filler,BorderLayout.WEST);

		actionButton.setPreferredSize(new Dimension(100,30));
		actionButton.addActionListener(actionListner);
		panel.add(actionButton,BorderLayout.EAST);
		return panel;
	}

}

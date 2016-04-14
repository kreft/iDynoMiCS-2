package guiTools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiMain
{
		
	protected static JComponent component = setComponent();
	
	protected static JTabbedPane tabbedPane;
	
	final static int CONSOLEPANE = 0;
	
	public static JComponent getConstructor() 
	{
		return component;
	}
	/*
	 * The JComponent set in the gui
	 */
	public static JComponent setComponent() {
		
		/* The tabs pane */
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		tabbedPane = new JTabbedPane();

		addTab("console", GuiConsole.getConsole(),
              "The Console");

		panel.add(tabbedPane);
		return (JComponent) tabbedPane;
		
	}
	
	public static void addTab(String tag, Component component,  String tooltip)
	{
		tabbedPane.addTab(tag, null, component, tooltip);
	}
	
	public static void togglePane(int paneNumber)
	{
		tabbedPane.setSelectedIndex(paneNumber);
	}
	
	public static void tabEnabled(int paneNumber, boolean bool)
	{
		tabbedPane.setEnabledAt(paneNumber, bool);
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
	public static JPanel inputPanel(String description, JTextArea inputArea)
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
	public static JComponent textPanel(String text) {
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
	public static JComponent selectPanel(JComboBox box) {
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
	public static JComponent actionButton(String description, JButton actionButton, ActionListener actionListner)
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

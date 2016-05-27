package guiTools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;

public class GuiComponent {
	/*
	 * The JComponent set in the gui
	 */
	public static JTabbedPane setPane(JTabbedPane pane) {
		
		pane.removeAll();
		
		/* The tabs pane */
		addTab(pane, "console", GuiConsole.getConsole(),
              "The Console");

		pane.setTabPlacement(JTabbedPane.LEFT);
		return pane;
		
	}
	
	public static JTabbedPane newPane() {
		/* a tabs pane */
		JTabbedPane tabbedPane = new JTabbedPane();
		return tabbedPane;
	}
	
	public static void addTab(JTabbedPane tabbedPane, String tag, Component component, String tooltip)
	{
		tabbedPane.addTab(tag, null, component, tooltip);
	}
	
	public static void togglePane(JTabbedPane tabbedPane, int paneNumber)
	{
		tabbedPane.setSelectedIndex(paneNumber);
	}
	
	public static void tabEnabled(JTabbedPane tabbedPane, int paneNumber, boolean bool)
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
		descriptionLabel.setPreferredSize(new Dimension(180,25));
		panel.add(descriptionLabel,BorderLayout.WEST);
	
		inputArea.setPreferredSize(new Dimension(400,25));
		inputArea.setFont(Font.getFont("verdana"));
		panel.add(inputArea,BorderLayout.EAST);
		return panel;
	}
	
	public static JPanel inputPanelLarge(String description, JTextArea inputArea)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setPreferredSize(new Dimension(200,25));
		panel.add(descriptionLabel,BorderLayout.NORTH);
		
		inputArea.setPreferredSize(new Dimension(580,300));
		inputArea.setLineWrap(true);
		inputArea.setFont(Font.getFont("verdana"));
		panel.add(inputArea,BorderLayout.SOUTH);
		return panel;
	}
	
	public static JComponent Spinner(int start, int selection, int end, int increment)
	{

		JSpinner spinner = new JSpinner( new SpinnerNumberModel( selection,start,end,increment ) );
		return spinner;
	}
	
	public static JComponent Spinner(int end)
	{
		return Spinner(1,1,end,1);
	}
	
	/*
	 * return a formated JPanel with JLabel
	 */
	public static JComponent textPanel(String text) {
        JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
        JLabel filler = new JLabel(text);
        filler.setPreferredSize(new Dimension(580,30));
        panel.add(filler,BorderLayout.CENTER);
        return panel;
    }
	
	/*
	 * return a formated JPanel with Combobox
	 */
	public static JComponent selectPanel(String description, JComboBox box) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setPreferredSize(new Dimension(180,30));
		panel.add(descriptionLabel,BorderLayout.WEST);
		
		box.setPreferredSize(new Dimension(400,30));
		panel.add(box,BorderLayout.EAST);
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
        filler.setPreferredSize(new Dimension(480,30));
        panel.add(filler,BorderLayout.WEST);

		actionButton.setPreferredSize(new Dimension(100,30));
		actionButton.addActionListener(actionListner);
		panel.add(actionButton,BorderLayout.EAST);
		return panel;
	}

}

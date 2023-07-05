package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import settable.Module;

public class GuiComponent {
	

	private final static String ICON_PATH = "icons/iDynoMiCS_logo_icon.png";
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
	
	public static void togglePane(JTabbedPane tabbedPane, Component component)
	{
		tabbedPane.setSelectedIndex( findComponentIndex(tabbedPane, component));
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
		panel.setSize(600, 25);
		
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
		panel.setSize(600, 25);
		
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
		panel.setSize(600, 25);
        JLabel filler = new JLabel(text);
        filler.setPreferredSize(new Dimension(580,25));
        panel.add(filler,BorderLayout.CENTER);
        return panel;
    }
	
	/*
	 * return a formated JPanel with JLabel
	 */
	public static JComponent textPanel(String text, int option) {
        JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 25);
        JLabel filler = new JLabel(text);
        if ( option == 1)
        {
	        filler.setFont(new Font(null, Font.BOLD, 14));
	        filler.setForeground(new Color(0f, 0f, 0.5f));
        }
        filler.setPreferredSize(new Dimension(580,25));
        panel.add(filler,BorderLayout.CENTER);
        return panel;
    }
	
	/*
	 * return a formated JPanel with Combobox
	 */
	public static JComponent selectPanel(String description, 
			JComboBox<String> box) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 25);
		
		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setPreferredSize(new Dimension(180,25));
		panel.add(descriptionLabel,BorderLayout.WEST);
		
		box.setPreferredSize(new Dimension(400,25));
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
		panel.setSize(600, 25);
		
		JLabel filler = new JLabel(description);
        filler.setPreferredSize(new Dimension(478,25));
        panel.add(filler,BorderLayout.WEST);

		actionButton.setPreferredSize(new Dimension(100,25));
		actionButton.addActionListener(actionListner);
		panel.add(actionButton,BorderLayout.EAST);
		return panel;
	}
	
	public static JComponent frameOnClick(Module node, String titel)
	{
		JButton button = new JButton(titel);
		ActionListener actionListner = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{

				JFrame frame = new JFrame ("descr");
				
				/* set the frame's initial position and make it visable */
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				
				/* add aditional 0 by 0 JPanel with key bindings */
				JPanel p = new JPanel();
				p.setPreferredSize(new Dimension(0,0));
				frame.add(p, BorderLayout.SOUTH);
				
				/* set the icon */
				ImageIcon img = new ImageIcon(ICON_PATH);
				frame.setIconImage(img.getImage());
				
				JTabbedPane tabs = GuiComponent.newPane();
				
				tabs.setUI(new BasicTabbedPaneUI() {
			        private final Insets borderInsets = new Insets(0, 0, 0, 0);
			        @Override
			        protected void paintContentBorder(Graphics g, int tabPlacement, 
			        		int selectedIndex) {
			        }
			        @Override
			        protected Insets getContentBorderInsets(int tabPlacement) {
			            return borderInsets;
			        }
			    });
				
				JScrollPane scrollPane = new JScrollPane();
				
				JPanel component = new JPanel();
				component.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));
				scrollPane.add(component);
				tabs.addTab(node.getTag(), scrollPane);
				JPanel attr = new JPanel();
				attr.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
				attr.add(GuiComponent.textPanel(node.getTag() + " " + node.getTitle(), 1));
				
				component.add(attr);
				scrollPane.setViewportView(component);
				
				frame.add(tabs);
				
			}
		};


		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 25);
		
		JLabel filler = new JLabel(titel);
        filler.setPreferredSize(new Dimension(478,25));
        panel.add(filler,BorderLayout.WEST);

		button.setPreferredSize(new Dimension(100,25));
		button.addActionListener(actionListner);
		panel.add(button,BorderLayout.EAST);
		
		return panel;
	}
}

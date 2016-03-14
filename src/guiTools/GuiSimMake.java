package guiTools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import idynomics.Simulator;
import modelBuilder.InputSetter;
import modelBuilder.IsSubmodel;
import modelBuilder.ParameterSetter;
import modelBuilder.SubmodelMaker;

/**
 * \brief TODO
 * 
 * <p>NOTE: Combines the approaches developed in ConsoleSimBuilder and in
 * GuiSimConstruct.</p>
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class GuiSimMake
{
	protected JFrame frame;
	
	public void makeNewSimulation()
	{
		Simulator sim = new Simulator();
		frame = getConstructor(sim);
		// TODO save to XML button?
		// TODO run simulation button?
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("test");
		frame.setSize(800, 800);
		frame.setVisible(true);
	}
	
	public JFrame getConstructor(IsSubmodel aSubmodel) 
	{
		JFrame out = new JFrame();
		//out.setContentPane(setComponent(aSubmodel));
		out.add(setComponent(aSubmodel));
		return out;
	}
	
	/**
	 * \brief The JComponent set in the GUI
	 */
	public JComponent setComponent(IsSubmodel aSubmodel)
	{
		/* Info about the submodel. */
		String subName = aSubmodel.getClass().getSimpleName();
		List<InputSetter> inputs = aSubmodel.getRequiredInputs();
		
		/* The tabs pane */
		JTabbedPane tabbedPane = new JTabbedPane();
		
		/* Pane for the current submodel. */
		JPanel mainPane = newTab();
		mainPane.add(textPanel(subName));
		tabbedPane.add(subName, mainPane);
		tabEnabled(tabbedPane, mainPane, true);
		JButton saveButton = new JButton("Save");
		
		
		for ( InputSetter aSetter : inputs )
		{
			String inputName = aSetter.getName();
			System.out.println("Looking at "+inputName);
			if ( aSetter instanceof SubmodelMaker )
			{
				SubmodelMaker smMaker = (SubmodelMaker) aSetter;
				/* Make the tab for this maker. */
				JPanel smPane = getTabFor(smMaker);
				tabbedPane.addTab(inputName, null, smPane, inputName);
				tabEnabled(tabbedPane, smPane, true);
			}
			else if ( aSetter instanceof ParameterSetter )
			{
				/* Add an input box for this parameter. */
				JTextArea inputArea = new JTextArea();
				mainPane.add(inputPanel(inputName, inputArea));
				/* Tell the save button to set this parameter when clicked. */
				saveButton.addActionListener(
						aSetter.getActionListener(inputArea.getText()));
			}
			else
			{
				// TODO safety? Similar to ParameterSetter?
			}
		}
		/* Add the save button to the main pane only if it does anything. */
		if ( saveButton.getActionListeners().length > 0 )
			mainPane.add(saveButton);
		
		return tabbedPane;
	}
	
	
	
	
	/**************************************************************************
	 * MAKER-TAB METHODS
	 *************************************************************************/
	
	protected JPanel getTabFor(SubmodelMaker smMaker)
	{
		JPanel smTab = newTab();
		String smName = smMaker.getName();
		smTab.add(textPanel(smName));
//		if ( smMaker.mustMakeMore() )
//		{
//			makeMaker(smTab, smMaker);
//		}
		if ( smMaker.canMakeMore() )
		{
			makeMaker(smTab, smMaker);
		}
		return smTab;
	}
	
	
	protected void makeMaker(JPanel smTab, SubmodelMaker smMaker)
	{
		
		JButton makeButton = new JButton("Make new "+smMaker.getName());
		Object options = smMaker.getOptions();
		if ( options == null )
		{
			//makeButton.addActionListener(smMaker.getActionListener(null));
			smMaker.actionPerformed(null);
			IsSubmodel sm = smMaker.getLastMadeSubmodel();
			smTab.add(setComponent(sm));
		}
		else if ( options instanceof String[] )
		{
			String[] strOptions = (String[]) options;
			JComboBox<String> selecter = new JComboBox<String>(strOptions);
			smTab.add(selectPanel(selecter));
			makeButton.addActionListener(
					smMaker.getActionListener(
							selecter.getSelectedItem().toString()));
		}
		else
		{
			// TODO
		}
	}
	
	/**************************************************************************
	 * HELPER METHODS
	 *************************************************************************/
	
	public static void tabEnabled(
					JTabbedPane tabbedPane, Component component, boolean bool)
	{
		tabbedPane.setEnabledAt(findComponentIndex(tabbedPane, component), bool);
	}
	
	/**
	 * \brief Find the index of the given {@code Component} among the tabs.
	 * 
	 * @param tabbedPane
	 * @param component
	 * @return {@code int} index if found, {@code -1} if not.
	 */
	public static int findComponentIndex(
								JTabbedPane tabbedPane, Component component)
	{
		int totalTabs = tabbedPane.getTabCount();
		for ( int i = 0; i < totalTabs; i++ )
		{
		   Component c = tabbedPane.getComponentAt(i);
		   if ( c.equals(component) )
			   return i;
		}
		return -1;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	protected static JPanel newTab()
	{
		JPanel out = new JPanel();
		out.setLayout(new WrapLayout(FlowLayout.CENTER, 5, 5));
		return out;
	}
	
	/**
	 * \brief Get a formatted JPanel with JLabel.
	 * 
	 * @param text
	 * @return
	 */
	protected static JComponent textPanel(String text)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		JLabel filler = new JLabel(text);
		filler.setPreferredSize(new Dimension(600,30));
		panel.add(filler, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param description
	 * @param inputArea
	 * @return
	 */
	protected static JPanel inputPanel(String description, JTextArea inputArea)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel descriptionLabel = new JLabel(description);
		descriptionLabel.setPreferredSize(new Dimension(200, 30));
		panel.add(descriptionLabel, BorderLayout.WEST);
		
		inputArea.setPreferredSize(new Dimension(400, 30));
		panel.add(inputArea, BorderLayout.EAST);
		return panel;
	}
	
	/**
	 * \brief Get a formatted JPanel with drop-down menu.
	 * 
	 * @param selecter
	 * @return
	 */
	protected static JComponent selectPanel(JComboBox<?> selecter)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		selecter.setPreferredSize(new Dimension(600,30));
		panel.add(selecter, BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * \brief Get a formatted JPanel with JButton, eventListner and description.
	 * 
	 * @param description
	 * @param actionButton
	 * @param actionListner
	 * @return
	 */
	protected static JComponent actionButton(String description, 
							JButton actionButton, ActionListener actionListner)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(600, 30);
		
		JLabel filler = new JLabel(description);
		filler.setPreferredSize(new Dimension(500,30));
		panel.add(filler, BorderLayout.WEST);
		
		actionButton.setPreferredSize(new Dimension(100,30));
		actionButton.addActionListener(actionListner);
		panel.add(actionButton, BorderLayout.EAST);
		return panel;
	}
}

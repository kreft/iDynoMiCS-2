package guiTools;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import idynomics.GuiLaunch;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;


/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiEditor
{
	static HashMap<ModelAttribute,JTextArea> attributes = new HashMap<ModelAttribute,JTextArea>();
	
	public static void setAttributes()
	{
		for ( ModelAttribute a : attributes.keySet())
			a.value = attributes.get(a).getText();
	}
	
	/*
	 * The JComponent set in the gui
	 */
	public static void addComponent(ModelNode node, JComponent parent) {
		
		JTabbedPane tabs = GuiComponent.newPane();
		JPanel component = new JPanel();
		tabs.addTab(node.tag, component);
		component.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));
		JPanel attr = new JPanel();
		attr.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
		for(NodeConstructor c : node.childConstructors.keySet())
		{
			if(node.childConstructors.get(c) == Requirements.EXACTLY_ONE && node.getChildNodes(c.defaultXmlTag()).isEmpty())
			{
				NodeConstructor newNode = c.newBlank();
				node.add(newNode.getNode());
				node.add(newNode);
			}
			else if(node.childConstructors.get(c) == Requirements.EXACTLY_ONE)
			{
				// childNode already present
			}
			else
			{
				attr.add(GuiComponent.actionButton(c.defaultXmlTag(), new JButton("add"), new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						NodeConstructor newNode = c.newBlank();
						node.add(newNode.getNode());
						addComponent(newNode.getNode(), component);
						node.add(newNode);
					}
				}
				));
			}
		}
		
		for(ModelAttribute a : node.attributes)
		{
			JTextArea input = new JTextArea();
			input.setText(a.value);
			input.setEditable(a.editable);
			attr.add(GuiComponent.inputPanel(a.tag, input));
			attributes.put(a, input);
		}
		component.add(attr);
		
		if( node.requirement.maxOne() && parent != GuiMain.tabbedPane )
		{
			parent.add(component, null);
			parent.revalidate();
		}
		else if( node.requirement == Requirements.ZERO_TO_FEW)
		{
			GuiComponent.addTab((JTabbedPane) parent.getParent().getParent(), node.tag, tabs, "");
		} 
		else
		{
			GuiComponent.addTab((JTabbedPane) parent, node.tag, tabs, "");
		}
		
		
		for(ModelNode n : node.childNodes)
			addComponent(n, component);
	}
}

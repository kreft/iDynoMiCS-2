package guiTools;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import idynomics.Idynomics;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;


/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiEditor
{
	/*
	 * The JComponent set in the gui
	 */
	public static void addComponent(ModelNode node) {
		
		JPanel component = new JPanel();
		component.setLayout(new WrapLayout(FlowLayout.CENTER, 5, 5));
		
		HashMap<String,JTextArea> attributes = new HashMap<String,JTextArea>();
		for(ModelAttribute a : node.attributes)
		{
			JTextArea input = new JTextArea();
			input.setText(a.value);
			input.setEditable(a.editable);
			component.add(GuiMain.inputPanel(a.tag, input));
			attributes.put(a.tag, input);
		}
		
		component.add(GuiMain.actionButton("set", new JButton("set"), new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				for ( String key : attributes.keySet())
					node.getAttribute(key).value = attributes.get(key).getText();
			}
		}
		));
		
		GuiMain.addTab(node.tag, component, "");
		for(ModelNode n : node.childNodes)
			addComponent(n);
	}
}

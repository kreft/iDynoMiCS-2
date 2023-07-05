package gui.navigator;

import javax.swing.JComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import gui.GuiConsole;
import settable.Module;

/**
 * Small test case, could potentially cleanup gui for files with large amount of
 * agents (eg. scenario's rather than initial states) has potential, will pick
 * this up again when we have more time.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class PageObject {
	
	DefaultMutableTreeNode pageNode = null;
		
	String pageName = null;
	
	Module node = null;
	
	JComponent pageComponent = null;
	
	public PageObject(String name, JComponent component, Module node)
	{
		this.pageName = name;
		this.pageComponent = component;
		this.node = node;
		this.pageNode = new DefaultMutableTreeNode(name);
	}

	public String toString() {
        return pageName;
    }

	public JComponent getComponent() 
	{
		if (pageComponent == null)
			return GuiConsole.getConsole();
		else
			return pageComponent;
	}
}

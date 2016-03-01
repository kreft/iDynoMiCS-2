/**
 * 
 */
package guiTools;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import dataIO.Log;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class GuiMenu
{
	private static JMenuBar menuBar;
	
	public static JMenuBar getMenuBar()
	{
		menuBar = new JMenuBar();
		menuBar.add(fileMenu());
		return menuBar;
	}
	
	
	private static JMenu fileMenu()
	{
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;
		/* 
		 * Set up the File menu.
		 */
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("File options");
		/*
		 * Add the option of opening a protocol file.
		 */
		menuItem = new JMenuItem(new GuiActions.FileOpen());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Open existing protocol file");
		menu.add(menuItem);
		/*
		 * Add the option of rendering a compartment.
		 */
		menuItem = new JMenuItem(new GuiActions.RenderThis());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
				"Render a spatial compartment");
		menu.add(menuItem);
		/* 
		 * Template for further development: we can do switches or toggles
		 * later.
		 */
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("placeholder");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(cbMenuItem);
		/*
		 * Output level.
		 */
		menu.addSeparator();
		submenu = new JMenu("OutputLevel");
		submenu.setMnemonic(KeyEvent.VK_L);
		ButtonGroup group = new ButtonGroup();
		for ( Log.Tier t : Log.Tier.values() )
		{
			rbMenuItem = new JRadioButtonMenuItem(new GuiActions.LogTier(t));
			group.add(rbMenuItem);
			submenu.add(rbMenuItem);
		}
		menu.add(submenu);
		/*
		 * Finally, return the File menu.
		 */
		return menu;
	}
}

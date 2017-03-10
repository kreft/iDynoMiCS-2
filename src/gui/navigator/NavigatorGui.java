package gui.navigator;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import gui.GuiButtons;
import gui.GuiMenu;
import idynomics.Idynomics;
import utility.Helper;

/**
 * Small test case, could potentially cleanup gui for files with large amount of
 * agents (eg. scenario's rather than initial states) has potential, will pick
 * this up again when we have more time.
 * 
 * http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TreeDemoProject/src/components/TreeDemo.java
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class NavigatorGui extends JPanel implements TreeSelectionListener {
	
	/**
	 * System file path to the iDynoMiCS logo.
	 */
	private final static String ICON_PATH = "icons/iDynoMiCS_logo_icon.png";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2532237635946835720L;
	private JTree tree;
	private JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	protected PageObject consolePage = new PageObject("console", null, null);
	protected DefaultMutableTreeNode root = new DefaultMutableTreeNode(consolePage);
	public static NavigatorGui activeGui = null;

	public NavigatorGui()
	{
        super(new GridLayout(1,0));
	    addPage(consolePage,root);
	    //Create a tree that allows one selection at a time.
	    tree = new JTree(root);
	    tree.getSelectionModel().setSelectionMode
	            (TreeSelectionModel.SINGLE_TREE_SELECTION);
	
	    //Listen for when the selection changes.
	    tree.addTreeSelectionListener(this);
	
	    //Create the scroll pane and add the tree to it. 
	    JScrollPane treeView = new JScrollPane(tree);
	    
	    //Add the scroll panes to a split pane.
	    
	    splitPane.setLeftComponent(treeView);
	    
	    displayComponent(consolePage.getComponent());
	    Dimension minimumSize = new Dimension(100, 50);
	    treeView.setMinimumSize(minimumSize);
	    splitPane.setDividerLocation(100); 
	    splitPane.setPreferredSize(new Dimension(500, 300));
	
	    //Add the split pane to this panel.
	    add(splitPane);
	}
	
	public void addPage(PageObject page, DefaultMutableTreeNode parent)
	{
		DefaultMutableTreeNode pageNode = new DefaultMutableTreeNode(page);
		page.pageNode = pageNode;
		parent.add(pageNode);
	}
	
    public void displayComponent(JComponent component) {
    	splitPane.setRightComponent(component);
    }
    

    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                           tree.getLastSelectedPathComponent();
        if (node == null) return;
        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            PageObject page = (PageObject)nodeInfo;
            displayComponent(page.getComponent());
        } else {
        	// TODO
        }
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        try 
        {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Couldn't use system look and feel.");
        }
        
        //Create and set up the window.
        JFrame frame = new JFrame();
        /* 
		 * When running in GUI we want dialog input instead of command line 
		 * input.
		 */
		Helper.gui = true;
		/* 
		 * Set the window size, position, title and its close operation.
		 */
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(Idynomics.fullDescription());

		frame.setLocationRelativeTo(null);
		
		ImageIcon img = new ImageIcon(ICON_PATH);

		frame.setIconImage(img.getImage());
		
		/* 
		 * Add the menu bar. This is independent of the layout of the rest of
		 * the GUI.
		 */
		frame.setJMenuBar(GuiMenu.getMenuBar());

		 //Add the scroll panes to a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(GuiButtons.getButtons());
        activeGui = new NavigatorGui();
        splitPane.setBottomComponent(activeGui);
        splitPane.setDividerLocation(30); 
        splitPane.setDividerSize(0);
        splitPane.setEnabled( false );
        frame.add(splitPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
	
}

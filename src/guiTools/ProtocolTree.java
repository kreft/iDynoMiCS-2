package guiTools;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

/**
 * 
 * 
 * <p>
 * Borrows heavily from here:
 * https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TreeDemoProject/src/components/TreeDemo.java
 * </p>
 * 
 */
// WORK IN PROGRESS, MAY NEVER BE USED
public class ProtocolTree extends JSplitPane implements TreeSelectionListener
{
	private static final long serialVersionUID = 9051301816462464424L;
	
	/**
	 * 
	 */
	private JTree tree;
	
	private JScrollPane treeView;
	
	private JScrollPane leafView;
	
	private GroupLayout leafLayout;
	
	public ProtocolTree()
	{
		super(JSplitPane.VERTICAL_SPLIT);
		
		DefaultMutableTreeNode top =
							new DefaultMutableTreeNode("Simulation");
		this.createNodes(top);
		/* Create a tree that allows one selection at a time. */
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode
							(TreeSelectionModel.SINGLE_TREE_SELECTION);
		/* Listen for when the selection changes. */
		tree.addTreeSelectionListener(this);
		/* Create the scroll pane and add the tree to it. */
		treeView = new JScrollPane(tree);
		/* Create the leaf editing pane. */
		leafView = new JScrollPane();
		/* Add the scroll panes to the overall view. */
		this.setTopComponent(treeView);
		this.setBottomComponent(leafView);
	}
	
	private void createNodes(DefaultMutableTreeNode top)
	{
		DefaultMutableTreeNode timer = new DefaultMutableTreeNode("Timer");
		top.add(timer);
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
										tree.getLastSelectedPathComponent();
		/* Safety. */
		if (node == null)
			return;
		
		Object nodeInfo = node.getUserObject();
		if ( node.isLeaf() )
		{
			// TODO this may need to be replaced by an extension of JPanel
			// that implements ActionListener
			JPanel leafPanel = new JPanel();
			leafLayout = new GroupLayout(leafPanel);
			leafLayout.setAutoCreateGaps(true);
			leafLayout.setAutoCreateContainerGaps(true);
			Group verticalLayoutGroup = leafLayout.createSequentialGroup();
			Group horizontalLayoutGroup = leafLayout.createParallelGroup();
			leafLayout.setVerticalGroup(verticalLayoutGroup);
			leafLayout.setHorizontalGroup(horizontalLayoutGroup);
			leafPanel.setLayout(leafLayout);
			leafView = new JScrollPane(leafPanel);
			leafView.setVisible(true);
		}
		else
		{
			 
		}
	}
}
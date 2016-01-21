/**
 * 
 */
package jsbmlSandpit;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.AbstractSBasePlugin;

/**
 * @author cleggrj
 *
 */
public class ExampleModel extends AbstractSBasePlugin {


	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;

	public ExampleModel(Model model)
	{
		super(model);
	}
	
	public Model getModel()
	{
		return (Model) getExtendedSBase();
	}
	
	/*************************************************************************
	 * INHERITED METHODS
	 ************************************************************************/
	
	/**\brief TODO
	 * 
	 */
	public ExampleModel() {
		// TODO Auto-generated constructor stub
	}

	/**\brief TODO
	 * 
	 * @param extendedSBase
	 */
	public ExampleModel(SBase extendedSBase) {
		super(extendedSBase);
		// TODO Auto-generated constructor stub
	}

	/**\brief TODO
	 * 
	 * @param plugin
	 */
	public ExampleModel(AbstractSBasePlugin plugin) {
		super(plugin);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#readAttribute(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean readAttribute(String attributeName, String prefix, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#getElementNamespace()
	 */
	@Override
	public String getElementNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#getPackageName()
	 */
	@Override
	public String getPackageName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#getPrefix()
	 */
	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.SBasePlugin#getURI()
	 */
	@Override
	public String getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	@Override
	public boolean getAllowsChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	@Override
	public TreeNode getChildAt(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	@Override
	public int getChildCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.ext.AbstractSBasePlugin#clone()
	 */
	@Override
	public AbstractSBasePlugin clone() {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * 
 */
package jsbmlSandpit;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;


public class JSBMLvisualizer extends JFrame
{
	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;
	
	public JSBMLvisualizer(SBMLDocument document)
	{
		super(document.getModel().getId());
		getContentPane().add(new JScrollPane(new JTree(document)));
		pack();
		setVisible(true);
	}
	
	/**
	* Main routine. Note: this does not perform any error checking, but should. It is an illustration only.
	* @param args Expects a valid path to an SBML file.
	*/
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		System.out.println(args[0]);
		new JSBMLvisualizer(SBMLReader.read(new File(args[0])));
	}
	
	/*************************************************************************
	 * INHERITED METHODS
	 ************************************************************************/
	
	

	/**\brief TODO
	 * 
	 * @throws HeadlessException
	 */
	public JSBMLvisualizer() throws HeadlessException {
		// TODO Auto-generated constructor stub
	}

	/**\brief TODO
	 * 
	 * @param arg0
	 */
	public JSBMLvisualizer(GraphicsConfiguration arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**\brief TODO
	 * 
	 * @param title
	 * @throws HeadlessException
	 */
	public JSBMLvisualizer(String title) throws HeadlessException {
		super(title);
		// TODO Auto-generated constructor stub
	}

	/**\brief TODO
	 * 
	 * @param title
	 * @param gc
	 */
	public JSBMLvisualizer(String title, GraphicsConfiguration gc) {
		super(title, gc);
		// TODO Auto-generated constructor stub
	}
}

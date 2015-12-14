package jsbmlSandpit;

import java.beans.PropertyChangeEvent;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.History;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.util.TreeNodeRemovedEvent;
/** Creates an {@link SBMLDocument} and writes its contents to a file. **/
public class JSBMLexample implements TreeNodeChangeListener {

	public JSBMLexample() throws Exception {
		// Create a new SBMLDocument object, using SBML Level 3 Version 1.
		SBMLDocument doc = new SBMLDocument(3, 1);
		doc.addTreeNodeChangeListener(this);
		// Create a new SBML model, and add a compartment to it.
		Model model = doc.createModel("test_model");
		Compartment compartment = model.createCompartment("default");
		compartment.setSize(1d);
		// Create a model history object and add author information to it.
		History hist = model.getHistory(); // Will create the History, if it does not exist
		Creator creator = new Creator("Given Name", "Family Name", "Organisation", "My@EMail.com");
		hist.addCreator(creator);
		// Create some sample content in the SBML model.
		Species specOne = model.createSpecies("test_spec1", compartment);
		Species specTwo = model.createSpecies("test_spec2", compartment);
		Reaction sbReaction = model.createReaction("reaction_id");
		// Add a substrate (SBO:0000015) and product (SBO:0000011) to the reaction.
		SpeciesReference subs = sbReaction.createReactant(specOne);
		subs.setSBOTerm(15);
		SpeciesReference prod = sbReaction.createProduct(specTwo);
		prod.setSBOTerm(11);
		// For brevity, WE DO NOT PERFORM ERROR CHECKING, but you should,
		// using the method doc.checkConsistency() and then checking the error log.
		doc.checkConsistency();
		// Write the SBML document to a file.
		SBMLWriter.write(doc, "test.xml", "JSBMLexample", "1.0");
	}

	/** Main routine. This does not take any arguments. */
	public static void main(String[] args) throws Exception {
		new JSBMLexample();
	}

	/* Methods for TreeNodeChangeListener, to respond to events from SBaseChangedListener. */
	@Override
	public void nodeAdded(TreeNode sb) {
		System.out.println("[ADD] " + sb);
	}

	@Override
	public void nodeRemoved(TreeNodeRemovedEvent evt) {
		System.out.println("[RMV] " + evt.getSource());
	}

	@Override
	public void propertyChange(PropertyChangeEvent ev) {
		System.out.println("[CHG] " + ev);
	}

}
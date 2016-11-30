/**
 * 
 */
package reaction;

import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Element;

import generalInterfaces.Instantiatable;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import referenceLibrary.XmlRef;

/**
 * \brief Stores environmental reactions that are used in every compartment.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 */
public class ReactionLibrary implements Instantiatable, NodeConstructor
{
	
	public void init(Element xmlElem, NodeConstructor parent)
	{
		/* init something from xml? */
	}
	
	/**
	 * Contains all common environmental reactions.
	 */
	Collection<Reaction> _reactions = new LinkedList<Reaction>();
	private NodeConstructor _parentNode;
	
	/* ***********************************************************************
	 * BASIC GETTERS
	 * **********************************************************************/
	
	/**
	 * @return Collection of all common environmental reactions.
	 */
	public Collection<Reaction> getAllReactions()
	{
		return this._reactions;
	}
	
	/**
	 * \brief Check if this contains a reaction with the given name.
	 * 
	 * @param name Name of the reaction.
	 * @return True if it is present, false if it is absent.
	 */
	public boolean hasReaction(String name)
	{
		for ( Reaction r : this._reactions )
			if ( r.getName().equals(name) )
				return true;
		return false;
	}
	
	/**
	 * \brief Get a reaction with the given name.
	 * 
	 * <p>Returns null if it cannot be found.</p>
	 * 
	 * @param name Name of the reaction.
	 * @return The reaction object, if present.
	 */
	public Reaction getReaction(String name)
	{
		for ( Reaction r : this._reactions )
			if ( r.getName().equals(name) )
				return r;
		return null;
	}
	
	/* ***********************************************************************
	 * NODE CONSTRUCTION
	 * **********************************************************************/
	
	public String getName()
	{
		return "Reaction Library";
	}
	
	@Override
	public ModelNode getNode()
	{
		/* The reaction library node. */
		ModelNode modelNode = new ModelNode(this.defaultXmlTag(), this);
		modelNode.setRequirements(Requirements.EXACTLY_ONE);
		/* Reaction constructor. */
		modelNode.addChildConstructor(new Reaction(), 
				ModelNode.Requirements.ZERO_TO_MANY);
		/* The already-existing reactions. */
		for ( Reaction r : this._reactions )
			modelNode.add(r.getNode());
		return modelNode;
	}

	@Override
	public void addChildObject(NodeConstructor childObject) 
	{
		if ( childObject instanceof Reaction )
			this._reactions.add((Reaction) childObject);
	}
	
	@Override
	public String defaultXmlTag()
	{
		return XmlRef.reactionLibrary;
	}

	@Override
	public void setParent(NodeConstructor parent) 
	{
		this._parentNode = parent;
	}

	@Override
	public NodeConstructor getParent() 
	{
		return this._parentNode;
	}
}

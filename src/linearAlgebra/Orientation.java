package linearAlgebra;

import org.w3c.dom.Element;

import dataIO.XmlHandler;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;

/** 
 * Object encapsulates unit vector to indicate direction or orientation
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Orientation implements Settable, Instantiable {
	
	private double[] _unitVector;
	private Settable _parent;
	
	/* *****************************************
	 * constructors
	 */
	
	public Orientation()
	{
		// Note instantiable implementation requires blank constructor
	}
	
	public Orientation( double[] unitVector, Settable parent )
	{
		this._unitVector = unitVector;
		this._parent = parent;
	}
	
	/* *****************************************
	 * getters and setters
	 */
	
	public double[] getUnitVector()
	{
		return this._unitVector;
	}
	
	public void setUnitVector(double[] vector)
	{
		this._unitVector = vector;
	}
	
	/* ****************************************
	 * practical methods
	 */
	
	/**
	 * returns inversion of this orientation
	 * @return
	 */
	public double[] inverse()
	{
		return Vector.reverse(this._unitVector);
	}
	
	/**
	 * returns true if the unit vector contains only zero values.
	 * @return
	 */
	public boolean isNullVector()
	{
		for( double d : this._unitVector)
			if( d != 0)
				return false;
		return true;
	}
	/* ****************************************
	 * Instantiable and Settable implementation
	 */
	
	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		Element p = xmlElement;
		if ( XmlHandler.hasAttribute(p, XmlRef.variable) )
			this._unitVector = Vector.dblFromString( ( 
					p.getAttribute(XmlRef.variable) ) );
		this.setParent(parent);
	}

	@Override
	public Module getModule() 
	{
		Module modelNode = new Module(defaultXmlTag(), this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.add(new Attribute(XmlRef.variable, 
					Vector.toString(this._unitVector), null, true ));
		return modelNode;
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.orientation;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parent = parent;
	}

	@Override
	public Settable getParent() 
	{
		return this._parent;
	}

}

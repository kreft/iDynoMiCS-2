package physicalObject;

import org.w3c.dom.Element;

import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import surface.*;

/**
 * \brief wrapper object for physical entities within the domain such as
 * additional domain surfaces
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class PhysicalObject implements Settable, Instantiable
{
	private Settable _parentNode;
	
	private Surface _surface;
	private boolean _mobile = false;
	
	public PhysicalObject()
	{
		// Keep this empty constructor
	}
	
	public PhysicalObject(Settable parent,
			Surface surface, boolean mobile)
	{
		this._parentNode = parent;
		
		this._surface = surface;
		this._mobile = mobile;
	}
	
	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		this._parentNode = parent;
		
		Surface.Type type = Surface.Type.valueOf( 
				xmlElement.getAttribute( XmlRef.typeAttribute ));
		
		switch (type) {
			case SPHERE :
				this._surface = new Ball(xmlElement);
				break;
			case ROD :
				this._surface = new Rod(xmlElement);
				break;
			case CUBOID :
				this._surface = new Cuboid(xmlElement);
				break;
			case VOXEL:
				this._surface = new Voxel(xmlElement);
				break;
			case PLANE :
				this._surface = new Plane(xmlElement);
		}	
	}
	
	public Surface getSurface()
	{
		return this._surface;
	}
	
	public boolean isMobile()
	{
		return this._mobile;
	}

	@Override
	public Module getModule() 
	{
		/* create the agent node */
		Module modelNode = new Module(this.defaultXmlTag(), this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.add(new Attribute(XmlRef.typeAttribute, 
				String.valueOf(this._surface.type()), null, false ));
		
		this._surface.appendToModule(modelNode);

		return modelNode;
	}

	public String defaultXmlTag() 
	{
		return XmlRef.physicalObject;
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}

}

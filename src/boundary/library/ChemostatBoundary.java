package boundary.library;

import boundary.Boundary;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;

/**
 * \brief abstract class that captures identical agent transfer behvior for
 * chemostat boundaries. 
 * 
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public abstract class ChemostatBoundary extends Boundary {
	
	protected boolean _agentRemoval = true;
	
	public ChemostatBoundary()
	{
		super();
	}
	
	@Override
	public Module getModule()
	{
		Module mod = super.getModule();
		mod.add( new Attribute( XmlRef.agentRemoval, String.valueOf( 
				this._agentRemoval ), null, true ));
		return mod;
	}

}

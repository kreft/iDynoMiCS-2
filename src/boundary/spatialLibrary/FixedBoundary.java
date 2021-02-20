package boundary.spatialLibrary;

import java.util.Collection;

import org.w3c.dom.Element;

import dataIO.XmlHandler;
import instantiable.Instantiable;
import referenceLibrary.XmlRef;
import settable.Settable;

/**
 * \brief Spatial boundary where solute concentrations are kept fixed. Solid
 * surface to agents. Intended for testing purposes.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class FixedBoundary extends BiofilmBoundaryLayer implements Instantiable
{
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/
	
	public FixedBoundary()
	{ 
		super(); 
	}
	
	public void instantiate(Element xmlElement, Settable parent)
	{
		super.instantiate(xmlElement,parent);
		
		Collection<Element> elements = 
				XmlHandler.getElements(xmlElement, XmlRef.solute);
		String name, concn;
		
		for ( Element e : elements )
		{
			name = XmlHandler.obtainAttribute(e,
					XmlRef.nameAttribute, XmlRef.concentration);
			this.setConcentration(name, XmlHandler.obtainDouble(e, 
					XmlRef.concentration, XmlRef.concentration));
		}
	}

	/* ***********************************************************************
	 * PARTNER BOUNDARY
	 * **********************************************************************/

	@Override
	public Class<?> getPartnerClass()
	{
		return null;
	}

	/* ***********************************************************************
	 * SOLUTE TRANSFERS
	 * **********************************************************************/
	
	/**
	 * \brief Set the concentration of a solute at this boundary.
	 * 
	 * @param name Name of the solute.
	 * @param concn Concentration of the solute.
	 */
	public void setConcentration(String name, double concn)
	{
		this._concns.put(name, concn);
	}

	@Override
	public void additionalPartnerUpdate() {}
	
	public boolean isSolid()
	{
		return true;
	}
}

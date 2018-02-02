package chemical;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Module;
import settable.Settable;
import utility.Helper;

public class Chemical implements Settable, Instantiable
{
	/**
	 * name of chemical
	 */
	private String _name;
	
	/**
	 * Standard gibbs energy of formation
	 */
	private double _formationGibbs;
	
	/**
	 * Charge and chemical composition
	 */
	private double[] _composition;
	
	/**
	 * 
	 */
	private double _referenceOxidationState;
	
	/**
	 * named compounds in chemical composition vector, default: 
	 * Charge e, Carbon C, Hydrogen H, Oxygen O, Nitrogen N, Phosphorus P, Sulfur S.
	 */
	public static final char[] components = "eCHONPS".toCharArray();
	
	public static final double[] atomicMass = 
			new double[] { 0, 12, 1, 16, 14, 31, 32};
	
	public String getName()
	{
		return this._name;
	}

	public double estimateRefOxidationState( int atom ) 
	{
		if( atom == 0 )
		{
			if (_composition[1] > 0.0 )
				atom = 1;
			else if (_composition[4] > 0.0 )
				atom = 4;
			else if (_composition[5] > 0.0 )
				atom = 5;
			else if (_composition[6] > 0.0 )
				atom = 6;
		}
		/* carbon */
		if ( atom == 1 )
			return (_composition[0] - _composition[2] + 2 * _composition[3] + 
					3 * _composition[4] - 2 * _composition[6]) / _composition[1];
		/* nitrogen */
		else if ( atom == 4 )
			return (_composition[0] - _composition[2] + 2 * _composition[3] - 
					4 * _composition[1]) / _composition[4];
		/* phosphorus */
		else if ( atom == 5 )
			return (_composition[0] - _composition[2] + 2 * _composition[3] +
					3 * _composition[4] - 4 * _composition[1]) / _composition[5];
		/* sulfur */
		else if ( atom == 6 )
			return (_composition[0] - _composition[2] + 2 * _composition[3] +
					3 * _composition[4] - 4 * _composition[1]) / _composition[6];
		else
		{
			Log.out(Tier.CRITICAL, this.getClass().getSimpleName() + " unable "
					+ "to estimate oxidation state, returning 0.0!");
			return 0.0;
		}
	}
	
	public double molarWeight() {
		return Vector.dotProduct(_composition, atomicMass);
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		this._name = XmlHandler.obtainAttribute(
				xmlElement, XmlRef.nameAttribute, this.defaultXmlTag() );
		
		this._formationGibbs = Double.valueOf( XmlHandler.obtainAttribute(
				xmlElement, XmlRef.formationGibbs, this.defaultXmlTag() ) );
		

		if( xmlElement.getAttribute( XmlRef.composition ).split(",").length > 1 )
		{
			/* simply a vector with a position for each chemical element */
			this._composition = Vector.dblFromString( XmlHandler.obtainAttribute(
					xmlElement, XmlRef.composition, this.defaultXmlTag() ) );
		}
		else
		{
			/* for instance CHO3e-1 for bicarbonate */
			this._composition = Vector.zerosDbl(components.length);
			String temp = xmlElement.getAttribute( XmlRef.composition );
			int count = 0;
			boolean match;
			for (int i = 0; i < temp.length(); i++)
			{
				match = false;
				for( int j = 0; j < components.length; j++)
				{
					if ( components[j] == temp.charAt(i) )
					{
						for(int k = 0; k < temp.length()-i-1; k++)
						{
							for ( char c : components )
								if ( c == temp.charAt(i+k+1) )
								{
									match = true;
									break;
								}
							if ( match )
								break;
							else
								count++;
						}
						if( count != 0)
							this._composition[j] += 
							Double.valueOf(temp.substring(i+1, i+count+1));
						else
							this._composition[j] += 1;
					}
				}
			}
		}
		
		if( XmlHandler.hasAttribute(xmlElement, XmlRef.oxidationState))
		{
			this._referenceOxidationState = Double.valueOf( 
					XmlHandler.obtainAttribute(
					xmlElement, XmlRef.oxidationState, this.defaultXmlTag() ) );
		}
		else
			this._referenceOxidationState = estimateRefOxidationState(0);
	}

	@Override
	public Module getModule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(Settable parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Settable getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}

package chemical;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import instantiable.Instantiable;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
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

	private Settable _parentNode;
	
	/**
	 * named compounds in chemical composition vector, default: 
	 * Charge e, Carbon C, Hydrogen H, Oxygen O, Nitrogen N, Phosphorus P, Sulfur S.
	 * 
	 */
	public static final String components = "eCHONPS";
	
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
	
	public double getOxidationState()
	{
		return this._referenceOxidationState;
	}
	
	public double getFormation()
	{
		return this._formationGibbs;
	}
	
	public double molarWeight() {
		return Vector.dotProduct(_composition, atomicMass);
	}
	
	public double get(String component)
	{
		return this._composition[components.indexOf(component)];
	}
	
	/**
	 * Read out chemical formula from string, currently limited amount of
	 * chemicals. For full chemical library we would need to restructure
	 * the components field to a dictionary of multi-character components.
	 * @param input
	 */
	public void setComposition(String input)
	{
		if( input.split(",").length > 1 )
		{
			/* simply a vector with a position for each chemical element */
			this._composition = Vector.dblFromString( input );
		}
		else
		{
			/* for instance CHO3e-1 for bicarbonate */
			this._composition = Vector.zerosDbl(components.length());
			String temp = input;
			int count;
			boolean match;
			for (int i = 0; i < temp.length(); i++)
			{
				match = false;
				count = 0;
				for( int j = 0; j < components.length(); j++)
				{
					if ( components.charAt(j) == temp.charAt(i) )
					{
						for(int k = 0; k < temp.length()-i-1; k++)
						{
							for ( char c : components.toCharArray() )
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
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		this._name = XmlHandler.obtainAttribute(
				xmlElement, XmlRef.nameAttribute, this.defaultXmlTag() );
		
		this._formationGibbs = XmlHandler.obtainDouble(
				xmlElement, XmlRef.formationGibbs, this.defaultXmlTag() );
		

		setComposition( xmlElement.getAttribute( XmlRef.composition ) );
		
		this._referenceOxidationState = Helper.setIfNone(XmlHandler.
				gatherDouble(xmlElement, XmlRef.oxidationState),
				estimateRefOxidationState(0));
	}

	@Override
	public Module getModule()
	{
		Module modelNode = new Module(XmlRef.chemical, this);

		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this._name);
		
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
				this._name, null, false ));
		
		modelNode.add(new Attribute(XmlRef.composition, 
				Vector.toString(this._composition), null, true ));
		
		modelNode.add(new Attribute(XmlRef.formationGibbs, 
				Double.toString(this._formationGibbs), null, true ));

		modelNode.add(new Attribute(XmlRef.oxidationState, 
				Double.toString(this._referenceOxidationState), null, true ));
		
		return modelNode;
	}
	
	/**
	 * Load and interpret the values of the given ModelNode to this 
	 * NodeConstructor object
	 * @param node
	 */
	public void setModule(Module node)
	{
		
		this.setComposition(node.getAttribute( XmlRef.composition ).getValue());
		
		this._formationGibbs = Double.valueOf( 
				node.getAttribute( XmlRef.formationGibbs ).getValue() );
		

		this._referenceOxidationState = Double.valueOf( 
				node.getAttribute( XmlRef.oxidationState ).getValue() );
	}
	
	public void removeModule(String specifier)
	{
		((ChemicalLib) this.getParent()).remove(this);
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.chemical;
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

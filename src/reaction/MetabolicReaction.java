package reaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Element;

import dataIO.XmlHandler;
import expression.Component;
import expression.Expression;
import generalInterfaces.Copyable;
import idynomics.Idynomics;
import instantiable.Instantiable;
import instantiable.object.InstantiableList;
import instantiable.object.InstantiableMap;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import utility.Helper;

public class MetabolicReaction 
	implements Instantiable, Copyable, Settable, Reaction 
{

	/**
	 * The name of this reaction.
	 */
	protected String _name;
	
	protected InstantiableMap<String,HalfReaction> _reactions = 
			new InstantiableMap<String,HalfReaction>(
			String.class, HalfReaction.class, XmlRef.nameAttribute, XmlRef.valueAttribute, 
			XmlRef.reactions, XmlRef.halfReaction, true);
	
	protected HalfReaction donor;

	protected HalfReaction acceptor;
	
	protected HalfReaction anabolismHalf;
	/**
	 * The mathematical expression describing the rate at which this reaction
	 * proceeds.
	 */
	private Component _kinetic;

	private Settable _parentNode;
	
	/* TODO unit dependent... */
	public static final double R = 0.008314;
	
	/* TODO unit dependent... */
	public static final double T = 298;
	
	@Override
	public String getName() 
	{
		return this._name;
	}

	@Override
	public Collection<String> getVariableNames() 
	{
		return this._kinetic.getAllVariablesNames();
	}

	@Override
	public Collection<String> getReactantNames() 
	{
		LinkedList<String> components = new LinkedList<String>();
		components.addAll( donor.getConstituentNames() );
		components.addAll( acceptor.getConstituentNames() );
		components.addAll( anabolismHalf.getConstituentNames() );
		return components;
	}

	@Override
	public Collection<String> getConstituentNames() 
	{
		LinkedList<String> components = new LinkedList<String>();
		components.addAll( donor.getConstituentNames() );
		components.addAll( acceptor.getConstituentNames() );
		components.addAll( anabolismHalf.getConstituentNames() );
		components.addAll( this._kinetic.getAllVariablesNames() );
		return components;
	}
	
	public Map<String, Double> getCatabolism() 
	{
		HashMap<String, Double> out = new HashMap<String, Double>();
		LinkedList<String> components = new LinkedList<String>();
		components.addAll( donor.getConstituentNames() );
		for( String key : acceptor.getConstituentNames() )
			if( !components.contains(key) )
				components.add(key);
		for( String key : components )
			out.put(key, donor.getStoichiometry(key) - 
					( donor.electrons() / acceptor.electrons() ) * 
					acceptor.getStoichiometry(key) );
		return out;
	}
	
	public Map<String, Double> getAnabolism() 
	{
		HashMap<String, Double> out = new HashMap<String, Double>();
		LinkedList<String> components = new LinkedList<String>();
		components.addAll( anabolismHalf.getConstituentNames() );
		if( anabolismHalf.electrons() < 0.0 )
		{
		for( String key : donor.getConstituentNames() )
			if( !components.contains(key) )
				components.add(key);
		for( String key : components )
			out.put(key, anabolismHalf.getStoichiometry(key) - 
					( anabolismHalf.electrons() / donor.electrons() ) * 
					donor.getStoichiometry(key) );
		}
		else
		{
			for( String key : acceptor.getConstituentNames() )
				if( !components.contains(key) )
					components.add(key);
			for( String key : components )
				out.put(key, anabolismHalf.getStoichiometry(key) - 
						( anabolismHalf.electrons() / acceptor.electrons() ) * 
						acceptor.getStoichiometry(key) );
		}
		return out;
	}
	
	public double dissipationGibbs()
	{
		double c = donor.maxCarbon().get("C");
		double y = donor.maxCarbon().getOxidationState();
		return 200.0 + 18.0 * Math.pow( 6.0-c , 1.8 ) + Math.exp( Math.pow( 
				Math.pow( -0.2 - y , 2 ) , 0.16 ) * ( 3.6 + 0.4 * y ) );
	}
	
	public double CatabolicGibbs( Map<String, Double> concns )
	{
		double out = 0.0, temp = 0;
		for( String k : this.getCatabolism().keySet() )
			out += Idynomics.simulator.chemicalLibrary.get( k ).getFormation() * 
					this.getCatabolism().get(k);
		
		for( String k : this.getCatabolism().keySet() )
			temp += Math.log( (concns.get(k) == 0.0 ? 1E-200 : concns.get(k) )) * this.getCatabolism().get(k);
		
		return out + R * T * temp;
	}
	
	public double anabolicGibbs( Map<String,Double> concns )
	{
		double out = 0.0, temp = 0;
		for( String k : this.getAnabolism().keySet() )
			out += Idynomics.simulator.chemicalLibrary.get( k ).getFormation() * 
					this.getAnabolism().get(k);
		
		for( String k : this.getAnabolism().keySet() )
			temp += Math.log( (concns.get(k) == 0.0 ? 1E-200 : concns.get(k) )) * this.getAnabolism().get(k);
		
		return out + R * T * temp;
	}
	
	public double fCat( Map<String,Double> concns )
	{
		return ( this.anabolicGibbs( concns ) + this.dissipationGibbs() ) / 
		( - CatabolicGibbs( concns ) );
	}
	
	public double maxGrowth( Map<String,Double> concns )
	{
		return ( ( ( -3.0 * CatabolicGibbs( concns ) /
				donor.electrons() ) - 4.5 ) / this.dissipationGibbs() ) * 
				Math.exp( ( -69000.0 / R ) * ( ( 1.0 / T ) - ( 1.0 / 298.0 ) ));
	}

	public Map<String, Double> getStoichiometry( Map<String,Double> concns ) 
	{
		if( concns == null )
		{
			concns = new HashMap<String, Double>();
			for( String key : anabolismHalf.getConstituentNames() )
				concns.put(key, 1.0);
			for( String key : donor.getConstituentNames() )
				concns.put(key, 1.0);
			for( String key : acceptor.getConstituentNames() )
				concns.put(key, 1.0);
			concns.put("proton" , 1.0E-7);
		}
		
		/* if not set use standard concentration */

		
		HashMap<String, Double> out = new HashMap<String, Double>();
		Map<String, Double> ana = getAnabolism();
		Map<String, Double> cat = getCatabolism();
		LinkedList<String> components = new LinkedList<String>();
		components.addAll( ana.keySet() );
		double f = fCat( concns );
		for( String key : cat.keySet() )
			if( !components.contains(key) )
				components.add(key);
		for( String key : components )
		{
			out.put(key, 
					(cat.containsKey(key) ? cat.get(key) : 0.0 ) * f +
					(ana.containsKey(key) ? ana.get(key) : 0.0 ) );
		}
		return out;
	}
	
	public Double getStoichiometry( Map<String,Double> concns,
			String reactant) 
	{
		if( this.getConstituentNames().contains(reactant))
			return this.getStoichiometry( concns ).get( reactant );
		else
			return 0.0;
	}
	
	private double getRate( Map<String, Double> concns )
	{
		concns.put("uMax", maxGrowth(concns) );
		return this._kinetic.getValue( concns );
	}
	
	@Override
	public Map<String, Double> getStoichiometryAtStdConcentration() 
	{
		return getStoichiometry(null);
	}

	@Override
	public double getProductionRate(Map<String, Double> concns, 
			String reactantName)
	{
		return this.getStoichiometry( concns, reactantName ) * 
				this.getRate( concns );
	}

	@Override
	public Module getModule() 
	{
		Module modelNode = new Module(XmlRef.metabolicReaction, this);

		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		modelNode.setTitle(this._name);
		
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
				this._name, null, false ));
		
		modelNode.add(((Expression) _kinetic).getModule());
		
		modelNode.add( this._reactions.getModule() );

		
		return modelNode;
	}

	@Override
	public String defaultXmlTag() 
	{
		return XmlRef.metabolicReaction;
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

	@Override
	public Object copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void instantiate(Element xmlElem, Settable parent) 
	{
		if (parent instanceof InstantiableList)
			((InstantiableList<MetabolicReaction>) parent).add(this);

		if ( !Helper.isNullOrEmpty( xmlElem ) && 
				XmlHandler.hasChild( xmlElem, XmlRef.reaction ) )
		{
			xmlElem = XmlHandler.findUniqueChild( xmlElem, XmlRef.reaction );
		}
		
		this._name = XmlHandler.obtainAttribute( xmlElem, XmlRef.nameAttribute, 
				this.defaultXmlTag() );
		/*
		 * Build the stoichiometric map.
		 */
		this._reactions.instantiate( xmlElem, this, 
				String.class.getSimpleName(),HalfReaction.class.getSimpleName(), 
				XmlRef.halfReaction );
		
		this.acceptor = this._reactions.get("acceptor");
		this.donor = this._reactions.get("donor");
		this.anabolismHalf = this._reactions.get("anabolism");

		/*
		 * Build the reaction rate expression.
		 */
		if ( Helper.isNullOrEmpty(xmlElem) || 
				!XmlHandler.hasChild(xmlElem, XmlRef.expression) )
			this._kinetic = new Expression("");
		else
			this._kinetic = new 
				Expression(XmlHandler.findUniqueChild(xmlElem,XmlRef.expression));
		
	}

}

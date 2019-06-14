package expression;

import java.util.HashMap;

import dataIO.Log;
import dataIO.Log.Tier;
/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class Unit {
	
	/**
	 * SI units
	 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
	 *
	 */
	public enum SI
	{
		/* seconds */
		s,
		
		/* meters */
		m,
		
		/* kilogram */
		kg,
		
		/* kelvin */
		K,
		
		/* mole */
		mol,
		
		/* ampere */
		A,
		
		/* candela */
		cd
	}

	/**
	 * map that keeps track of the SI units and their exponents
	 */
	private HashMap<SI, Integer> unitMap = new HashMap<SI, Integer>();
	
	/**
	 * modifier keeps track of the multiplication factor as a result of unit
	 * conversion ( for example 1 day = 86400 s, where 86400 would be the
	 * modifier ).
	 */
	private double modifier;
	
	/**
	 * empty unit constructor
	 */
	public Unit()
	{
		this.init();
	}
	
	/**
	 * new unit from string
	 * @param unit
	 */
	public Unit(String unit)
	{
		this.init();
		this.fromString(unit);
	}
	
	/**
	 * Copy constructor
	 * @param unit
	 */
	public Unit(Unit unit)
	{
		this.modifier = unit.modifier;
		for ( SI si : unit.unitMap.keySet() )
			this.unitMap.put( si, unit.unitMap.get(si) );
	}
	
	/**
	 * initiate empty unitMap and set modifier to 1.
	 */
	public void init()
	{
		for (SI si : SI.values())
			this.unitMap.put(si, 0);
		this.modifier = 1;
	}
	
	/**
	 * returns false on unit mismatch
	 * @param unit
	 * @return
	 */
	public boolean compatible(Unit unit)
	{
		for (SI si : this.unitMap.keySet())
		{
			if (this.unitMap.get(si) != unit.unitMap.get(si))
				return false;
		}
		return true;
	}
	
	/**
	 * the product of two input units
	 * @param unitA
	 * @param unitB
	 * @return
	 */
	public static Unit product(Unit unitA, Unit unitB)
	{
		Unit out = new Unit(unitA);
		out.modifier *= unitB.modifier;
		for ( SI si : out.unitMap.keySet() )
			out.unitMap.put( si, out.unitMap.get(si) + unitB.unitMap.get(si) );
		return out;
	}
	
	/**
	 * The quotient of two input units (unitA / unitB)
	 * @param unitA
	 * @param unitB
	 * @return
	 */
	public static Unit quotient(Unit unitA, Unit unitB)
	{
		Unit out = new Unit(unitA);
		out.modifier /= unitB.modifier;
		for ( SI si : out.unitMap.keySet() )
			out.unitMap.put( si, out.unitMap.get(si) - unitB.unitMap.get(si) );
		return out;
	}
	
	/**
	 * output the units as string including the modifier
	 */
	public String toString()
	{
		return this.modifier + " [" + this.unit() + "]";
	}
	
	/**
	 * get the unit formatter for the requested output format
	 */
	public double format(String format)
	{
		Unit formatter = new Unit(format);
		if ( ! compatible(formatter) )
		{
			Log.out(Tier.QUIET, formatter.unit() + " incompatible with: " 
					+ this.unit());
			///FIXME or should we throw something
			return 0;
		}
		return 1.0/formatter.modifier;
	}
	
	/**
	 * Output just the units
	 * @return
	 */
	public String unit()
	{
		String out = "";
		Integer power;
		for (SI si : this.unitMap.keySet())
		{
			power = unitMap.get(si);
			if ( power == 1 )
				out += si.toString() + "·";
			else if ( power != 0 )
				out += si.toString() + (power > 0 ? "+" : "") + power + "·";
		}
		/* remove tailing · */
		out = out.substring(0, out.length()-1);
		return out;
	}
	
	public double modifier()
	{
		return this.modifier;
	}
	
	/**
	 * build the unit map from string
	 * @param units
	 */
	public void fromString(String units)
	{
		Tier level = Tier.BULK;
		if ( Log.shouldWrite(level) )
			Log.out(level, "Interpretting unit string: " + units);
		/* replace all unit braces and all white space */
		units = units.replaceAll("\\[", "");
		units = units.replaceAll("\\]", "");
		units = units.replaceAll("\\s+", "");
		
		/* split by dot · ALT 250 */
		String[] unitsArray; 
		unitsArray = units.split("·");
		String[] unitPower;
		Integer power;
		/* analyse the powers */
		for (String s : unitsArray)
		{
			/* try + */
			unitPower = s.split("\\+");
			
			if (unitPower.length == 1)
			{
				/* if not + try - */
				unitPower= s.split("-");
				if (unitPower.length == 1)
					/* if not + or - the power is 1 ( no sign ). */
					power = 1;
				else
					/* in case of - */
					power = - Integer.valueOf(unitPower[1]);
			}
			else
				/* in case of + */
				power = Integer.valueOf(unitPower[1]);
			
			/* update the unit map and modifier */
			this.update(unitPower[0], power);
		}
		if ( Log.shouldWrite(level) )
			Log.out(level, "SI interpretation: " + toString());
	}
	
	/** 
	 * mutate the unit map 
	 */
	private void mutation(SI si, Integer mutation)
	{
		this.unitMap.put( si, this.unitMap.get(si) + mutation );
	}
	
	/**
	 * update the unit map and the modifier
	 * @param unit
	 * @param update
	 */
	private void update(String unit, Integer update)
	{
		double power = Double.valueOf(update);
		switch (unit)
		{
		/*
		 * SI units
		 */
			case "s" :
				this.mutation( SI.s, update );
				break;
			case "m" :
				this.mutation( SI.m, update );
				break;
			case "kg" :
				this.mutation( SI.kg, update );
				break;
			case "K" :
				this.mutation( SI.K, update );
				break;
			case "mol" :
				this.mutation( SI.mol, update );
				break;
			case "A" :
				this.mutation( SI.A, update );
				break;
			case "cd" :
				this.mutation( SI.cd, update );
				break;
		/*
		 * Time
		 */
			case "min" :
			case "minute" :
				this.mutation( SI.s, update );
				this.modifier *= Math.pow( 60.0, power );
				break;
			case "h" :
			case "hour" :
				this.mutation( SI.s, update );
				this.modifier *= Math.pow( 3600.0, power );
				break;
			case "d" :
			case "day" :
				this.mutation( SI.s, update );
				this.modifier *= Math.pow( 3600.0*24.0, power );
				break;
		/*
		 * Length
		 */
			case "dm" :
				this.mutation( SI.m, update );
				this.modifier *= Math.pow( 0.1, power );
				break;
			case "cm" :
				this.mutation( SI.m, update );
				this.modifier *= Math.pow( 0.01, power );
				break;
			case "mm" :
				this.mutation( SI.m, update );
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "µm" :
				this.mutation( SI.m, update );
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "nm" :
				this.mutation( SI.m, update );
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "pm" :
				this.mutation( SI.m, update );
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "fm" :
				this.mutation( SI.m, update );
				this.modifier *= Math.pow( 1.0e-15, power );
				break;
		/*
		 * Mass
		 */
			case "g" :
				this.mutation( SI.kg, update );
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "mg" :
				this.mutation( SI.kg, update );
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "µg" :
				this.mutation( SI.kg, update );
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "ng" :
				this.mutation( SI.kg, update );
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "pg" :
				this.mutation( SI.kg, update );
				this.modifier *= Math.pow( 1.0e-15, power );
				break;
			case "fg" :
				this.mutation( SI.kg, update );
				this.modifier *= Math.pow( 1.0e-18, power );
				break;
		/*
		 * Temperature
		 */
			case "C" :
				this.mutation( SI.K, update );
				break;
			case "F" :
				this.mutation( SI.K, update );
				this.modifier /= Math.pow( 1.8, power );
				break;
		/*
		 * Amount of substance
		 */
			case "mmol" :
				this.mutation( SI.mol, update );
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "µmol" :
				this.mutation( SI.mol, update );
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "nmol" :
				this.mutation( SI.mol, update );
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "pmol" :
				this.mutation( SI.mol, update );
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "fmol" :
				this.mutation( SI.mol, update );
				this.modifier *= Math.pow( 1.0e-15, power );
				break;
		
		///////////////////////////////////////////////////////////////////////
		// Derived units
		///////////////////////////////////////////////////////////////////////
				
		/*
		 * Volume
		 */
			case "l" :
				this.mutation( SI.m, (3 * update ));
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "ml" :
				this.update("l", update);
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "µl" :
				this.update("l", update);
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "nl" :
				this.update("l", update);
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "pl" :
				this.update("l", update);
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "fl" :
				this.update("l", update);
				this.modifier *= Math.pow( 1.0e-15, power );
				break;	
		
		/*
		 * Force
		 */
			case "N" :
				this.mutation( SI.kg, update );
				this.mutation( SI.m, update );
				this.mutation( SI.s, -(2 * update) );
				break;
			case "mN" :
				this.update("N", update);
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "µN" :
				this.update("N", update);
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "nN" :
				this.update("N", update);
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "pN" :
				this.update("N", update);
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "fN" :
				this.update("N", update);
				this.modifier *= Math.pow( 1.0e-15, power );
				break;
			/*
			 * Energy
			 */
			case "J" :
				this.mutation( SI.kg, update );
				this.mutation( SI.m, (2 * update ) );
				this.mutation( SI.s, -(2 * update) );
				break;
			case "mJ" :
				this.update("J", update);
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "µJ" :
				this.update("J", update);
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "nJ" :
				this.update("J", update);
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "pJ" :
				this.update("J", update);
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "fJ" :
				this.update("J", update);
				this.modifier *= Math.pow( 1.0e-15, power );
				break;
			case "cal" :
				this.update("J", update);
				this.modifier *= 4.184;
				Log.out(Tier.BULK, "Note: using Thermochemical calorie");
				break;
			case "mcal" :
				this.update("cal", update);
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "µcal" :
				this.update("cal", update);
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "ncal" :
				this.update("cal", update);
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "pcal" :
				this.update("cal", update);
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "fcal" :
				this.update("cal", update);
				this.modifier *= Math.pow( 1.0e-15, power );
				break;
			case "eV" :
				this.update("J", update);
				this.modifier *= 1.60217656535e-19;
				break;
			/*
			 * Power
			 */
			case "W" :
				this.mutation( SI.kg, update );
				this.mutation( SI.m, (2 * update ) );
				this.mutation( SI.s, -(3 * update) );
				break;
			case "mW" :
				this.update("W", update);
				this.modifier *= Math.pow( 0.001, power );
				break;
			case "µW" :
				this.update("W", update);
				this.modifier *= Math.pow( 1.0e-6, power );
				break;
			case "nW" :
				this.update("W", update);
				this.modifier *= Math.pow( 1.0e-9, power );
				break;
			case "pW" :
				this.update("W", update);
				this.modifier *= Math.pow( 1.0e-12, power );
				break;
			case "fW" :
				this.update("W", update);
				this.modifier *= Math.pow( 1.0e-15, power );
				break;
				
		}
	}
}

package expression;

import java.util.HashMap;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlRef;

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
	 * conversion ( for example 1 day = 86400 h, where 86400 would be the
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
	 * output the units as string including the modifier
	 */
	public String toString()
	{
		return this.modifier + " [" + this.unit() + "]";
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
	
	/**
	 * build the unit map from string
	 * @param units
	 */
	public void fromString(String units)
	{
		Log.out(Tier.BULK, "Interpretting unit string: " + units);
		
		/* replace all unit braces and all white space */
		units.replaceAll("\\[\\]\\s+", "");
		
		/* split by dot · */
		String[] unitsArray = units.split("·");
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
		Log.out(Tier.BULK, "SI interpretation: " + toString());
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
				this.mutation( SI.s, update );
				this.modifier *= Math.pow( 60.0, power );
				break;
			case "h" :
				this.mutation( SI.s, update );
				this.modifier *= Math.pow( 3600.0, power );
				break;
			case "d" :
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
			case "F" :
				this.mutation( SI.K, update );
				this.modifier /= Math.pow( 1.8, power );
		}
	}
}

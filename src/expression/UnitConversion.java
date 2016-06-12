package expression;

import java.util.HashMap;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlRef;

public class UnitConversion {
	
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

	public class Units
	{
		private HashMap<SI, Integer> unitMap = new HashMap<SI, Integer>();
		private double modifier;
		
		public Units()
		{
			for (SI si : SI.values())
				this.unitMap.put(si, 0);
			this.modifier = 1;
		}
		
		/**
		 * output the units as string
		 */
		public String toString()
		{
			String out = this.modifier + " ";
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
			return out + "";
		}
		
		/**
		 * build the unit map from string
		 * @param units
		 */
		public void fromString(String units)
		{
			Log.out(Tier.DEBUG, "Interpretting unit string: " + units);
			
			/* replace all unit braces and all white space */
			units.replaceAll("\\[\\]\\s+", "");
			
			String[] unitsArray = units.split("·");
			String[] unitPower;
			Integer power;
			/* analyse the powers */
			for (String s : unitsArray)
			{
				/* try + */
				unitPower = s.split("\\+");
				/* if not + */
				if (unitPower.length == 1)
				{
					/* try - */
					unitPower= s.split("-");
					/* if not + or - the power is 1 ( no sign ). */
					if (unitPower.length == 1)
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
			Log.out(Tier.DEBUG, "SI interpretation: " + toString());
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
}

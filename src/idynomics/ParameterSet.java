package idynomics;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Properties;

import utility.Helper;

/**
 * General class to efficiently load parameters from cfg file
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract class ParameterSet {

	public ParameterSet(Properties... properties)
	{
		this.set( properties );
	}
	
	public void set(Properties... properties)
	{
		for (Properties p : properties )
		{
			Field[] fields = this.getClass().getDeclaredFields();
			for( Field f : fields)
			{
				if( p.containsKey( f.getName() ) )
				{
					try {
						if( f.getType().equals( String.class ) )
							f.set(this, (String) p.get( f.getName() ) );
						if( f.getType().equals( Double.class ) )
							f.set(this, Double.valueOf( 
									(String) p.get( f.getName() ) ) );
						if( f.getType().equals( Integer.class ) )
							f.set(this, Integer.valueOf( 
									(String) p.get( f.getName() ) ) );
						if( f.getType().equals( Color.class ) )
							f.set(this, Helper.obtainColor( 
									(String) p.get( f.getName() ) ) );
						if( f.getType().equals( Boolean.class ) )
							f.set(this, Boolean.valueOf(
									(String) p.get( f.getName() ) ) );
					} catch (IllegalArgumentException | 
							IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

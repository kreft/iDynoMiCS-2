package idynomics;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
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
	
	public void set(String...properties)
	{
		Properties props = new Properties();
		if ( ! Helper.isNullOrEmpty( properties ) )
		{
			for( String s : properties)
			{
				try {
					props.load( new FileInputStream(s) );
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			set( props );
		}
	}
	
	public void set(Properties... properties)
	{
		if ( ! Helper.isNullOrEmpty( properties ) )
		{
			for (Properties p : properties )
			{
				Field[] fields = this.getClass().getDeclaredFields();
				for( Field f : fields)
				{
					if( p.containsKey( f.getName() ) )
						setField( p, f );
				}
			}
		}
	}
	
	private void setField(Properties p, Field f)
	{
		try 
		{
			if( f.getType().equals( String.class ) )
			{
				f.set(this, (String) p.get( f.getName() ) );
			}
			else if( f.getType().equals( Double.class ) ||  
					f.getType().equals( double.class ) )
			{
				f.set(this, Double.valueOf( (String) p.get( f.getName() ) ) );
			}
			else if( f.getType().equals( Integer.class ) ||
					f.getType().equals( int.class ) )
			{
				f.set(this, Integer.valueOf( (String) p.get( f.getName() ) ) );
			}
			else if( f.getType().equals( Color.class ) )
			{
				f.set(this, 
						Helper.obtainColor( (String) p.get( f.getName() ) ) );
			}
			else if( f.getType().equals( Boolean.class ) || 
					f.getType().equals( boolean.class ))
			{
				f.set(this, Boolean.valueOf( (String) p.get( f.getName() ) ) );
			}
			else if( f.getType().equals( String[].class ) )
			{
				f.set(this, ( (String) p.get( f.getName() ) ).split(",") );
			}
			else if( f.getType().equals( SimpleDateFormat.class) )
			{
				f.set(this, 
						new SimpleDateFormat( (String) p.get( f.getName() ) ) );
			}
		} 
		catch (IllegalArgumentException | IllegalAccessException e) 
		{
			e.printStackTrace();
		}
	}
}

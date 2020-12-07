package surface.link;

import expression.Expression;
import instantiable.Instantiable;
import settable.Settable;
import shape.Shape;
import surface.Point;

/**
 * 
 * @author Bastiaan
 *
 */
public interface Spring extends Instantiable, Settable {
	
	public void applyForces(Shape shape);

	public void setRestValue(double restValue);
	
	public void setSpringFunction( Expression function );
	
	public void setStiffness(double stiffness);
	
	public boolean ready();
	
	public void setPoint(int i, Point points, boolean tempDuplicate);
	
	@Override
	public default void setParent(Settable parent) 
	{
		
	}

	@Override
	public default Settable getParent() 
	{
		return null;
	}
}

package surface;

import expression.Expression;
import instantiable.Instantiable;
import settable.Settable;
import shape.Shape;

public interface Spring extends Instantiable, Settable {
	
	public void applyForces(Shape shape);

	public void setRestValue(double restValue);
	
	public void setSpringFunction( Expression function );
	
	public void setStiffness(double stiffness);
	
	public boolean ready();
	
	public void setPoint(int i, Point points);
	
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

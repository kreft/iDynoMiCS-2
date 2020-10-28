package surface;

import expression.Expression;
import shape.Shape;

public interface Spring {
	
	public void applyForces(Shape shape);

	public void setRestValue(double restValue);
	
	public void setSpringFunction( Expression function );
	
	public void setStiffness(double stiffness);
	
	public boolean ready();
}

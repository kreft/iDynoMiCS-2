package glRender;

import com.jogamp.opengl.GLAutoDrawable;

public interface CommandMediator {

	public abstract void draw(GLAutoDrawable drawable);

	public abstract void draw(GLAutoDrawable drawable, float zoom, float tilt);
}

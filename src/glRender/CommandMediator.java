package glRender;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * CommandMediators are passed to the openGL renderer to express the 3D scene
 * @author baco
 *
 */
public interface CommandMediator {

	public abstract float kickback();

	/**
	 * simple drawing without zoom and tilt options
	 * @param drawable
	 */
	public abstract void draw(GLAutoDrawable drawable);

}

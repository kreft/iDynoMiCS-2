package glRender;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * CommandMediators are passed to the openGL renderer to express the 3D scene
 * @author baco
 *
 */
public interface CommandMediator {

	/**
	 * simple drawing without zoom and tilt options
	 * @param drawable
	 */
	public abstract void draw(GLAutoDrawable drawable);

	/**
	 * drawing that adjusts to tilt and zoom commands
	 * @param drawable
	 * @param zoom
	 * @param tilt
	 */
	public abstract void draw(GLAutoDrawable drawable, float zoom, float tilt);
}
